package dev.mieser.tsa.signing.cert;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileSystemCertificateLoader extends Pkcs12SigningCertificateLoader {

    public FileSystemCertificateLoader(String path, char[] password) {
        super(path, password);
    }

    @Override
    InputStream pkcs12InputStream(String path) throws IOException {
        return new FileInputStream(path);
    }

}
