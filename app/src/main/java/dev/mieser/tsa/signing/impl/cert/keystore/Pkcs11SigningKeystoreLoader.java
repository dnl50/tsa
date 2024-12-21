package dev.mieser.tsa.signing.impl.cert.keystore;

import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.mieser.tsa.signing.impl.cert.CertificateAndPrivateKey;

/**
 * {@link SigningKeystoreLoader} which loads key material from a PKCS#11 token.
 */
@Slf4j
@RequiredArgsConstructor
public class Pkcs11SigningKeystoreLoader implements SigningKeystoreLoader {

    private static final String KEYSTORE_TYPE = "pkcs11";

    private final Provider pkcs11JceProvider;

    private final char[] pin;

    // TODO: refactor + unify with Pkcs12 logic
    @Override
    public CertificateAndPrivateKey loadCertificateAndPrivateKey() {
        KeyStore keyStore = loadKeystoreFromToken();
        List<String> keyIdentifiers = extractKeyIdentifiers(keyStore);
        if (keyIdentifiers.isEmpty()) {
            throw new IllegalStateException("The configured PKCS#11 token contains no certificates.");
        } else if (keyIdentifiers.size() > 1) {
            throw new IllegalStateException("The configured PKCS#11 token contains than one certificate: " + keyIdentifiers);
        }

        String keyIdentifierToUse = keyIdentifiers.getFirst();
        try {
            Key key = keyStore.getKey(keyIdentifierToUse, pin);
            if (!(key instanceof PrivateKey)) {
                throw new IllegalStateException("Key is not a private key!");
            }

            Certificate certificate = keyStore.getCertificate(keyIdentifierToUse);
            if (!(certificate instanceof X509Certificate)) {
                throw new IllegalStateException("Certificate is not a X509 certificate!");
            }

            return new CertificateAndPrivateKey((X509Certificate) certificate, (PrivateKey) key);
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyStore loadKeystoreFromToken() {
        try {
            KeyStore pkcs11Keystore = KeyStore.getInstance(KEYSTORE_TYPE, pkcs11JceProvider);
            pkcs11Keystore.load(null, pin);

            return pkcs11Keystore;
        } catch (KeyStoreException e) {
            throw new IllegalStateException("The pseudo-mechanism of the SunPKCS11 provider to read keystores (PCKM_KEYSTORE) " +
                "is not enabled. Please add it to the list of enabled mechanisms.");
        } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to query certificates and private keys from PKCS#11 token.", e);
        }
    }

    private List<String> extractKeyIdentifiers(KeyStore keyStore) {
        try {
            return Collections.list(keyStore.aliases());
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Failed to query certificates from PKCS#11 token.", e);
        }
    }

}
