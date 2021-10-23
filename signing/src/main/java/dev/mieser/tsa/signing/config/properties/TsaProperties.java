package dev.mieser.tsa.signing.config.properties;

import dev.mieser.tsa.domain.HashAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.Set;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "tsa")
public class TsaProperties {

    /**
     * The hash algorithm which is used to calculate the TSA's certificate identifier ({@code ESSCertID}).
     * <p/>
     * {@link HashAlgorithm#SHA256} is used by default. Cannot be {@code null}.
     */
    @NotNull
    private HashAlgorithm essCertIdAlgorithm = SHA256;

    /**
     * The hash algorithm which is used to calculate the TSP requests digest, which will be signed by the TSA. A JCA Provider must be registered which
     * provides an algorithm called {@code XwithY} where {@code X} is the value of this property and {@code Y} is the name of the algorithm of the public key used
     * to sign the TSP requests.
     * <p/>
     * {@link HashAlgorithm#SHA256} is used by default. Cannot be {@code null}
     */
    @NotNull
    private HashAlgorithm signingDigestAlgorithm = SHA256;

    /**
     * The Hash Algorithms which are accepted by the Timestamp Authority. A TSP request specifying a known {@link HashAlgorithm}
     * which is not part of this set will be rejected.
     * <p/>
     * All known {@link HashAlgorithm}s are accepted by default. Cannot be empty.
     */
    @NotEmpty
    private Set<HashAlgorithm> acceptedHashAlgorithms = EnumSet.allOf(HashAlgorithm.class);

    /**
     * The OID of the policy under which the TSP responses are produced.
     * <p/>
     * Default is set to OID {@code 1.2}. Cannot be empty.
     */
    @NotEmpty
    private String policyOid = "1.2";

    /**
     * Encapsulates the properties for configuring the TSA certificate.
     */
    @Valid
    private CertificateLoaderProperties certificate = new CertificateLoaderProperties();

    @Getter
    @Setter
    public static class CertificateLoaderProperties {

        /**
         * The file system path to the PKCS#12 file containing the certificate and private key. Paths beginning with {@code classpath:}
         * can be used to read PKCS#12 files from the classpath.
         * <p/>
         * Cannot be empty.
         */
        @NotEmpty
        private String path;

        /**
         * The password of the PKCS#12 file containing the certificate and private key.
         * <p/>
         * No password is used by default.
         */
        private String password;

    }

}
