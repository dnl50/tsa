package dev.mieser.tsa.signing.impl.cert;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.bouncycastle.asn1.nist.NISTObjectIdentifiers.id_sha256;
import static org.mockito.BDDMockito.given;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HashBasedCertificateSelectorTest {

    private static final AlgorithmIdentifier SHA256_IDENTIFIER = new AlgorithmIdentifier(id_sha256);

    @Test
    void matchThrowsExceptionWhenDigestCalculatorCannotBeConstructed(@Mock X509CertificateHolder certificateMock) {
        // given
        var testSubject = new HashBasedCertificateSelector(new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.3")),
            new byte[0]);

        // when / then
        assertThatIllegalStateException()
            .isThrownBy(() -> testSubject.match(certificateMock))
            .withMessage("Failed to calculate hash.")
            .withCauseInstanceOf(OperatorCreationException.class);
    }

    @Test
    void matchReturnsTrueWhenDigestOfCertificateMatches(@Mock X509CertificateHolder certificateMock) throws Exception {
        // given
        byte[] encodedCert = "encoded".getBytes(UTF_8);
        byte[] actualHash = Hex.decodeHex("766adc67b02bf315b9b5057994bfe6cfbd9354c433f259b29ba415dbe0f7afa5");
        var testSubject = new HashBasedCertificateSelector(SHA256_IDENTIFIER, actualHash);

        given(certificateMock.getEncoded()).willReturn(encodedCert);

        // when
        boolean matches = testSubject.match(certificateMock);

        // then
        assertThat(matches).isTrue();
    }

    @Test
    void matchReturnsFalseWhenDigestOfCertificateDiffers(@Mock X509CertificateHolder certificateMock) throws Exception {
        // given
        byte[] encodedCert = "encoded".getBytes(UTF_8);
        byte[] sha1Hash = Hex.decodeHex("a5645bb67778c147a3b366da521477d254f5c4e8");
        var testSubject = new HashBasedCertificateSelector(SHA256_IDENTIFIER, sha1Hash);

        given(certificateMock.getEncoded()).willReturn(encodedCert);

        // when
        boolean matches = testSubject.match(certificateMock);

        // then
        assertThat(matches).isFalse();
    }

    @Test
    void cloneReturnsNewInstance() {
        // given
        var testSubject = new HashBasedCertificateSelector(SHA256_IDENTIFIER, "sha256 of cert".getBytes(UTF_8));

        // when
        HashBasedCertificateSelector clonedSelector = testSubject.clone();

        // then
        assertSoftly(softly -> {
            softly.assertThat(clonedSelector).isNotNull();
            softly.assertThat(clonedSelector).isNotSameAs(testSubject);
        });
    }

}
