package dev.mieser.tsa.signing.impl.cert;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemReader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Pkcs12SigningKeystoreLoaderTest {

    private static final char[] NO_PASSWORD = new char[0];

    private static final char[] PASSWORD = "supersecurepassword".toCharArray();

    @TempDir
    private Path tempDir;

    @Nested
    class ClasspathResource {

        @Test
        void canLoadKeystoreFromClasspath() {
            // given
            var testSubject = new Pkcs12SigningKeystoreLoader("classpath:keystore/ec.p12", NO_PASSWORD, null);

            // when / then
            assertSoftly(softly -> {
                softly.assertThat(testSubject.loadCertificate()).isNotNull();
                softly.assertThat(testSubject.loadPrivateKey()).isNotNull();
            });
        }

        @Test
        void throwsExceptionWhenClasspathResourceWasNotFound() {
            // given
            var testSubject = new Pkcs12SigningKeystoreLoader("classpath:unknown-resource.p12", NO_PASSWORD, null);

            // when / then
            assertThatException()
                .isThrownBy(testSubject::loadCertificate)
                .withMessage("Classpath resource 'unknown-resource.p12' not found.");
        }

    }

    @Nested
    class FileSystemResource {

        @Test
        void throwsExceptionWhenKeyStoreFileNotFound() {
            // given
            var testSubject = new Pkcs12SigningKeystoreLoader(tempDir.resolve("unknown-file.p12").toAbsolutePath().toString(),
                NO_PASSWORD, null);

            // when / then
            assertThatIllegalStateException().isThrownBy(testSubject::loadCertificate)
                .withMessageMatching("Failed to load PKCS#12 Keystore from '.*unknown-file\\.p12'\\.");
        }

    }

    @Nested
    class PasswordProtection {

        @Test
        void canLoadKeystoreWhenPkcs12FileIsNotPasswordProtected() throws Exception {
            // given
            X509Certificate expectedCertificate = loadCertificate();
            PrivateKey expectedPrivateKey = loadPrivateKey();
            String tempFilePath = copyResourceToTempDirectory("unprotected.p12");

            var testSubject = new Pkcs12SigningKeystoreLoader(tempFilePath, NO_PASSWORD, null);

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
            String tempFilePath = copyResourceToTempDirectory("password-protected.p12");

            var testSubject = new Pkcs12SigningKeystoreLoader(tempFilePath, PASSWORD, null);

            // when
            X509Certificate actualCertificate = testSubject.loadCertificate();
            PrivateKey actualPrivateKey = testSubject.loadPrivateKey();

            // then
            assertSoftly(softly -> {
                softly.assertThat(actualCertificate).isEqualTo(expectedCertificate);
                softly.assertThat(actualPrivateKey).isEqualTo(expectedPrivateKey);
            });
        }

    }

    @Nested
    class Aliases {

        @Test
        void usesConfiguredAlias() throws Exception {
            X509Certificate expectedCertificate = loadCertificate();
            PrivateKey expectedPrivateKey = loadPrivateKey();
            String tempFilePath = copyResourceToTempDirectory("multiple-aliases.p12");

            var testSubject = new Pkcs12SigningKeystoreLoader(tempFilePath, NO_PASSWORD, "alias-1");

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
        void throwsExceptionWhenNoEntriesArePresent() throws Exception {
            // given
            String tempFilePath = copyResourceToTempDirectory("empty-keystore.p12");
            var testSubject = new Pkcs12SigningKeystoreLoader(tempFilePath, NO_PASSWORD, null);

            // when / then
            assertThatIllegalStateException().isThrownBy(testSubject::loadCertificate)
                .withMessage("No entries present in PKCS#12 container.");
        }

        @Test
        void throwsExceptionWhenMultipleEntriesArePresentAndAliasNotConfigured() throws Exception {
            // given
            String tempFilePath = copyResourceToTempDirectory("multiple-aliases.p12");
            var testSubject = new Pkcs12SigningKeystoreLoader(tempFilePath, NO_PASSWORD, null);

            // when / then
            assertThatIllegalStateException().isThrownBy(testSubject::loadCertificate)
                .withMessage("Multiple entries present in PKCS#12 container. Please configure the alias to use.");
        }

        @Test
        void throwsExceptionWhenConfiguredAliasNotFound() throws Exception {
            // given
            String tempFilePath = copyResourceToTempDirectory("unprotected.p12");
            var testSubject = new Pkcs12SigningKeystoreLoader(tempFilePath, NO_PASSWORD, "unknown-alias");

            // when / then
            assertThatIllegalStateException()
                .isThrownBy(testSubject::loadCertificate)
                .withMessage("The keystore does not contain an entry with alias 'unknown-alias'.");
        }

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

    private String copyResourceToTempDirectory(String resourcePath) throws IOException {
        Path tempFile = tempDir.resolve("file");
        try (var inputStream = getClass().getResourceAsStream(resourcePath);
            OutputStream tempFileOutputStream = Files.newOutputStream(tempFile)) {
            inputStream.transferTo(tempFileOutputStream);
        }

        return tempFile.toAbsolutePath().toString();
    }

}
