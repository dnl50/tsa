package dev.mieser.tsa.signing;

import dev.mieser.tsa.domain.TimestampVerificationResult;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import dev.mieser.tsa.signing.cert.PublicKeyAnalyzer;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.mapper.TimestampVerificationResultMapper;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;
import static dev.mieser.tsa.signing.cert.PublicKeyAlgorithm.*;
import static dev.mieser.tsa.testutil.TestCertificateLoader.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BouncyCastleTimeStampValidatorTest {

    private final TspParser tspParserMock;

    private final SigningCertificateLoader signingCertificateLoaderMock;

    private final PublicKeyAnalyzer publicKeyAnalyzerMock;

    private final TimestampVerificationResultMapper timestampVerificationResultMapperMock;

    private final TspValidator tspValidatorMock;

    BouncyCastleTimeStampValidatorTest(@Mock TspParser tspParserMock, @Mock SigningCertificateLoader signingCertificateLoaderMock,
            @Mock PublicKeyAnalyzer publicKeyAnalyzerMock, @Mock TimestampVerificationResultMapper timestampVerificationResultMapperMock,
            @Mock TspValidator tspValidatorMock) {
        this.tspParserMock = tspParserMock;
        this.signingCertificateLoaderMock = signingCertificateLoaderMock;
        this.publicKeyAnalyzerMock = publicKeyAnalyzerMock;
        this.timestampVerificationResultMapperMock = timestampVerificationResultMapperMock;
        this.tspValidatorMock = tspValidatorMock;
    }

    @Nested
    class VerifyResponseTest {

        @Test
        void throwsExceptionWhenValidatorIsNotInitialized() {
            // given
            BouncyCastleTimeStampValidator testSubject = createUninitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));

            // when / then
            assertThatExceptionOfType(TsaNotInitializedException.class)
                    .isThrownBy(() -> testSubject.verifyResponse(responseStream));
        }

        @Test
        void throwsExceptionWhenHashAlgorithmIsUnknown(@Mock(answer = RETURNS_DEEP_STUBS) TimeStampResponse timeStampResponseMock) throws IOException {
            // given
            BouncyCastleTimeStampValidator testSubject = createInitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));
            var md5Oid = new ASN1ObjectIdentifier("1.2.840.113549.2.5");

            given(tspParserMock.parseResponse(responseStream)).willReturn(timeStampResponseMock);
            given(timeStampResponseMock.getTimeStampToken().getTimeStampInfo().getMessageImprintAlgOID()).willReturn(md5Oid);
            given(tspValidatorMock.isKnownHashAlgorithm(md5Oid)).willReturn(false);

            // when / then
            assertThatExceptionOfType(UnknownHashAlgorithmException.class)
                    .isThrownBy(() -> testSubject.verifyResponse(responseStream))
                    .withMessage("Unknown hash algorithm OID '1.2.840.113549.2.5'.");
        }

        @Test
        void verifyResponseUsesConfiguredCertificateToValidateResponse(@Mock TimeStampResponse timeStampResponseMock,
                @Mock(answer = RETURNS_DEEP_STUBS) TimeStampToken timeStampTokenMock) throws Exception {
            // given
            BouncyCastleTimeStampValidator testSubject = createInitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));
            var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());

            given(tspParserMock.parseResponse(responseStream)).willReturn(timeStampResponseMock);
            given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
            given(timeStampTokenMock.getTimeStampInfo().getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
            given(tspValidatorMock.isKnownHashAlgorithm(hashAlgorithmOid)).willReturn(true);

            // when
            testSubject.verifyResponse(responseStream);

            // then
            ArgumentCaptor<SignerInformationVerifier> signerInformationCaptor = ArgumentCaptor.forClass(SignerInformationVerifier.class);
            then(timeStampTokenMock).should().validate(signerInformationCaptor.capture());

            assertThat(signerInformationCaptor.getValue().getAssociatedCertificate().getEncoded()).isEqualTo(loadEcCertificate().getEncoded());
        }

        @Test
        void verifyResponseMarksResponseAsSignedByOtherTsaWhenValidationFails(@Mock TimeStampResponse timeStampResponseMock,
                @Mock(answer = RETURNS_DEEP_STUBS) TimeStampToken timeStampTokenMock) throws Exception {
            // given
            BouncyCastleTimeStampValidator testSubject = createInitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));
            var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
            TimestampVerificationResult verificationResult = TimestampVerificationResult.builder().build();

            given(tspParserMock.parseResponse(responseStream)).willReturn(timeStampResponseMock);
            given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
            given(timeStampTokenMock.getTimeStampInfo().getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
            given(tspValidatorMock.isKnownHashAlgorithm(hashAlgorithmOid)).willReturn(true);
            willThrow(new TSPException("Validation Error!!1!")).given(timeStampTokenMock).validate(notNull());
            given(timestampVerificationResultMapperMock.map(timeStampResponseMock, false)).willReturn(verificationResult);

            // when
            TimestampVerificationResult actualVerificationResult = testSubject.verifyResponse(responseStream);

            // then
            assertThat(actualVerificationResult).isEqualTo(verificationResult);
        }

        @Test
        void verifyResponseMarksResponseAsSignedByThisTsaWhenValidationSucceeds(@Mock TimeStampResponse timeStampResponseMock,
                @Mock(answer = RETURNS_DEEP_STUBS) TimeStampToken timeStampTokenMock) throws Exception {
            // given
            BouncyCastleTimeStampValidator testSubject = createInitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));
            var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
            TimestampVerificationResult verificationResult = TimestampVerificationResult.builder().build();

            given(tspParserMock.parseResponse(responseStream)).willReturn(timeStampResponseMock);
            given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
            given(timeStampTokenMock.getTimeStampInfo().getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
            given(tspValidatorMock.isKnownHashAlgorithm(hashAlgorithmOid)).willReturn(true);
            willDoNothing().given(timeStampTokenMock).validate(notNull());
            given(timestampVerificationResultMapperMock.map(timeStampResponseMock, true)).willReturn(verificationResult);

            // when
            TimestampVerificationResult actualVerificationResult = testSubject.verifyResponse(responseStream);

            // then
            assertThat(actualVerificationResult).isEqualTo(verificationResult);
        }

    }

    @Nested
    class InitializeTest {

        @Test
        void initializeThrowsExceptionWhenPublicKeyAlgorithmIsNotSupported() throws IOException {
            // given
            X509Certificate dsaCertificate = loadCertificateFromClasspath("/dsa/cert.pem");

            given(signingCertificateLoaderMock.loadCertificate()).willReturn(dsaCertificate);
            given(publicKeyAnalyzerMock.publicKeyAlgorithm(dsaCertificate)).willReturn(DSA);

            BouncyCastleTimeStampValidator testSubject = createUninitializedTestSubject();

            // when / then
            assertThatExceptionOfType(TsaInitializationException.class)
                    .isThrownBy(testSubject::initialize)
                    .withMessage("Public key algorithm 'DSA' is not supported.");
        }

        @Test
        void initializeAcceptsRsaCertificates() throws IOException {
            // given
            X509Certificate rsaCertificate = loadRsaCertificate();

            given(signingCertificateLoaderMock.loadCertificate()).willReturn(rsaCertificate);
            given(publicKeyAnalyzerMock.publicKeyAlgorithm(rsaCertificate)).willReturn(RSA);

            BouncyCastleTimeStampValidator testSubject = createUninitializedTestSubject();

            // when / then
            assertThatCode(testSubject::initialize).doesNotThrowAnyException();
        }

        @Test
        void initializeAcceptsEcCertificates() throws IOException {
            // given
            X509Certificate ecCertificate = loadEcCertificate();

            given(signingCertificateLoaderMock.loadCertificate()).willReturn(ecCertificate);
            given(publicKeyAnalyzerMock.publicKeyAlgorithm(ecCertificate)).willReturn(EC);

            BouncyCastleTimeStampValidator testSubject = createUninitializedTestSubject();

            // when / then
            assertThatCode(testSubject::initialize).doesNotThrowAnyException();
        }

    }

    private BouncyCastleTimeStampValidator createUninitializedTestSubject() {
        return new BouncyCastleTimeStampValidator(tspParserMock, signingCertificateLoaderMock, publicKeyAnalyzerMock, timestampVerificationResultMapperMock,
                tspValidatorMock);
    }

    private BouncyCastleTimeStampValidator createInitializedTestSubject() throws IOException {
        BouncyCastleTimeStampValidator testSubject = createUninitializedTestSubject();
        X509Certificate ecCertificate = loadEcCertificate();

        given(signingCertificateLoaderMock.loadCertificate()).willReturn(ecCertificate);
        given(publicKeyAnalyzerMock.publicKeyAlgorithm(ecCertificate)).willReturn(EC);

        testSubject.initialize();

        return testSubject;
    }

}
