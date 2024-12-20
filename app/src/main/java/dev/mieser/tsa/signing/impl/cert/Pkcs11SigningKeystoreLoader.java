package dev.mieser.tsa.signing.impl.cert;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SigningKeystoreLoader} which loads key material from a PKCS#11 device.
 */
@Slf4j
@RequiredArgsConstructor
public class Pkcs11SigningKeystoreLoader implements SigningKeystoreLoader {

    private static final String KEYSTORE_TYPE = "PKCS11";

    private final Provider jceProvider;

    private final char[] pin;

    @Override
    public X509Certificate loadCertificate() {
        // KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE, jceProvider);
        // keyStore.load();

        return null;
    }

    @Override
    public PrivateKey loadPrivateKey() {
        return null;
    }

}
