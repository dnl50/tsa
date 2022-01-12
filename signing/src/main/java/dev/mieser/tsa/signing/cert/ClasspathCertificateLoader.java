package dev.mieser.tsa.signing.cert;

import java.io.InputStream;

public class ClasspathCertificateLoader extends Pkcs12SigningCertificateLoader {

    public ClasspathCertificateLoader(String path, char[] password) {
        super(path, password);
    }

    @Override
    InputStream pkcs12InputStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

}
