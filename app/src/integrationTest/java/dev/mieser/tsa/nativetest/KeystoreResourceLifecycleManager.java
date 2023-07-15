package dev.mieser.tsa.nativetest;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * {@link QuarkusTestResourceLifecycleManager} which copies a PKCS#12 keystore to a temporary directory on the host
 * system and adds an argument to the {@code docker run} command executed by Quarkus so that the temporary file on the
 * host system is mounted in the container. The {@code tsa.keystore.path} config property is configured to point to the
 * mounted file.
 */
public class KeystoreResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private static final String CONTAINER_MOUNT_PATH = "/tmp/keystore.p12";

    @Override
    public Map<String, String> start() {
        Path tempFileOnHostSystem = copyKeystoreToTemporaryFile();

        return Map.of(
            "quarkus.test.arg-line", String.format("-v %s:%s", tempFileOnHostSystem.toAbsolutePath(), CONTAINER_MOUNT_PATH),
            "tsa.keystore.path", CONTAINER_MOUNT_PATH);
    }

    @Override
    public void stop() {
        // the temp file is automatically deleted on JVM shutdown
    }

    private Path copyKeystoreToTemporaryFile() {
        try (var keystore = getClass().getResourceAsStream("/keystore/ec.p12")) {
            Path tempFile = Files.createTempFile("keystore", ".p12");
            Files.copy(keystore, tempFile, REPLACE_EXISTING);

            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy keystore to temporary file.", e);
        }
    }

}
