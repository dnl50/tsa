package dev.mieser.tsa.signing.cert;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ClasspathCertificateLoaderTest {

    @Test
    void pkcs12InputStreamReturnsInputStreamOfClasspathResource() throws IOException {
        // given
        String resourcePath = "/dev/mieser/tsa/signing/cert/unprotected.p12";

        var testSubject = new ClasspathCertificateLoader(resourcePath, "password".toCharArray());

        // when
        byte[] actualPkcs12Container = readToByteArrayAndClose(testSubject.pkcs12InputStream(resourcePath));

        // then
        byte[] expectedPkcs12Container = readToByteArrayAndClose(getClass().getResourceAsStream(resourcePath));

        assertThat(actualPkcs12Container).isEqualTo(expectedPkcs12Container);
    }

    private byte[] readToByteArrayAndClose(InputStream inputStream) throws IOException {
        try (inputStream) {
            return IOUtils.toByteArray(inputStream);
        }
    }

}
