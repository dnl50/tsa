package dev.mieser.tsa.signing.cert;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.bouncycastle.asn1.nist.NISTObjectIdentifiers.id_sha256;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class HashBasedCertificateSelectorTest {

    private static final AlgorithmIdentifier SHA256_IDENTIFIER = new AlgorithmIdentifier(id_sha256);

    private final DigestCalculatorProvider digestCalculatorProviderMock;

    HashBasedCertificateSelectorTest(@Mock DigestCalculatorProvider digestCalculatorProviderMock) {
        this.digestCalculatorProviderMock = digestCalculatorProviderMock;
    }

    @Test
    void matchThrowsExceptionWhenDigestCalculatorCannotBeConstructed(@Mock X509CertificateHolder certificateMock) throws Exception {
        // given
        var testSubject = new HashBasedCertificateSelector(SHA256_IDENTIFIER, new byte[0], digestCalculatorProviderMock);
        OperatorCreationException thrownException = new OperatorCreationException("error!!1!");

        given(digestCalculatorProviderMock.get(SHA256_IDENTIFIER)).willThrow(thrownException);

        // when / then
        assertThatIllegalStateException()
                .isThrownBy(() -> testSubject.match(certificateMock))
                .withMessage("Failed to calculate hash.")
                .withCause(thrownException);
    }

    @Test
    void matchThrowsExceptionWhenCertificateCannotBeEncoded(@Mock DigestCalculator digestCalculatorMock, @Mock X509CertificateHolder certificateMock)
            throws Exception {
        // given
        var testSubject = new HashBasedCertificateSelector(SHA256_IDENTIFIER, new byte[0], digestCalculatorProviderMock);
        IOException thrownException = new IOException("error!!1!");

        given(digestCalculatorProviderMock.get(SHA256_IDENTIFIER)).willReturn(digestCalculatorMock);
        given(certificateMock.getEncoded()).willThrow(thrownException);

        // when / then
        assertThatIllegalStateException()
                .isThrownBy(() -> testSubject.match(certificateMock))
                .withMessage("Failed to calculate hash.")
                .withCause(thrownException);
    }

    @Test
    @Disabled("problems with spying output stream")
    void matchThrowsExceptionWhenDigestOutputStreamCannotBeClosed(@Mock DigestCalculator digestCalculatorMock, @Mock X509CertificateHolder certificateMock)
            throws Exception {
        // given
        var testSubject = new HashBasedCertificateSelector(SHA256_IDENTIFIER, new byte[0], digestCalculatorProviderMock);
        IOException thrownException = new IOException("error!!1!");

        OutputStream outputStreamSpy = spy(new ByteArrayOutputStream());

        given(digestCalculatorProviderMock.get(SHA256_IDENTIFIER)).willReturn(digestCalculatorMock);
        given(digestCalculatorMock.getOutputStream()).willReturn(outputStreamSpy);
        given(certificateMock.getEncoded()).willReturn("cert".getBytes(UTF_8));
        willThrow(thrownException).given(outputStreamSpy).close();

        // when / then
        assertThatIllegalStateException()
                .isThrownBy(() -> testSubject.match(certificateMock))
                .withMessage("Failed to calculate hash.")
                .withCause(thrownException);
    }

    @Test
    void matchReturnsTrueWhenDigestOfCertificateMatches(@Mock DigestCalculator digestCalculatorMock, @Mock X509CertificateHolder certificateMock)
            throws Exception {
        // given
        byte[] hash = "sha256 of cert".getBytes(UTF_8);
        var testSubject = new HashBasedCertificateSelector(SHA256_IDENTIFIER, hash, digestCalculatorProviderMock);

        given(digestCalculatorProviderMock.get(SHA256_IDENTIFIER)).willReturn(digestCalculatorMock);
        given(digestCalculatorMock.getOutputStream()).willReturn(new ByteArrayOutputStream());
        given(certificateMock.getEncoded()).willReturn("cert".getBytes(UTF_8));
        given(digestCalculatorMock.getDigest()).willReturn(hash);

        // when
        boolean matches = testSubject.match(certificateMock);

        // then
        assertThat(matches).isTrue();
    }

    @Test
    void matchReturnsFalseWhenDigestOfCertificateDiffers(@Mock DigestCalculator digestCalculatorMock, @Mock X509CertificateHolder certificateMock)
            throws Exception {
        // given
        var testSubject = new HashBasedCertificateSelector(SHA256_IDENTIFIER, "sha256 of cert".getBytes(UTF_8), digestCalculatorProviderMock);

        given(digestCalculatorProviderMock.get(SHA256_IDENTIFIER)).willReturn(digestCalculatorMock);
        given(digestCalculatorMock.getOutputStream()).willReturn(new ByteArrayOutputStream());
        given(certificateMock.getEncoded()).willReturn("cert".getBytes(UTF_8));
        given(digestCalculatorMock.getDigest()).willReturn("other hash".getBytes(UTF_8));

        // when
        boolean matches = testSubject.match(certificateMock);

        // then
        assertThat(matches).isFalse();
    }

}
