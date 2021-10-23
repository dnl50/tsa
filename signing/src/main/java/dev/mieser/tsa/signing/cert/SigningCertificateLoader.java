package dev.mieser.tsa.signing.cert;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public interface SigningCertificateLoader {

    X509Certificate loadCertificate() throws IOException;

    PrivateKey loadPrivateKey() throws IOException;

}
