package dev.mieser.tsa.signing;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;
import static dev.mieser.tsa.signing.cert.PublicKeyAlgorithm.*;
import static dev.mieser.tsa.testutil.TestCertificateLoader.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.BDDMockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Optional;

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

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import dev.mieser.tsa.signing.cert.PublicKeyAnalyzer;
import dev.mieser.tsa.signing.cert.SigningCertificateExtractor;
import dev.mieser.tsa.signing.cert.SigningCertificateHolder;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.mapper.TimeStampValidationResultMapper;

@ExtendWith(MockitoExtension.class)
class BouncyCastleTimeStampValidatorTest {

    @Mock
    private TspParser tspParserMock;

    @Mock
    private SigningCertificateLoader signingCertificateLoaderMock;

    @Mock
    private PublicKeyAnalyzer publicKeyAnalyzerMock;

    @Mock
    private TimeStampValidationResultMapper timeStampValidationResultMapperMock;

    @Mock
    private TspValidator tspValidatorMock;

    @Mock
    private SigningCertificateExtractor signingCertificateExtractorMock;

    @Nested
    class ValidateResponseTest {

        @Test
        void throwsExceptionWhenValidatorIsNotInitialized() {
            // given
            BouncyCastleTimeStampValidator testSubject = createUninitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));

            // when / then
            assertThatExceptionOfType(TsaNotInitializedException.class)
                .isThrownBy(() -> testSubject.validateResponse(responseStream));
        }

        @Test
        void throwsExceptionWhenHashAlgorithmIsUnknown(
            @Mock(answer = RETURNS_DEEP_STUBS) TimeStampResponse timeStampResponseMock) throws IOException {
            // given
            BouncyCastleTimeStampValidator testSubject = createInitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));
            var md5Oid = new ASN1ObjectIdentifier("1.2.840.113549.2.5");

            given(tspParserMock.parseResponse(responseStream)).willReturn(timeStampResponseMock);
            given(timeStampResponseMock.getTimeStampToken().getTimeStampInfo().getMessageImprintAlgOID()).willReturn(md5Oid);
            given(tspValidatorMock.isKnownHashAlgorithm(md5Oid)).willReturn(false);

            // when / then
            assertThatExceptionOfType(UnknownHashAlgorithmException.class)
                .isThrownBy(() -> testSubject.validateResponse(responseStream))
                .withMessage("Unknown hash algorithm OID '1.2.840.113549.2.5'.");
        }

        @Test
        void validateResponseUsesConfiguredCertificateToValidateResponse(@Mock TimeStampResponse timeStampResponseMock,
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
            testSubject.validateResponse(responseStream);

            // then
            ArgumentCaptor<SignerInformationVerifier> signerInformationCaptor = ArgumentCaptor
                .forClass(SignerInformationVerifier.class);
            then(timeStampTokenMock).should().validate(signerInformationCaptor.capture());

            assertThat(signerInformationCaptor.getValue().getAssociatedCertificate().getEncoded())
                .isEqualTo(loadEcCertificate().getEncoded());
        }

        @Test
        void validateResponseMarksResponseAsSignedByOtherTsaWhenValidationFails(@Mock TimeStampResponse timeStampResponseMock,
            @Mock(answer = RETURNS_DEEP_STUBS) TimeStampToken timeStampTokenMock) throws Exception {
            // given
            BouncyCastleTimeStampValidator testSubject = createInitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));
            var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
            TimeStampValidationResult validationResult = TimeStampValidationResult.builder().build();

            given(tspParserMock.parseResponse(responseStream)).willReturn(timeStampResponseMock);
            given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
            given(timeStampTokenMock.getTimeStampInfo().getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
            given(tspValidatorMock.isKnownHashAlgorithm(hashAlgorithmOid)).willReturn(true);
            willThrow(new TSPException("Validation Error!!1!")).given(timeStampTokenMock).validate(notNull());
            given(signingCertificateExtractorMock.extractSigningCertificate(timeStampResponseMock)).willReturn(Optional.empty());
            given(timeStampValidationResultMapperMock.map(timeStampResponseMock, null, false)).willReturn(validationResult);

            // when
            TimeStampValidationResult actualVerificationResult = testSubject.validateResponse(responseStream);

            // then
            assertThat(actualVerificationResult).isEqualTo(validationResult);
        }

        @Test
        void validateResponseMarksResponseAsSignedByThisTsaWhenValidationSucceeds(@Mock TimeStampResponse timeStampResponseMock,
            @Mock(answer = RETURNS_DEEP_STUBS) TimeStampToken timeStampTokenMock) throws Exception {
            // given
            BouncyCastleTimeStampValidator testSubject = createInitializedTestSubject();
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));
            var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
            TimeStampValidationResult validationResult = TimeStampValidationResult.builder().build();
            SigningCertificateHolder signingCertificateHolder = new SigningCertificateHolder(null, new byte[0], null);

            given(tspParserMock.parseResponse(responseStream)).willReturn(timeStampResponseMock);
            given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
            given(timeStampTokenMock.getTimeStampInfo().getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
            given(tspValidatorMock.isKnownHashAlgorithm(hashAlgorithmOid)).willReturn(true);
            willDoNothing().given(timeStampTokenMock).validate(notNull());
            given(signingCertificateExtractorMock.extractSigningCertificate(timeStampResponseMock))
                .willReturn(Optional.of(signingCertificateHolder));
            given(timeStampValidationResultMapperMock.map(timeStampResponseMock, signingCertificateHolder, true))
                .willReturn(validationResult);

            // when
            TimeStampValidationResult actualVerificationResult = testSubject.validateResponse(responseStream);

            // then
            assertThat(actualVerificationResult).isEqualTo(validationResult);
        }

        @Test
        void validateMarksResponseAsSignedByOtherTsaWhenResponseDoesNotContainToken(
            @Mock TimeStampResponse timeStampResponseMock) throws Exception {
            // given
            InputStream responseStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));

            given(tspParserMock.parseResponse(responseStream)).willReturn(timeStampResponseMock);
            given(signingCertificateExtractorMock.extractSigningCertificate(timeStampResponseMock)).willReturn(Optional.empty());

            BouncyCastleTimeStampValidator testSubject = createInitializedTestSubject();

            // when
            testSubject.validateResponse(responseStream);

            // then
            then(timeStampValidationResultMapperMock).should().map(timeStampResponseMock, null, false);
        }

    }

    @Nested
    class InitializeTest {

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

        @Test
        void initializeAcceptsDsaCertificates() throws IOException {
            // given
            X509Certificate dsaCertificate = loadDsaCertificate();

            given(signingCertificateLoaderMock.loadCertificate()).willReturn(dsaCertificate);
            given(publicKeyAnalyzerMock.publicKeyAlgorithm(dsaCertificate)).willReturn(DSA);

            BouncyCastleTimeStampValidator testSubject = createUninitializedTestSubject();

            // when / then
            assertThatCode(testSubject::initialize).doesNotThrowAnyException();
        }

    }

    private BouncyCastleTimeStampValidator createUninitializedTestSubject() {
        return new BouncyCastleTimeStampValidator(tspParserMock, signingCertificateLoaderMock, publicKeyAnalyzerMock,
            timeStampValidationResultMapperMock,
            tspValidatorMock, signingCertificateExtractorMock);
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
