package dev.mieser.tsa.signing.cert;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bouncycastle.asn1.x509.Extension.extendedKeyUsage;
import static org.bouncycastle.asn1.x509.KeyPurposeId.id_kp_serverAuth;
import static org.bouncycastle.asn1.x509.KeyPurposeId.id_kp_timeStamping;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TimeStampCertificateSelectorTest {

    private final TimeStampCertificateSelector testSubject = new TimeStampCertificateSelector();

    @Test
    void matchReturnsFalseWhenExtendedKeyUsageExtensionIsNotPresent(@Mock X509CertificateHolder x509CertificateMock) {
        // given / when
        boolean matched = testSubject.match(x509CertificateMock);

        // then
        assertThat(matched).isFalse();
    }

    @Test
    void matchReturnsFalseWhenExtendedKeyUsageExtensionIsNotCritical(@Mock X509CertificateHolder x509CertificateMock) {
        // given
        var extendedKeyUsageExtension = new Extension(extendedKeyUsage, false, new DEROctetString(new byte[0]));

        given(x509CertificateMock.getExtension(extendedKeyUsage)).willReturn(extendedKeyUsageExtension);

        // when
        boolean matched = testSubject.match(x509CertificateMock);

        // then
        assertThat(matched).isFalse();
    }

    @Test
    void matchReturnsFalseWhenExtendedKeyUsageExtensionDoesNotHaveTimeStampingPurpose(@Mock X509CertificateHolder x509CertificateMock) throws IOException {
        // given
        var keyPurposeIds = new DEROctetString(new DERSequence(new ASN1Encodable[] {id_kp_serverAuth}));
        var extendedKeyUsageExtension = new Extension(extendedKeyUsage, true, keyPurposeIds);

        given(x509CertificateMock.getExtension(extendedKeyUsage)).willReturn(extendedKeyUsageExtension);

        // when
        boolean matched = testSubject.match(x509CertificateMock);

        // then
        assertThat(matched).isFalse();
    }

    @Test
    void matchReturnsTrueWhenExtendedKeyUsageExtensionSingleTimeStampingPurpose(@Mock X509CertificateHolder x509CertificateMock) throws IOException {
        // given
        var keyPurposeIds = new DEROctetString(new DERSequence(new ASN1Encodable[] {id_kp_timeStamping}));
        var extendedKeyUsageExtension = new Extension(extendedKeyUsage, true, keyPurposeIds);

        given(x509CertificateMock.getExtension(extendedKeyUsage)).willReturn(extendedKeyUsageExtension);

        // when
        boolean matched = testSubject.match(x509CertificateMock);

        // then
        assertThat(matched).isTrue();
    }

}
