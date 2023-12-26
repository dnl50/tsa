package dev.mieser.tsa.signing.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.datetime.impl.DateConverterImpl;
import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.api.exception.InvalidCertificateException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.impl.cert.CertificateParser;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateExtractor;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateHolder;
import dev.mieser.tsa.signing.impl.cert.SigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.mapper.TimeStampValidationResultMapper;
import dev.mieser.tsa.testutil.CertificateGenerator;
import dev.mieser.tsa.testutil.TestKeyLoader;

@ExtendWith(MockitoExtension.class)
class BouncyCastleTimeStampValidatorTest {

    @Mock
    private SigningKeystoreLoader signingKeystoreLoaderMock;

    private BouncyCastleTimeStampValidator testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new BouncyCastleTimeStampValidator(new TspParser(), signingKeystoreLoaderMock,
            new TimeStampValidationResultMapper(new DateConverterImpl()), new SigningCertificateExtractor(),
            new CertificateParser());
    }

    @Nested
    class ValidateWithoutCertificate {

        @Test
        void initializeThrowsExceptionWhenUnsupportedPublicKeyAlgorithmIsUsed() throws Exception {
            // given
            given(signingKeystoreLoaderMock.loadCertificate()).willReturn(CertificateGenerator.createEd25519Certificate());

            // when / then
            assertThatExceptionOfType(TsaInitializationException.class)
                .isThrownBy(testSubject::initialize)
                .withCauseInstanceOf(InvalidCertificateException.class);
        }

        @Test
        void throwsExceptionWhenResponseCannotBeParsed() {
            // given
            InputStream invalidResponse = new ByteArrayInputStream("invalid".getBytes());

            given(signingKeystoreLoaderMock.loadCertificate()).willReturn(TestKeyLoader.loadEcCertificate());
            testSubject.initialize();

            // when / then
            assertThatExceptionOfType(InvalidTspResponseException.class)
                .isThrownBy(() -> testSubject.validateResponse(invalidResponse));
        }

        @Test
        void signatureIsInvalidWhenResponseIsNotSignedUsingCorrespondingPrivateKey() throws Exception {
            // given
            InputStream tspResponse = new ByteArrayInputStream(readValidResponse());

            given(signingKeystoreLoaderMock.loadCertificate()).willReturn(TestKeyLoader.loadEcCertificate());
            testSubject.initialize();

            // when
            TimeStampValidationResult actual = testSubject.validateResponse(tspResponse);

            // then
            assertThat(actual.isSignatureValid()).isFalse();
        }

        @Test
        void signatureIsValidWhenResponseIsSignedUsingCorrespondingPrivateKey() throws Exception {
            // given
            InputStream tspResponse = new ByteArrayInputStream(readValidResponse());
            X509CertificateHolder matchingCertificate = new SigningCertificateExtractor()
                .extractSigningCertificate(new TimeStampResponse(readValidResponse()))
                .map(SigningCertificateHolder::getSigningCertificate)
                .orElseThrow();

            given(signingKeystoreLoaderMock.loadCertificate())
                .willReturn(new JcaX509CertificateConverter().getCertificate(matchingCertificate));
            testSubject.initialize();

            // when
            TimeStampValidationResult actual = testSubject.validateResponse(tspResponse,
                new ByteArrayInputStream(matchingCertificate.getEncoded()));

            // then
            assertThat(actual.isSignatureValid()).isTrue();
        }

    }

    @Nested
    class ValidateWithCertificate {

        @Test
        void throwsExceptionWhenCertificateCannotBeParsed() throws IOException {
            // given
            InputStream validResponse = new ByteArrayInputStream(readValidResponse());
            InputStream invalidCertificate = new ByteArrayInputStream("cert".getBytes());

            // when / then
            assertThatExceptionOfType(InvalidCertificateException.class)
                .isThrownBy(() -> testSubject.validateResponse(validResponse, invalidCertificate));
        }

        @Test
        void throwsExceptionWhenUnsupportedPublicKeyAlgorithmIsUsed() throws Exception {
            // given
            InputStream validResponse = new ByteArrayInputStream(readValidResponse());
            InputStream invalidCertificate = new ByteArrayInputStream(
                CertificateGenerator.createEd25519Certificate().getEncoded());

            // when / then
            assertThatExceptionOfType(InvalidCertificateException.class)
                .isThrownBy(() -> testSubject.validateResponse(validResponse, invalidCertificate))
                .withMessage("Unsupported public key algorithm 'EdDSA'.");
        }

        @Test
        void throwsExceptionWhenResponseCannotBeParsed() throws Exception {
            // given
            InputStream invalidResponse = new ByteArrayInputStream("invalid".getBytes());
            InputStream validCertificate = new ByteArrayInputStream(TestKeyLoader.loadEcCertificate().getEncoded());

            // when / then
            assertThatExceptionOfType(InvalidTspResponseException.class)
                .isThrownBy(() -> testSubject.validateResponse(invalidResponse, validCertificate));
        }

        @Test
        void signatureIsInvalidWhenResponseIsNotSignedUsingCorrespondingPrivateKey() throws Exception {
            // given
            InputStream tspResponse = new ByteArrayInputStream(readValidResponse());
            InputStream nonMatchingCertificate = new ByteArrayInputStream(TestKeyLoader.loadRsaCertificate().getEncoded());

            // when
            TimeStampValidationResult actual = testSubject.validateResponse(tspResponse, nonMatchingCertificate);

            // then
            assertThat(actual.isSignatureValid()).isFalse();
        }

        @Test
        void signatureIsValidWhenResponseIsSignedUsingCorrespondingPrivateKey() throws Exception {
            // given
            InputStream tspResponse = new ByteArrayInputStream(readValidResponse());
            X509CertificateHolder matchingCertificate = new SigningCertificateExtractor()
                .extractSigningCertificate(new TimeStampResponse(readValidResponse()))
                .map(SigningCertificateHolder::getSigningCertificate)
                .orElseThrow();

            // when
            TimeStampValidationResult actual = testSubject.validateResponse(tspResponse,
                new ByteArrayInputStream(matchingCertificate.getEncoded()));

            // then
            assertThat(actual.isSignatureValid()).isTrue();
        }

    }

    private byte[] readValidResponse() throws IOException {
        try (var resource = getClass().getResourceAsStream("digicert-response-2023-08-13.asn1")) {
            return resource.readAllBytes();
        }
    }

}
