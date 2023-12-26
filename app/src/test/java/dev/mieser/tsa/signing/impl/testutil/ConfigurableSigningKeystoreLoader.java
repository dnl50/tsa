package dev.mieser.tsa.signing.impl.testutil;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import lombok.Setter;

import dev.mieser.tsa.signing.impl.cert.SigningKeystoreLoader;
import dev.mieser.tsa.testutil.TestKeyLoader;

/**
 * {@link SigningKeystoreLoader}, which uses the {@link TestKeyLoader} to load a certificate and its corresponding
 * private key.
 *
 * @see TsaConfiguration
 */
@Setter
public class ConfigurableSigningKeystoreLoader implements SigningKeystoreLoader {

    private TsaConfiguration configuration;

    @Override
    public X509Certificate loadCertificate() {
        return switch (configuration.publicKeyAlgorithm()) {
        case DSA -> TestKeyLoader.loadDsaCertificate();
        case RSA -> TestKeyLoader.loadRsaCertificate();
        case EC -> TestKeyLoader.loadEcCertificate();
        };
    }

    @Override
    public PrivateKey loadPrivateKey() {
        return switch (configuration.publicKeyAlgorithm()) {
        case DSA -> TestKeyLoader.loadDsaPrivateKey();
        case RSA -> TestKeyLoader.loadRsaPrivateKey();
        case EC -> TestKeyLoader.loadEcPrivateKey();
        };
    }

}
