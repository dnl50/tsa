package dev.mieser.tsa.signing.impl.cert.keystore;

import static java.util.Collections.list;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import dev.mieser.tsa.signing.impl.cert.CertificateAndPrivateKey;

/**
 * {@link SigningKeystoreLoader} which supports PKCS#12 key stores.
 */
@Slf4j
@RequiredArgsConstructor
public class Pkcs12SigningKeystoreLoader implements SigningKeystoreLoader {

    private static final String CLASSPATH_MARKER = "classpath:";

    private static final String PKCS12_KEYSTORE_TYPE = "pkcs12";

    private final String path;

    private final char[] password;

    private final String alias;

    private CertificateAndPrivateKey loadedCertificateAndPrivateKey;

    @Override
    public CertificateAndPrivateKey loadCertificateAndPrivateKey() {
        extractCertificateAndPrivateKey();
        return loadedCertificateAndPrivateKey;
    }

    private void extractCertificateAndPrivateKey() {
        if (loadedCertificateAndPrivateKey != null) {
            return;
        }

        KeyStore keyStore = loadKeystore();
        this.loadedCertificateAndPrivateKey = new CertificateAndPrivateKey(extractCertificate(keyStore),
            extractPrivateKey(keyStore));
    }

    private KeyStore loadKeystore() {
        try (var inputStream = openStream()) {
            var keyStore = KeyStore.getInstance(PKCS12_KEYSTORE_TYPE);
            keyStore.load(inputStream, password);

            return keyStore;
        } catch (KeyStoreException e) {
            throw new IllegalStateException("PKCS#12 keystores are not supported by the JVM.", e);
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException(String.format("Failed to load PKCS#12 Keystore from '%s'.", path), e);
        }
    }

    private InputStream openStream() throws IOException {
        if (path.toLowerCase().startsWith(CLASSPATH_MARKER)) {
            String pathWithoutClasspathPrefix = StringUtils.removeStartIgnoreCase(path, CLASSPATH_MARKER);
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(pathWithoutClasspathPrefix);
            return Objects.requireNonNull(resourceStream,
                String.format("Classpath resource '%s' not found.", pathWithoutClasspathPrefix));
        } else {
            return new BufferedInputStream(new FileInputStream(path));
        }
    }

    private X509Certificate extractCertificate(KeyStore keyStore) {
        return extractEntry(keyStore, (k, a) -> (X509Certificate) k.getCertificate(a))
            .orElseThrow(() -> new IllegalStateException("The keystore entry does not contain a X.509 certificate."));
    }

    private PrivateKey extractPrivateKey(KeyStore keyStore) {
        return extractEntry(keyStore, (k, alias) -> (PrivateKey) k.getKey(alias, password))
            .orElseThrow(() -> new IllegalStateException("The keystore entry does not contain a private key."));
    }

    private <T> Optional<T> extractEntry(KeyStore keyStore, KeystoreEntryExtractor<T> entryExtractor) {
        try {
            String entryAliasToUse = StringUtils.isNotBlank(alias) ? alias : extractSingleAlias(keyStore);
            if (!keyStore.isKeyEntry(entryAliasToUse) && !keyStore.isCertificateEntry(entryAliasToUse)) {
                throw new IllegalStateException(
                    "The keystore does not contain an entry with alias '%s'.".formatted(entryAliasToUse));
            }

            log.info("Using keystore entry alias '{}'.", entryAliasToUse);
            return Optional.ofNullable(entryExtractor.extractEntry(keyStore, entryAliasToUse));
        } catch (KeyStoreException | UnrecoverableEntryException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to extract entry from key store.", e);
        }
    }

    private String extractSingleAlias(KeyStore keyStore) throws KeyStoreException {
        List<String> aliases = list(keyStore.aliases());
        if (aliases.isEmpty()) {
            throw new IllegalStateException("No entries present in PKCS#12 container.");
        } else if (aliases.size() > 1) {
            throw new IllegalStateException("Multiple entries present in PKCS#12 container. Please configure the alias to use.");
        }

        return aliases.getFirst();
    }

}
