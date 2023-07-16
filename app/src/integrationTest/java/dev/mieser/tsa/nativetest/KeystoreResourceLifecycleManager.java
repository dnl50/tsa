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
 * host system is mounted in the container.
 */
public class KeystoreResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Path tempFileOnHostSystem = copyKeystoreToTemporaryFile();

        return Map.of("quarkus.test.arg-line", String.format("-v %s:/work/keystore.p12", tempFileOnHostSystem.toAbsolutePath()));
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
