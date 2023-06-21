package dev.mieser.tsa.quarkus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * {@link QuarkusTestResourceLifecycleManager}, which copies a PKCS#12 Keystore to a temporary directory.
 */
public class SigningCertificateResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private static final String KEYSTORE_FILE_NAME = "ec.p12";

    @Override
    public Map<String, String> start() {
        try (InputStream pkcs12Keystore = new BufferedInputStream(getClass().getResourceAsStream(KEYSTORE_FILE_NAME))) {
            Path temporaryKeystoreFile = Files.createTempDirectory("tsa").resolve(KEYSTORE_FILE_NAME);
            FileUtils.copyInputStreamToFile(pkcs12Keystore, temporaryKeystoreFile.toFile());

            return Map.of("tsa.certificate.path", temporaryKeystoreFile.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy keystore to temporary directory", e);
        }
    }

    @Override
    public void stop() {
        // the temporary directory is automatically deleted on JVM shutdown
    }

}
