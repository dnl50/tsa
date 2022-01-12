package dev.mieser.tsa.signing.cert;

import static dev.mieser.tsa.signing.cert.PublicKeyAlgorithm.RSA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicKeyAnalyzerTest {

    private final PublicKeyAnalyzer testSubject = new PublicKeyAnalyzer();

    @Test
    void publicKeyAlgorithmThrowsExceptionWhenAlgorithmIsUnknown(@Mock X509Certificate certificateMock,
        @Mock PublicKey publicKeyMock) {
        // given
        given(certificateMock.getPublicKey()).willReturn(publicKeyMock);
        given(publicKeyMock.getAlgorithm()).willReturn("unknown JCA algorithm");

        // when / then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> testSubject.publicKeyAlgorithm(certificateMock))
            .withMessage("Unknown JCA algorithm 'unknown JCA algorithm'.");
    }

    @Test
    void publicKeyAlgorithmReturnsExpectedAlgorithm(@Mock X509Certificate certificateMock, @Mock PublicKey publicKeyMock) {
        // given
        given(certificateMock.getPublicKey()).willReturn(publicKeyMock);
        given(publicKeyMock.getAlgorithm()).willReturn("RSA");

        // when
        PublicKeyAlgorithm algorithm = testSubject.publicKeyAlgorithm(certificateMock);

        // then
        assertThat(algorithm).isEqualTo(RSA);
    }

}
