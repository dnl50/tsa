package dev.mieser.tsa.web;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.stereotype.Service;

/**
 * Service to retrieve the current version of the application. The application version file is generated in the build
 * process.
 */
@Slf4j
@Service("applicationVersionService")
public class ApplicationVersionService {

    /**
     * The path the generated application version file is located at.
     */
    private static final String DEFAULT_VERSION_FILE_PATH = "META-INF/application-version.txt";

    /**
     * The path the application version file will be read from.
     */
    private final String versionFilePath;

    /**
     * Caches the read application version.
     */
    private final MutableObject<String> cachedVersion = new MutableObject<>(null);

    public ApplicationVersionService() {
        this(DEFAULT_VERSION_FILE_PATH);
    }

    ApplicationVersionService(String versionFilePath) {
        this.versionFilePath = versionFilePath;
    }

    /**
     * @return The current application version.
     */
    public String getApplicationVersion() {
        if (cachedVersion.getValue() == null) {
            synchronized (cachedVersion) {
                cachedVersion.setValue(readVersionFileFromManifest());
                log.debug("Successfully read application version '{}' from '{}'.", cachedVersion.getValue(), versionFilePath);
            }
        }

        return cachedVersion.getValue();
    }

    /**
     * @return The content of the application version file with leading and trailing whitespace removed.
     * @throws IllegalStateException
     *     When the application version file was not found or could not be read.
     */
    private String readVersionFileFromManifest() {
        InputStream versionFileStream = getClass().getClassLoader().getResourceAsStream(versionFilePath);
        if (versionFileStream == null) {
            throw new IllegalStateException("Application version file was not found on the classpath.");
        }

        try (versionFileStream) {
            return IOUtils.toString(versionFileStream, UTF_8).strip();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read application version file.", e);
        }
    }

}
