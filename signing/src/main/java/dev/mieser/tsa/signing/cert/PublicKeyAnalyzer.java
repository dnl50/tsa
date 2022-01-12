package dev.mieser.tsa.signing.cert;

import static java.lang.String.format;

import java.security.cert.X509Certificate;

/**
 * Service to retrieve information from a X509 Certificate/Public Key.
 */
public class PublicKeyAnalyzer {

    /**
     * @param certificate
     *     The certificate which contains the public key to retrieve the algorithm from, not {@code null}.
     * @return The algorithm of the public key.
     * @throws IllegalArgumentException
     *     When no known {@link PublicKeyAlgorithm} exists for the specified public key.
     */
    public PublicKeyAlgorithm publicKeyAlgorithm(X509Certificate certificate) {
        String jcaName = certificate.getPublicKey().getAlgorithm();

        return PublicKeyAlgorithm.fromJcaName(jcaName)
            .orElseThrow(() -> new IllegalArgumentException(format("Unknown JCA algorithm '%s'.", jcaName)));
    }

}
