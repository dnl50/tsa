package dev.mieser.tsa.signing.cert;

import static java.lang.String.format;
import static java.util.Collections.list;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Loads the signing key pair from a PKCS#12 keystore.
 */
@RequiredArgsConstructor
public class Pkcs12SigningCertificateLoader implements SigningCertificateLoader {

    private static final String PKCS12_KEYSTORE_TYPE = "pkcs12";

    private final ResourceLoader resourceLoader;

    private final String path;

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
        Resource keyStoreResource = resourceLoader.getResource(path);
        if (!keyStoreResource.exists()) {
            throw new IllegalStateException(format("PKCS#12 key store not found at '%s'.", path));
        }

        try (InputStream pkcs12InputStream = keyStoreResource.getInputStream()) {
            KeyStore pkcs12Keystore = KeyStore.getInstance(PKCS12_KEYSTORE_TYPE);
            pkcs12Keystore.load(pkcs12InputStream, password);

            return pkcs12Keystore;
        } catch (KeyStoreException e) {
            throw new IllegalStateException("JVM does not comply with JVM Spec since PKCS#12 keystores are not supported.", e);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalStateException("Cannot load PKCS#12 container.", e);
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
