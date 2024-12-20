package dev.mieser.tsa.signing.impl.cert;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Loads an X.509 certificate and the corresponding private key which will be used to sign TSP requests.
 */
public interface SigningKeystoreLoader {

    /**
     * @return The X.509 certificate which should be used to sign the TSP requests.
     */
    X509Certificate loadCertificate();

    /**
     * @return The corresponding private key.
     */
    PrivateKey loadPrivateKey();

}
