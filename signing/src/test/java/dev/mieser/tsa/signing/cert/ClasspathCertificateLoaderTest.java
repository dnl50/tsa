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
        var testSubject = new ClasspathCertificateLoader("dev/mieser/tsa/signing/cert/unprotected.p12", "password".toCharArray());

        // when
        byte[] actualPkcs12Container = readToByteArrayAndClose(testSubject.pkcs12InputStream("dev/mieser/tsa/signing/cert/unprotected.p12"));

        // then
        byte[] expectedPkcs12Container = readToByteArrayAndClose(getClass().getResourceAsStream("unprotected.p12"));

        assertThat(actualPkcs12Container).isEqualTo(expectedPkcs12Container);
    }

    private byte[] readToByteArrayAndClose(InputStream inputStream) throws IOException {
        try (inputStream) {
            return IOUtils.toByteArray(inputStream);
        }
    }

}
