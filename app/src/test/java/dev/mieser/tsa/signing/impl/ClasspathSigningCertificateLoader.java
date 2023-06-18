package dev.mieser.tsa.signing.impl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import lombok.RequiredArgsConstructor;

import dev.mieser.tsa.signing.impl.cert.PublicKeyAlgorithm;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateLoader;
import dev.mieser.tsa.testutil.TestKeyLoader;

@RequiredArgsConstructor
public class ClasspathSigningCertificateLoader implements SigningCertificateLoader {

    private final PublicKeyAlgorithm publicKeyAlgorithm;

    @Override
    public X509Certificate loadCertificate() {
        return switch (publicKeyAlgorithm) {
        case DSA -> TestKeyLoader.loadDsaCertificate();
        case RSA -> TestKeyLoader.loadRsaCertificate();
        case EC -> TestKeyLoader.loadEcCertificate();
        };
    }

    @Override
    public PrivateKey loadPrivateKey() {
        return switch (publicKeyAlgorithm) {
        case DSA -> TestKeyLoader.loadDsaPrivateKey();
        case RSA -> TestKeyLoader.loadRsaPrivateKey();
        case EC -> TestKeyLoader.loadEcPrivateKey();
        };
    }

}
