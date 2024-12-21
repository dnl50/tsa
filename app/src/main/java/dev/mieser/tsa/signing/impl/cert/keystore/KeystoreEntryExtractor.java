package dev.mieser.tsa.signing.impl.cert.keystore;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;

/**
 * Interface abstraction of the {@link KeyStore#getCertificate(String)} and {@link KeyStore#getKey(String, char[])}
 * methods.
 *
 * @param <T>
 *     The type of the certificate or key to be abstracted from the {@link KeyStore}.
 */
@FunctionalInterface
interface KeystoreEntryExtractor<T> {

    T extractEntry(KeyStore keyStore,
        String alias) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException;

}
