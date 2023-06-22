package dev.mieser.tsa.signing.impl.cert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Pkcs12SigningKeystoreLoaderTest {

    private static final char[] NO_PASSWORD = new char[0];

    private static final char[] PASSWORD = "supersecurepassword".toCharArray();

    @TempDir
    private File tempDir;

    @Test
    void canLoadCertificateWhenPkcs12FileIsNotPasswordProtected() throws Exception {
        // given
        X509Certificate expectedCertificate = loadCertificate();
        File tempFile = copyResourceToTempDirectory("unprotected.p12");

        var testSubject = new Pkcs12SigningKeystoreLoader(tempFile, NO_PASSWORD);

        // when
        X509Certificate actualCertificate = testSubject.loadCertificate();

        // then
        assertThat(actualCertificate).isEqualTo(expectedCertificate);
    }

    @Test
    void canLoadPrivateKeyWhenPkcs12FileIsNotPasswordProtected() throws Exception {
        // given
        PrivateKey expectedPrivateKey = loadPrivateKey();
        File tempFile = copyResourceToTempDirectory("unprotected.p12");

        var testSubject = new Pkcs12SigningKeystoreLoader(tempFile, NO_PASSWORD);

        // when
        PrivateKey actualCertificate = testSubject.loadPrivateKey();

        // then
        assertThat(actualCertificate).isEqualTo(expectedPrivateKey);
    }

    @Test
    void canLoadCertificateWhenPkcs12FileIsPasswordProtected() throws Exception {
        // given
        X509Certificate expectedCertificate = loadCertificate();
        File tempFile = copyResourceToTempDirectory("password-protected.p12");

        var testSubject = new Pkcs12SigningKeystoreLoader(tempFile, PASSWORD);

        // when
        X509Certificate actualCertificate = testSubject.loadCertificate();

        // then
        assertThat(actualCertificate).isEqualTo(expectedCertificate);
    }

    @Test
    void canLoadPrivateKeyWhenPkcs12FileIsPasswordProtected() throws Exception {
        /// given
        PrivateKey expectedPrivateKey = loadPrivateKey();
        File tempFile = copyResourceToTempDirectory("password-protected.p12");

        var testSubject = new Pkcs12SigningKeystoreLoader(tempFile, PASSWORD);

        // when
        PrivateKey actualCertificate = testSubject.loadPrivateKey();

        // then
        assertThat(actualCertificate).isEqualTo(expectedPrivateKey);
    }

    @Test
    void throwsExceptionWhenKeyStoreNotFound() {
        // given
        String keyStoreFile = "unknown-file.p12";

        var testSubject = new Pkcs12SigningKeystoreLoader(new File(tempDir, keyStoreFile), NO_PASSWORD);

        // when / then
        assertThatExceptionOfType(FileNotFoundException.class)
            .isThrownBy(testSubject::loadCertificate);
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

    private File copyResourceToTempDirectory(String resourcePath) throws IOException {
        var tempFile = new File(tempDir, "file");
        try (var inputStream = getClass().getResourceAsStream(resourcePath);) {
            FileUtils.copyInputStreamToFile(inputStream, tempFile);
        }

        return tempFile;
    }

}
