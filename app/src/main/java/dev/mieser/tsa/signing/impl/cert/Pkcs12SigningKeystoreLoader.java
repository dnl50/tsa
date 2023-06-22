package dev.mieser.tsa.signing.impl.cert;

import static java.util.Collections.list;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import lombok.RequiredArgsConstructor;

/**
 * {@link SigningKeystoreLoader} which supports PKCS#12 key stores.
 */
@RequiredArgsConstructor
public class Pkcs12SigningKeystoreLoader implements SigningKeystoreLoader {

    private static final String PKCS12_KEYSTORE_TYPE = "pkcs12";

    private final File path;

    private final char[] password;

    private X509Certificate certificate;

    private PrivateKey privateKey;

    @Override
    public X509Certificate loadCertificate() throws IOException {
        extractCertificateAndPrivateKey();

        return certificate;
    }

    @Override
    public PrivateKey loadPrivateKey() throws IOException {
        extractCertificateAndPrivateKey();

        return privateKey;
    }

    private void extractCertificateAndPrivateKey() throws IOException {
        if (certificate != null && privateKey != null) {
            return;
        }

        KeyStore keyStore = loadKeystore();
        this.certificate = extractCertificate(keyStore);
        this.privateKey = extractPrivateKey(keyStore);
    }

    private KeyStore loadKeystore() throws IOException {
        try (var inputStream = new BufferedInputStream(new FileInputStream(path))) {
            var keyStore = KeyStore.getInstance(PKCS12_KEYSTORE_TYPE);
            keyStore.load(inputStream, password);

            return keyStore;
        } catch (KeyStoreException e) {
            throw new IllegalStateException("PKCS#12 keystores are not supported by the JVM.", e);
        } catch (CertificateException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(String.format("Failed to load PKCS#12 Keystore from '%s'.", path.getAbsolutePath()),
                e);
        }
    }

    private X509Certificate extractCertificate(KeyStore keyStore) {
        return extractEntry(keyStore, (k, a) -> (X509Certificate) k.getCertificate(a));
    }

    private PrivateKey extractPrivateKey(KeyStore keyStore) {
        return extractEntry(keyStore, (k, a) -> (PrivateKey) k.getKey(a, password));
    }

    private <T> T extractEntry(KeyStore keyStore, KeystoreEntryExtractor<T> entryExtractor) {
        try {
            String keyAlias = extractSingleKeyAlias(keyStore);
            return entryExtractor.extractEntry(keyStore, keyAlias);
        } catch (KeyStoreException | UnrecoverableEntryException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot extract entry from key store.", e);
        }
    }

    private String extractSingleKeyAlias(KeyStore keyStore) throws KeyStoreException {
        String firstKeyAlias = null;

        for (String alias : list(keyStore.aliases())) {
            if (!keyStore.isKeyEntry(alias)) {
                continue;
            } else if (firstKeyAlias != null) {
                throw new IllegalStateException("Multiple key entries present in PKCS#12 container.");
            }

            firstKeyAlias = alias;
        }

        if (firstKeyAlias == null) {
            throw new IllegalStateException("No key entry present in PKCS#12 container.");
        }

        return firstKeyAlias;
    }

    /**
     * Interface abstraction of the {@link KeyStore#getCertificate(String)} and {@link KeyStore#getKey(String, char[])}
     * methods.
     *
     * @param <T>
     *     The type of the certificate or key to be abstracted from the {@link KeyStore}.
     */
    @FunctionalInterface
    private interface KeystoreEntryExtractor<T> {

        T extractEntry(KeyStore keyStore,
            String alias) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException;

    }

}
