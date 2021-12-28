package dev.mieser.tsa.signing.cert;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemCertificateLoaderTest {

    @Test
    void readsFileFromSpecifiedPath(@TempDir File tempDir) throws IOException {
        // given
        String resourcePath = "/dev/mieser/tsa/signing/cert/unprotected.p12";
        File file = copyResourceToDirectory(resourcePath, tempDir);

        var testSubject = new FileSystemCertificateLoader(file.getAbsolutePath(), "password".toCharArray());

        // when
        byte[] actualPkcs12Container = readToByteArrayAndClose(testSubject.pkcs12InputStream(file.getAbsolutePath()));

        // then
        byte[] expectedPkcs12Container = readToByteArrayAndClose(getClass().getResourceAsStream(resourcePath));

        assertThat(actualPkcs12Container).isEqualTo(expectedPkcs12Container);
    }

    private File copyResourceToDirectory(String resourcePath, File dir) throws IOException {
        File temporaryFile = Files.createTempFile(dir.toPath(), "container", ".p12").toFile();
        try (InputStream resourceInputStream = getClass().getResourceAsStream(resourcePath);
            OutputStream fos = new FileOutputStream(temporaryFile)) {
            IOUtils.copy(resourceInputStream, fos);
        }

        return temporaryFile;
    }

    private byte[] readToByteArrayAndClose(InputStream inputStream) throws IOException {
        try (inputStream) {
            return IOUtils.toByteArray(inputStream);
        }
    }

}
