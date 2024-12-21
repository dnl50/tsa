package dev.mieser.tsa.signing.impl.cert.keystore;

import dev.mieser.tsa.signing.impl.cert.CertificateAndPrivateKey;

/**
 * Loads an X.509 certificate and the corresponding private key which will be used to sign TSP requests.
 */
public interface SigningKeystoreLoader {

    CertificateAndPrivateKey loadCertificateAndPrivateKey();

}
