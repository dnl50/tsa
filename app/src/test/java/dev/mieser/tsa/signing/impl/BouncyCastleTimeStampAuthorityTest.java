package dev.mieser.tsa.signing.impl;

import static dev.mieser.tsa.signing.config.HashAlgorithm.*;
import static dev.mieser.tsa.signing.impl.cert.PublicKeyAlgorithm.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Stream;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.datetime.impl.DateConverterImpl;
import dev.mieser.tsa.domain.FailureInfo;
import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.domain.TimeStampRequestData;
import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.config.DigestAlgorithmConverter;
import dev.mieser.tsa.signing.impl.cert.PublicKeyAlgorithm;
import dev.mieser.tsa.signing.impl.cert.SigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.mapper.TimeStampResponseMapper;
import dev.mieser.tsa.signing.impl.testutil.ConfigurableSigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.testutil.CurrentDateServiceStub;
import dev.mieser.tsa.signing.impl.testutil.DelegatingTsaProperties;
import dev.mieser.tsa.signing.impl.testutil.SerialNumberGeneratorStub;
import dev.mieser.tsa.signing.impl.testutil.TsaConfiguration;
import dev.mieser.tsa.testutil.TestKeyLoader;

@ExtendWith(MockitoExtension.class)
class BouncyCastleTimeStampAuthorityTest {

    private final DelegatingTsaProperties delegatingTsaProperties = new DelegatingTsaProperties();

    private final ConfigurableSigningKeystoreLoader configurableSigningCertificateLoader = new ConfigurableSigningKeystoreLoader();

    private final CurrentDateServiceStub currentDateServiceStub = new CurrentDateServiceStub();

    private final SerialNumberGeneratorStub serialNumberGeneratorStub = new SerialNumberGeneratorStub();

    private BouncyCastleTimeStampAuthority testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new BouncyCastleTimeStampAuthority(delegatingTsaProperties,
            new TspParser(),
            configurableSigningCertificateLoader,
            currentDateServiceStub,
            serialNumberGeneratorStub,
            new TimeStampResponseMapper(new DateConverterImpl()),
            new DigestAlgorithmConverter());
    }

    @Nested
    class InitializationTest {

        @Test
        void throwsExceptionWhenUnsupportedPublicKeyAlgorithmIsDetected(@Mock SigningKeystoreLoader certificateLoaderMock,
            @Mock X509Certificate certificateMock, @Mock PublicKey publicKeyMock) throws Exception {
            // given
            var configuration = new TsaConfiguration(EC, SHA512, SHA512, Set.of(), "1.2", false);
            delegatingTsaProperties.setConfiguration(configuration);

            given(certificateLoaderMock.loadCertificate()).willReturn(certificateMock);
            given(certificateMock.getPublicKey()).willReturn(publicKeyMock);
            given(publicKeyMock.getAlgorithm()).willReturn("EdDSA");

            var testSubject = new BouncyCastleTimeStampAuthority(delegatingTsaProperties,
                new TspParser(),
                certificateLoaderMock,
                currentDateServiceStub,
                serialNumberGeneratorStub,
                new TimeStampResponseMapper(new DateConverterImpl()),
                new DigestAlgorithmConverter());

            // when / then
            assertThatExceptionOfType(TsaInitializationException.class)
                .isThrownBy(testSubject::initialize)
                .withMessage("Could not initialize TSA.")
                .havingCause()
                .withMessage("Public Key algorithm 'EdDSA' is not supported.");
        }

        @ParameterizedTest(name = "{index}: Initialize TSA with ''{0}''")
        @MethodSource("supportedTsaConfigurations")
        void initializeTsaWithSupportedConfigurations(TsaConfiguration tsaConfiguration) {
            // given
            configurableSigningCertificateLoader.setConfiguration(tsaConfiguration);
            delegatingTsaProperties.setConfiguration(tsaConfiguration);

            // when / then
            assertThatCode(testSubject::initialize).doesNotThrowAnyException();
        }

        static Stream<Arguments> supportedTsaConfigurations() {
            return Stream.of(
                // ECDSA
                arguments(
                    named("SHA512withECDSA", new TsaConfiguration(EC, SHA512, SHA256, Set.of(SHA1, SHA512), "1.3.3.7", false))),
                arguments(named("SHA256withECDSA", new TsaConfiguration(EC, SHA256, SHA512, Set.of(SHA1), "1.2", false))),
                arguments(named("SHA1withECDSA", new TsaConfiguration(EC, SHA1, SHA512, Set.of(SHA256), "1.3.3.7", false))),

                // RSA
                arguments(named("SHA512withRSA", new TsaConfiguration(RSA, SHA512, SHA256, Set.of(SHA1, SHA512), "1.2", false))),
                arguments(named("SHA256withRSA", new TsaConfiguration(RSA, SHA256, SHA512, Set.of(SHA1), "1.2", false))),
                arguments(
                    named("SHA1withRSA", new TsaConfiguration(RSA, SHA1, SHA1, Set.of(SHA1, SHA256, SHA512), "1.2", false))),

                // DSA
                arguments(named("SHA512withDSA", new TsaConfiguration(DSA, SHA512, SHA256, Set.of(SHA1, SHA512), "1.2", false))),
                arguments(named("SHA256withDSA", new TsaConfiguration(DSA, SHA256, SHA512, Set.of(SHA256), "1.2", false))));
        }

    }

    @Nested
    class SignatureTest {

        @Test
        void throwsExceptionWhenTsaIsNotInitialized() {
            // given
            InputStream tspRequestStream = new ByteArrayInputStream("request".getBytes());

            // when / then
            assertThatExceptionOfType(TsaNotInitializedException.class)
                .isThrownBy(() -> testSubject.signRequest(tspRequestStream));
        }

        @Test
        void rejectsRequestWhenHashAlgorithmIsNotAllowed() throws Exception {
            // given
            var sha1Imprint = new MessageImprint(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1),
                "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3".getBytes(UTF_8));
            var sha1Request = new TimeStampReq(sha1Imprint, null, null, ASN1Boolean.FALSE, null);
            InputStream tspRequestStream = new ByteArrayInputStream(sha1Request.getEncoded());

            var configuration = new TsaConfiguration(RSA, SHA512, SHA512, Set.of(SHA512), "1.2", false);
            delegatingTsaProperties.setConfiguration(configuration);
            configurableSigningCertificateLoader.setConfiguration(configuration);

            testSubject.initialize();

            // when
            TimeStampResponseData response = testSubject.signRequest(tspRequestStream);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.getStatus()).isEqualTo(ResponseStatus.REJECTION);
                softly.assertThat(response.getFailureInfo()).isEqualTo(FailureInfo.BAD_ALGORITHM);
            });
        }

        @ParameterizedTest
        @EnumSource
        void signsValidRequest(PublicKeyAlgorithm algorithm) throws Exception {
            // given
            byte[] sha256Hash = Base64.getDecoder().decode("n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=");
            var nonce = BigInteger.valueOf(53_125L);
            var sha256Imprint = new MessageImprint(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), sha256Hash);
            var sha256Request = new TimeStampReq(sha256Imprint, null, new ASN1Integer(nonce), ASN1Boolean.TRUE, null);
            byte[] asnEncodedRequest = sha256Request.getEncoded();

            var configuration = new TsaConfiguration(algorithm, SHA256, SHA256, Set.of(SHA512, SHA256), "1.2", false);
            delegatingTsaProperties.setConfiguration(configuration);
            configurableSigningCertificateLoader.setConfiguration(configuration);

            testSubject.initialize();

            ZonedDateTime now = LocalDateTime.parse("2023-06-21T13:37:00").atZone(ZoneId.systemDefault());
            currentDateServiceStub.setCurrentDateSupplier(() -> now);
            serialNumberGeneratorStub.setSerialNumberSupplier(() -> 1337L);

            // when
            TimeStampResponseData response = testSubject.signRequest(new ByteArrayInputStream(asnEncodedRequest));

            // then
            var expectedRequest = TimeStampRequestData.builder(SHA256.getObjectIdentifier(), sha256Hash, asnEncodedRequest)
                .nonce(nonce)
                .certificateRequested(true)
                .tsaPolicyId(null)
                .build();
            var expectedResponse = TimeStampResponseData
                .builder(ResponseStatus.GRANTED, now, expectedRequest, "not-assertable-here".getBytes())
                .receptionTime(now)
                .generationTime(now)
                .serialNumber(BigInteger.valueOf(1337L))
                .statusString("Operation Okay")
                .build();

            assertSoftly(softly -> {
                softly.assertThat(response).usingRecursiveComparison()
                    .ignoringFields("asnEncoded")
                    .isEqualTo(expectedResponse);

                // smoke test the ASN.1 DER encoded response
                softly.assertThat(TimeStampResp.getInstance(response.getAsnEncoded()).getStatus().getStatus())
                    .isEqualTo(ResponseStatus.GRANTED.getValue());
            });
        }

        @Test
        void includesCertificateSubject() throws Exception {
            byte[] sha256Hash = Base64.getDecoder().decode("n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=");
            var nonce = BigInteger.valueOf(1337L);
            var sha256Imprint = new MessageImprint(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), sha256Hash);
            var sha256Request = new TimeStampReq(sha256Imprint, null, new ASN1Integer(nonce), ASN1Boolean.TRUE, null);
            byte[] asnEncodedRequest = sha256Request.getEncoded();

            var configuration = new TsaConfiguration(EC, SHA256, SHA256, Set.of(SHA512, SHA256), "1.2", true);
            delegatingTsaProperties.setConfiguration(configuration);
            configurableSigningCertificateLoader.setConfiguration(configuration);

            testSubject.initialize();

            // when
            TimeStampResponseData response = testSubject.signRequest(new ByteArrayInputStream(asnEncodedRequest));

            // then
            var expectedName = new X509CertificateHolder(TestKeyLoader.loadEcCertificate().getEncoded()).getSubject();
            var actualName = (X500Name) new TimeStampResponse(TimeStampResp.getInstance(response.getAsnEncoded()))
                .getTimeStampToken()
                .getTimeStampInfo()
                .getTsa()
                .getName();

            assertThat(actualName).isEqualTo(expectedName);
        }

    }

}
