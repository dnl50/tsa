package dev.mieser.tsa.signing.impl.cert;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import dev.mieser.tsa.signing.api.exception.InvalidCertificateException;

public class CertificateParser {

    public X509Certificate parseCertificate(InputStream x509Certificate) throws InvalidCertificateException {
        try (x509Certificate) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(x509Certificate);
        } catch (CertificateException | IOException e) {
            throw new InvalidCertificateException(e);
        }
    }

}
