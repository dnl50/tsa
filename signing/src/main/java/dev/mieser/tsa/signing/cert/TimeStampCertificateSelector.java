package dev.mieser.tsa.signing.cert;

import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.Selector;

/**
 * {@link Selector} which selects {@link X509CertificateHolder X509 Certificates} which have a
 * critical <i>Extended Key Usage</i> Extension which contains {@code id-kp-timeStamping} as its only <i>KeyPurposeId</i>.
 */
public class TimeStampCertificateSelector implements Selector<X509CertificateHolder> {

    @Override
    public boolean match(X509CertificateHolder obj) {
        Extension extendedKeyUsageExtension = obj.getExtension(Extension.extendedKeyUsage);
        if (extendedKeyUsageExtension == null || !extendedKeyUsageExtension.isCritical()) {
            return false;
        }

        ExtendedKeyUsage extendedKeyUsage = ExtendedKeyUsage.getInstance(extendedKeyUsageExtension.getParsedValue());
        return extendedKeyUsage.hasKeyPurposeId(KeyPurposeId.id_kp_timeStamping);
    }

    @Override
    public Object clone() {
        return new TimeStampCertificateSelector();
    }

}
