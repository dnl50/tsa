package dev.mieser.tsa.signing.impl.cert;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;

/**
 * Encapsulates information about the signing certificate as well as the certificate itself, in case it is present in
 * the time stamp token.
 */
@Data
@AllArgsConstructor
public class SigningCertificateHolder {

    /**
     * The hash algorithm identifier, not {@code null}.
     */
    private final AlgorithmIdentifier algorithmIdentifier;

    /**
     * The hash value of the signing certificate, not {@code null}.
     */
    private final byte[] hash;

    /**
     * The certificate which was used to sign the response with. May be null.
     */
    private final X509CertificateHolder signingCertificate;

}
