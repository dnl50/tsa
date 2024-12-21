package dev.mieser.tsa.signing.impl.cert;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public record CertificateAndPrivateKey(X509Certificate certificate, PrivateKey privateKey) {

}
