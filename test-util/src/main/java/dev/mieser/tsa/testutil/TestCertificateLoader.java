package dev.mieser.tsa.testutil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.bouncycastle.util.io.pem.PemReader;

/**
 * Util class for loading self-signed certificates which can be used in tests.
 */
public class TestCertificateLoader {

    private static X509Certificate rsaCertificate;

    private static RSAPrivateKey rsaPrivateKey;

    private static X509Certificate ecCertificate;

    private static ECPrivateKey ecPrivateKey;

    private static X509Certificate dsaCertificate;

    private static DSAPrivateKey dsaPrivateKey;

    private TestCertificateLoader() {
        // only static util methods
    }

    /**
     * @return A RSA certificate which has an <i>Extended Key Usage</i> Extension marked as critical which contains
     * {@code id-kp-timeStamping} as its only <i>KeyPurposeId</i>.
     * @see #loadRsaPrivateKey()
     */
    public static synchronized X509Certificate loadRsaCertificate() {
        if (rsaCertificate == null) {
            rsaCertificate = loadCertificateFromClasspath("/rsa/cert.pem");
        }

        return rsaCertificate;
    }

    /**
     * @return The corresponding RSA private key.
     * @see #loadRsaCertificate()
     */
    public static synchronized RSAPrivateKey loadRsaPrivateKey() {
        if (rsaPrivateKey == null) {
            rsaPrivateKey = loadPrivateKeyFromClasspath("/rsa/key.pem", "RSA");
        }

        return rsaPrivateKey;
    }

    /**
     * @return A DSA certificate which has an <i>Extended Key Usage</i> Extension marked as critical which contains
     * {@code id-kp-timeStamping} as its only <i>KeyPurposeId</i>.
     * @see #loadDsaPrivateKey()
     */
    public static synchronized X509Certificate loadDsaCertificate() {
        if (dsaCertificate == null) {
            dsaCertificate = loadCertificateFromClasspath("/dsa/cert.pem");
        }

        return dsaCertificate;
    }

    /**
     * @return The corresponding DSA private key.
     * @see #loadDsaCertificate()
     */
    public static synchronized DSAPrivateKey loadDsaPrivateKey() {
        if (dsaPrivateKey == null) {
            dsaPrivateKey = loadPrivateKeyFromClasspath("/dsa/key.pem", "DSA");
        }

        return dsaPrivateKey;
    }

    /**
     * @return An EC certificate which has an <i>Extended Key Usage</i> Extension marked as critical which contains
     * {@code id-kp-timeStamping} as its only <i>KeyPurposeId</i>.
     * @see #loadEcPrivateKey()
     */
    public static synchronized X509Certificate loadEcCertificate() {
        if (ecCertificate == null) {
            ecCertificate = loadCertificateFromClasspath("/ec/cert.pem");
        }

        return ecCertificate;
    }

    /**
     * @return The corresponding EC private key.
     * @see #loadEcCertificate()
     */
    public static synchronized ECPrivateKey loadEcPrivateKey() {
        if (ecPrivateKey == null) {
            ecPrivateKey = loadPrivateKeyFromClasspath("/ec/key.pem", "EC");
        }

        return ecPrivateKey;
    }

    /**
     * @param path
     *     The path to the X.509 certificate, not empty. The path should be absolute.
     * @return The X.509 certificate.
     */
    public static X509Certificate loadCertificateFromClasspath(String path) {
        return loadResourceFromClasspath(path, certificateStream -> {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            return (X509Certificate) certFactory.generateCertificate(certificateStream);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends PrivateKey> T loadPrivateKeyFromClasspath(String path, String keyAlgorithmName) {
        return loadResourceFromClasspath(path, privateKeyStream -> {
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithmName);

            try (Reader privateKeyReader = new InputStreamReader(CloseShieldInputStream.wrap(privateKeyStream))) {
                PemReader pemReader = new PemReader(privateKeyReader);
                byte[] privateKeyBytes = pemReader.readPemObject().getContent();
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                return (T) keyFactory.generatePrivate(privateKeySpec);
            }
        });
    }

    private static <T> T loadResourceFromClasspath(String path, ThrowingFunction<InputStream, T> streamConsumer) {
        try (InputStream resourceStream = TestCertificateLoader.class.getResourceAsStream(path)) {
            return streamConsumer.apply(resourceStream);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read resource from classpath.", e);
        }
    }

    /**
     * @param <I>
     *     The input of the function.
     * @param <O>
     *     The output of the function.
     */
    private interface ThrowingFunction<I, O> {

        /**
         * @param input
         *     The value to which the function should be applied.
         * @return The
         * @throws Exception
         *     When an error ocurrs while applying the function.
         */
        O apply(I input) throws Exception;

    }

}
