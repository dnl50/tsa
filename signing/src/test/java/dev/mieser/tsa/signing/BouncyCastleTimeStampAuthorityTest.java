package dev.mieser.tsa.signing;

import dev.mieser.tsa.datetime.api.CurrentDateTimeService;
import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import dev.mieser.tsa.signing.cert.PublicKeyAlgorithm;
import dev.mieser.tsa.signing.cert.PublicKeyAnalyzer;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.config.TsaProperties;
import dev.mieser.tsa.signing.mapper.TimestampResponseMapper;
import dev.mieser.tsa.signing.serial.SerialNumberGenerator;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.EnumSet;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;
import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static dev.mieser.tsa.signing.TestCertificateLoader.loadRsaCertificate;
import static dev.mieser.tsa.signing.TestCertificateLoader.loadRsaPrivateKey;
import static dev.mieser.tsa.signing.cert.PublicKeyAlgorithm.RSA;
import static java.math.BigInteger.ONE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BouncyCastleTimeStampAuthorityTest {

    private static final byte[] SHA_256_HASH = "32 character long ASCII sequence".getBytes(UTF_8);

    private final TspParser tspParserMock;

    private final TspRequestValidator tspRequestValidatorMock;

    private final SigningCertificateLoader signingCertificateLoaderMock;

    private final CurrentDateTimeService currentDateTimeServiceMock;

    private final SerialNumberGenerator serialNumberGeneratorMock;

    private final TimestampResponseMapper timestampResponseMapperMock;

    private final PublicKeyAnalyzer publicKeyAnalyzerMock;

    BouncyCastleTimeStampAuthorityTest(@Mock TspParser tspParserMock, @Mock TspRequestValidator tspRequestValidatorMock,
                                       @Mock SigningCertificateLoader signingCertificateLoaderMock, @Mock CurrentDateTimeService currentDateTimeServiceMock,
                                       @Mock SerialNumberGenerator serialNumberGeneratorMock, @Mock TimestampResponseMapper timestampResponseMapper, @Mock PublicKeyAnalyzer publicKeyAnalyzerMock) {
        this.tspParserMock = tspParserMock;
        this.tspRequestValidatorMock = tspRequestValidatorMock;
        this.signingCertificateLoaderMock = signingCertificateLoaderMock;
        this.currentDateTimeServiceMock = currentDateTimeServiceMock;
        this.serialNumberGeneratorMock = serialNumberGeneratorMock;
        this.timestampResponseMapperMock = timestampResponseMapper;
        this.publicKeyAnalyzerMock = publicKeyAnalyzerMock;
    }

    @Nested
    class InitializeTest {

        @Test
        void throwsExceptionWhenInitializationFails() throws IOException {
            // given
            BouncyCastleTimeStampAuthority testSubject = createTestSubject(new TsaProperties());

            IOException cause = new IOException();
            when(signingCertificateLoaderMock.loadCertificate()).thenThrow(cause);

            // when / then
            assertThatExceptionOfType(TsaInitializationException.class)
                    .isThrownBy(testSubject::initialize)
                    .withMessage("Could not initialize TSA.")
                    .withCause(cause);
        }

    }

    @Nested
    class SignRequestTest {

        @Test
        void throwsExceptionWhenTsaIsNotInitialized() {
            // given
            var tspRequestInputStream = new ByteArrayInputStream(new byte[0]);

            BouncyCastleTimeStampAuthority testSubject = createTestSubject(new TsaProperties());

            // when / then
            assertThatExceptionOfType(TsaNotInitializedException.class)
                    .isThrownBy(() -> testSubject.signRequest(tspRequestInputStream));
        }

        @Test
        void throwsExceptionWhenTsaDoesNotRecognizeHashAlgorithm(@Mock TimeStampRequest timeStampRequestMock) throws Exception {
            // given
            var tspRequestInputStream = new ByteArrayInputStream(new byte[0]);
            var tsaProperties = new TsaProperties();
            tsaProperties.setEssCertIdAlgorithm(HashAlgorithm.SHA1);
            tsaProperties.setSigningDigestAlgorithm(SHA256);

            X509Certificate certificate = loadRsaCertificate();
            PrivateKey privateKey = loadRsaPrivateKey();

            given(tspParserMock.parseRequest(tspRequestInputStream)).willReturn(timeStampRequestMock);
            given(tspRequestValidatorMock.isKnownHashAlgorithm(timeStampRequestMock)).willReturn(false);
            given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(new ASN1ObjectIdentifier("1.2.840.113549.2.5"));

            BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, RSA, certificate, privateKey);

            // when / then
            assertThatExceptionOfType(UnknownHashAlgorithmException.class)
                    .isThrownBy(() -> testSubject.signRequest(tspRequestInputStream))
                    .withMessage("Unknown hash algorithm OID '1.2.840.113549.2.5'.");
        }

        @Test
        void returnsMappedTspResponse(@Mock TimestampResponseData timestampResponseDataMock) throws Exception {
            // given
            TimeStampRequest tspRequest = createSha256TimestampRequest();
            var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());
            var tsaProperties = new TsaProperties();
            tsaProperties.setAcceptedHashAlgorithms(EnumSet.of(SHA512));

            X509Certificate certificate = loadRsaCertificate();
            PrivateKey privateKey = loadRsaPrivateKey();

            given(tspParserMock.parseRequest(tspRequestInputStream)).willReturn(tspRequest);
            given(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).willReturn(true);
            given(timestampResponseMapperMock.map(eq(tspRequest), any())).willReturn(timestampResponseDataMock);
            given(currentDateTimeServiceMock.now()).willReturn(new Date());
            given(serialNumberGeneratorMock.generateSerialNumber()).willReturn(BigInteger.TEN);

            BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, RSA, certificate, privateKey);

            // when
            TimestampResponseData actualResponseData = testSubject.signRequest(tspRequestInputStream);

            // then
            assertThat(actualResponseData).isEqualTo(timestampResponseDataMock);
        }

        @Test
        void doesNotSignRequestWhenHashAlgorithmIsNotAccepted() throws Exception {
            // given
            TimeStampRequest tspRequest = createSha256TimestampRequest();
            var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());
            var tsaProperties = new TsaProperties();
            tsaProperties.setAcceptedHashAlgorithms(EnumSet.of(SHA512));

            X509Certificate certificate = loadRsaCertificate();
            PrivateKey privateKey = loadRsaPrivateKey();

            given(tspParserMock.parseRequest(tspRequestInputStream)).willReturn(tspRequest);
            given(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).willReturn(true);
            given(currentDateTimeServiceMock.now()).willReturn(new Date());
            given(serialNumberGeneratorMock.generateSerialNumber()).willReturn(BigInteger.TEN);

            BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, RSA, certificate, privateKey);

            // when
            testSubject.signRequest(tspRequestInputStream);

            // then
            ArgumentCaptor<TimeStampResponse> tspResponseCaptor = ArgumentCaptor.forClass(TimeStampResponse.class);
            then(timestampResponseMapperMock).should().map(eq(tspRequest), tspResponseCaptor.capture());

            TimeStampResponse generatedTspResponse = tspResponseCaptor.getValue();
            assertThat(generatedTspResponse.getFailInfo()).extracting(PKIFailureInfo::intValue).isEqualTo(PKIFailureInfo.badAlg);
        }

        @Test
        void signsRequestWhenHashAlgorithmIsAccepted() throws Exception {
            // given
            TimeStampRequest tspRequest = createSha256TimestampRequest();
            var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());

            X509Certificate certificate = loadRsaCertificate();
            PrivateKey privateKey = loadRsaPrivateKey();
            Date signingDate = Date.from(ZonedDateTime.parse("2021-11-06T15:57:37+01:00").toInstant());

            given(tspParserMock.parseRequest(tspRequestInputStream)).willReturn(tspRequest);
            given(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).willReturn(true);
            given(currentDateTimeServiceMock.now()).willReturn(signingDate);
            given(serialNumberGeneratorMock.generateSerialNumber()).willReturn(BigInteger.TEN);

            BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(new TsaProperties(), RSA, certificate, privateKey);

            // when
            testSubject.signRequest(tspRequestInputStream);

            // then
            ArgumentCaptor<TimeStampResponse> tspResponseCaptor = ArgumentCaptor.forClass(TimeStampResponse.class);
            then(timestampResponseMapperMock).should().map(eq(tspRequest), tspResponseCaptor.capture());

            TimeStampResponse generatedTspResponse = tspResponseCaptor.getValue();
            assertThat(generatedTspResponse.getTimeStampToken().getTimeStampInfo().getGenTime()).isEqualTo(signingDate);
        }

        @Test
        void usesConfiguredPolicy() throws Exception {
            // given
            TimeStampRequest tspRequest = createSha256TimestampRequest();
            var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());
            var tsaProperties = new TsaProperties();
            tsaProperties.setPolicyOid("1.2.3.4.5");

            X509Certificate certificate = loadRsaCertificate();
            PrivateKey privateKey = loadRsaPrivateKey();
            Date signingDate = Date.from(ZonedDateTime.parse("2021-11-06T15:57:37+01:00").toInstant());

            given(tspParserMock.parseRequest(tspRequestInputStream)).willReturn(tspRequest);
            given(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).willReturn(true);
            given(currentDateTimeServiceMock.now()).willReturn(signingDate);
            given(serialNumberGeneratorMock.generateSerialNumber()).willReturn(BigInteger.TEN);

            BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, RSA, certificate, privateKey);

            // when
            testSubject.signRequest(tspRequestInputStream);

            // then
            ArgumentCaptor<TimeStampResponse> tspResponseCaptor = ArgumentCaptor.forClass(TimeStampResponse.class);
            then(timestampResponseMapperMock).should().map(eq(tspRequest), tspResponseCaptor.capture());

            TimeStampResponse generatedTspResponse = tspResponseCaptor.getValue();
            assertThat(generatedTspResponse.getTimeStampToken().getTimeStampInfo().getPolicy()).isEqualTo(new ASN1ObjectIdentifier("1.2.3.4.5"));
        }

        @Test
        void usesGeneratedSerialNumber() throws Exception {
            // given
            TimeStampRequest tspRequest = createSha256TimestampRequest();
            var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());
            var tsaProperties = new TsaProperties();
            tsaProperties.setPolicyOid("1.2.3.4.5");

            X509Certificate certificate = loadRsaCertificate();
            PrivateKey privateKey = loadRsaPrivateKey();
            Date signingDate = Date.from(ZonedDateTime.parse("2021-11-06T15:57:37+01:00").toInstant());

            given(tspParserMock.parseRequest(tspRequestInputStream)).willReturn(tspRequest);
            given(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).willReturn(true);
            given(currentDateTimeServiceMock.now()).willReturn(signingDate);
            given(serialNumberGeneratorMock.generateSerialNumber()).willReturn(BigInteger.TEN);

            BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, RSA, certificate, privateKey);

            // when
            testSubject.signRequest(tspRequestInputStream);

            // then
            ArgumentCaptor<TimeStampResponse> tspResponseCaptor = ArgumentCaptor.forClass(TimeStampResponse.class);
            then(timestampResponseMapperMock).should().map(eq(tspRequest), tspResponseCaptor.capture());

            TimeStampResponse generatedTspResponse = tspResponseCaptor.getValue();
            assertThat(generatedTspResponse.getTimeStampToken().getTimeStampInfo().getSerialNumber()).isEqualTo(BigInteger.TEN);
        }

    }

    private BouncyCastleTimeStampAuthority createTestSubject(TsaProperties tsaProperties) {
        return new BouncyCastleTimeStampAuthority(tsaProperties, tspParserMock, tspRequestValidatorMock, signingCertificateLoaderMock,
                currentDateTimeServiceMock, serialNumberGeneratorMock, timestampResponseMapperMock, publicKeyAnalyzerMock);
    }

    private BouncyCastleTimeStampAuthority createInitializedTestSubject(TsaProperties tsaProperties, PublicKeyAlgorithm publicKeyAlgorithm, X509Certificate x509Certificate, PrivateKey privateKey) throws IOException {
        given(signingCertificateLoaderMock.loadCertificate()).willReturn(x509Certificate);
        given(signingCertificateLoaderMock.loadPrivateKey()).willReturn(privateKey);
        given(publicKeyAnalyzerMock.publicKeyAlgorithm(x509Certificate)).willReturn(publicKeyAlgorithm);

        BouncyCastleTimeStampAuthority testSubject = createTestSubject(tsaProperties);
        testSubject.initialize();
        return testSubject;
    }

    private TimeStampRequest createSha256TimestampRequest() {
        var imprint = new MessageImprint(new AlgorithmIdentifier(new ASN1ObjectIdentifier(SHA256.getObjectIdentifier())), SHA_256_HASH);
        return new TimeStampRequest(new TimeStampReq(imprint, null, new ASN1Integer(ONE), ASN1Boolean.TRUE, null));
    }

}
