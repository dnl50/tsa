package dev.mieser.tsa.nativetest;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * {@link QuarkusTestResourceLifecycleManager} which copies a PKCS#12 keystore to a temporary directory on the host
 * system and adds an argument to the {@code docker run} command executed by Quarkus so that the temporary file on the
 * host system is mounted in the container.
 */
@Slf4j
public class KeystoreResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private Path temporaryFile;

    @Override
    public Map<String, String> start() {
        temporaryFile = copyKeystoreToTemporaryFile();

        return Map.of("quarkus.test.arg-line", String.format("-v %s:/work/keystore.p12", temporaryFile.toAbsolutePath()));
    }

    @Override
    public void stop() {
        if (temporaryFile == null) {
            return;
        }

        try {
            Files.delete(temporaryFile);
        } catch (Exception e) {
            log.warn("Failed to delete temporary file.", e);
        }
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
