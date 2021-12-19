package dev.mieser.tsa.signing.config;

import dev.mieser.tsa.signing.cert.ClasspathCertificateLoader;
import dev.mieser.tsa.signing.cert.FileSystemCertificateLoader;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class TsaConfigurationTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = TsaConfiguration.class)
    @TestPropertySource(properties = "tsa.certificate.path=classpath:/file.p12")
    class ClasspathCertificateLoaderConfiguration {

        private final SigningCertificateLoader signingCertificateLoader;

        @Autowired ClasspathCertificateLoaderConfiguration(SigningCertificateLoader signingCertificateLoader) {
            this.signingCertificateLoader = signingCertificateLoader;
        }

        @Test
        void isClasspathCertificateLoader() {
            // given / when / then
            assertThat(signingCertificateLoader).isInstanceOf(ClasspathCertificateLoader.class);
        }

    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = TsaConfiguration.class)
    @TestPropertySource(properties = "tsa.certificate.path=/file.p12")
    class FileSystemCertificateLoaderConfiguration {

        private final SigningCertificateLoader signingCertificateLoader;

        @Autowired FileSystemCertificateLoaderConfiguration(SigningCertificateLoader signingCertificateLoader) {
            this.signingCertificateLoader = signingCertificateLoader;
        }

        @Test
        void isFileSystemCertificateLoader() {
            // given / when / then
            assertThat(signingCertificateLoader).isInstanceOf(FileSystemCertificateLoader.class);
        }

    }

}
