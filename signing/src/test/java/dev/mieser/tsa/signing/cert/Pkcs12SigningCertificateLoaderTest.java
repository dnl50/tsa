package dev.mieser.tsa.signing.cert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemReader;
import org.junit.jupiter.api.Test;

class Pkcs12SigningCertificateLoaderTest {

    private static final char[] NO_PASSWORD = new char[0];

    private static final char[] PASSWORD = "supersecurepassword".toCharArray();

    @Test
    void canLoadCertificateWhenPkcs12FileIsNotPasswordProtected() throws Exception {
        // given
        X509Certificate expectedCertificate = loadCertificate();

        var testSubject = new Pkcs12SigningCertificateLoaderImpl("unprotected.p12", NO_PASSWORD);

        // when
        X509Certificate actualCertificate = testSubject.loadCertificate();

        // then
        assertThat(actualCertificate).isEqualTo(expectedCertificate);
    }

    @Test
    void canLoadPrivateKeyWhenPkcs12FileIsNotPasswordProtected() throws Exception {
        // given
        PrivateKey expectedPrivateKey = loadPrivateKey();

        var testSubject = new Pkcs12SigningCertificateLoaderImpl("unprotected.p12", NO_PASSWORD);

        // when
        PrivateKey actualCertificate = testSubject.loadPrivateKey();

        // then
        assertThat(actualCertificate).isEqualTo(expectedPrivateKey);
    }

    @Test
    void canLoadCertificateWhenPkcs12FileIsPasswordProtected() throws Exception {
        // given
        X509Certificate expectedCertificate = loadCertificate();

        var testSubject = new Pkcs12SigningCertificateLoaderImpl("password-protected.p12", PASSWORD);

        // when
        X509Certificate actualCertificate = testSubject.loadCertificate();

        // then
        assertThat(actualCertificate).isEqualTo(expectedCertificate);
    }

    @Test
    void canLoadPrivateKeyWhenPkcs12FileIsPasswordProtected() throws Exception {
        /// given
        PrivateKey expectedPrivateKey = loadPrivateKey();

        var testSubject = new Pkcs12SigningCertificateLoaderImpl("password-protected.p12", PASSWORD);

        // when
        PrivateKey actualCertificate = testSubject.loadPrivateKey();

        // then
        assertThat(actualCertificate).isEqualTo(expectedPrivateKey);
    }

    @Test
    void throwsExceptionWhenKeyStoreNotPresent() {
        // given
        var testSubject = new Pkcs12SigningCertificateLoaderImpl("/unknown-file.p12", NO_PASSWORD);

        // when / then
        assertThatIllegalStateException()
            .isThrownBy(testSubject::loadCertificate)
            .withMessage("PKCS#12 key store not found at '/unknown-file.p12'.");
    }

    private PrivateKey loadPrivateKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        try (Reader privateKeyReader = new InputStreamReader(getClass().getResourceAsStream("x509/key.pem"))) {
            PemReader pemReader = new PemReader(privateKeyReader);
            byte[] privateKeyBytes = pemReader.readPemObject().getContent();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        }
    }

    private X509Certificate loadCertificate() throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        try (InputStream certificateInputStream = getClass().getResourceAsStream("x509/cert.pem")) {
            return (X509Certificate) certFactory.generateCertificate(certificateInputStream);
        }
    }

    private static class Pkcs12SigningCertificateLoaderImpl extends Pkcs12SigningCertificateLoader {

        private Pkcs12SigningCertificateLoaderImpl(String path, char[] password) {
            super(path, password);
        }

        @Override
        InputStream pkcs12InputStream(String path) {
            return getClass().getResourceAsStream(path);
        }

    }

}
