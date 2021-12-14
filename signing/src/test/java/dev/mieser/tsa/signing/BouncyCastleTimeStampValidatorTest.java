package dev.mieser.tsa.signing;

import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.cert.PublicKeyAnalyzer;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.mapper.TimestampVerificationResultMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class BouncyCastleTimeStampValidatorTest {

    private final TspParser tspParserMock;

    private final SigningCertificateLoader signingCertificateLoaderMock;

    private final PublicKeyAnalyzer publicKeyAnalyzerMock;

    private final TimestampVerificationResultMapper timestampVerificationResultMapperMock;

    BouncyCastleTimeStampValidatorTest(@Mock TspParser tspParserMock, @Mock SigningCertificateLoader signingCertificateLoaderMock,
                                       @Mock PublicKeyAnalyzer publicKeyAnalyzerMock, @Mock TimestampVerificationResultMapper timestampVerificationResultMapperMock) {
        this.tspParserMock = tspParserMock;
        this.signingCertificateLoaderMock = signingCertificateLoaderMock;
        this.publicKeyAnalyzerMock = publicKeyAnalyzerMock;
        this.timestampVerificationResultMapperMock = timestampVerificationResultMapperMock;
    }

    @Nested
    class VerifyResponseTest {

        @Test
        void throwsExceptionWhenValidatorIsNotInitialized() {
            // given
            BouncyCastleTimeStampValidator testSubject = createUninitializedTestSubject();
            InputStream asnStream = new ByteArrayInputStream("tsp response".getBytes(UTF_8));

            // when / then
            assertThatExceptionOfType(TsaNotInitializedException.class)
                    .isThrownBy(() -> testSubject.verifyResponse(asnStream));
        }

    }

    private BouncyCastleTimeStampValidator createUninitializedTestSubject() {
        return new BouncyCastleTimeStampValidator(tspParserMock, signingCertificateLoaderMock, publicKeyAnalyzerMock, timestampVerificationResultMapperMock);
    }

}
