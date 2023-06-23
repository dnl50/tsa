package dev.mieser.tsa.signing.impl.cert;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

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
    void canLoadKeystoreFromClasspath() throws Exception {
        // given
        var testSubject = new Pkcs12SigningKeystoreLoader("classpath:keystore/ec.p12", NO_PASSWORD);

        // when / then
        assertThat(testSubject.loadCertificate()).isNotNull();
    }

    @Test
    void canLoadKeystoreWhenPkcs12FileIsNotPasswordProtected() throws Exception {
        // given
        X509Certificate expectedCertificate = loadCertificate();
        PrivateKey expectedPrivateKey = loadPrivateKey();
        File tempFile = copyResourceToTempDirectory("unprotected.p12");

        var testSubject = new Pkcs12SigningKeystoreLoader(tempFile.getAbsolutePath(), NO_PASSWORD);

        // when
        X509Certificate actualCertificate = testSubject.loadCertificate();
        PrivateKey actualPrivateKey = testSubject.loadPrivateKey();

        // then
        assertSoftly(softly -> {
            softly.assertThat(actualCertificate).isEqualTo(expectedCertificate);
            softly.assertThat(actualPrivateKey).isEqualTo(expectedPrivateKey);
        });
    }

    @Test
    void canLoadKeystoreWhenPkcs12FileIsPasswordProtected() throws Exception {
        // given
        X509Certificate expectedCertificate = loadCertificate();
        PrivateKey expectedPrivateKey = loadPrivateKey();
        File tempFile = copyResourceToTempDirectory("password-protected.p12");

        var testSubject = new Pkcs12SigningKeystoreLoader(tempFile.getAbsolutePath(), PASSWORD);

        // when
        X509Certificate actualCertificate = testSubject.loadCertificate();
        PrivateKey actualPrivateKey = testSubject.loadPrivateKey();

        // then
        assertSoftly(softly -> {
            softly.assertThat(actualCertificate).isEqualTo(expectedCertificate);
            softly.assertThat(actualPrivateKey).isEqualTo(expectedPrivateKey);
        });
    }

    @Test
    void throwsExceptionWhenKeyStoreFileNotFound() {
        // given
        String keyStoreFile = "unknown-file.p12";

        var testSubject = new Pkcs12SigningKeystoreLoader(new File(tempDir, keyStoreFile).getAbsolutePath(), NO_PASSWORD);

        // when / then
        assertThatExceptionOfType(FileNotFoundException.class)
            .isThrownBy(testSubject::loadCertificate);
    }

    @Test
    void throwsExceptionWhenClasspathResourceWasNotFound() {
        // given
        var testSubject = new Pkcs12SigningKeystoreLoader("classpath:unknown-resource.p12", NO_PASSWORD);

        // when / then
        assertThatException()
            .isThrownBy(testSubject::loadCertificate)
            .withMessage("Classpath resource 'unknown-resource.p12' not found.");
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
