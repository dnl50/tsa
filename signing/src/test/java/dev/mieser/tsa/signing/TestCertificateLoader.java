package dev.mieser.tsa.signing;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class TestCertificateLoader {

    private static X509Certificate rsaCertificate;

    private static RSAPrivateKey rsaPrivateKey;

    private TestCertificateLoader() {
        // only static util methods
    }

    public static X509Certificate loadRsaCertificate() {
        if (rsaCertificate == null) {
            rsaCertificate = loadResourceFromClassPath("tsa-cert/cert-rsa.pem", certificateStream -> {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

                return (X509Certificate) certFactory.generateCertificate(certificateStream);
            });
        }

        return rsaCertificate;
    }

    public static PrivateKey loadRsaPrivateKey() {
        if (rsaPrivateKey == null) {
            rsaPrivateKey = loadResourceFromClassPath("tsa-cert/key-rsa.pem", certificateStream -> {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                try (Reader privateKeyReader = new InputStreamReader(CloseShieldInputStream.wrap(certificateStream))) {
                    PemReader pemReader = new PemReader(privateKeyReader);
                    byte[] privateKeyBytes = pemReader.readPemObject().getContent();
                    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                    return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
                }
            });
        }

        return rsaPrivateKey;
    }

    private static <T> T loadResourceFromClassPath(String path, ThrowingFunction<InputStream, T> streamConsumer) {
        try (InputStream resourceStream = TestCertificateLoader.class.getResourceAsStream(path)) {
            return streamConsumer.apply(resourceStream);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read resource from classpath.", e);
        }
    }

    /**
     * @param <I> The input of the function.
     * @param <O> The output of the function.
     */
    private interface ThrowingFunction<I, O> {

        /**
         * @param input The value to which the function should be applied.
         * @return The
         * @throws Exception When an error ocurrs while applying the function.
         */
        O apply(I input) throws Exception;

    }

}
