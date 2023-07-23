package dev.mieser.tsa.signing.config;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import dev.mieser.tsa.signing.config.validator.ValidDigestAlgorithmIdentifier;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "tsa")
public interface TsaProperties {

    /**
     * The hash algorithm which is used to calculate the TSA's certificate identifier ({@code ESSCertIDv2}).
     * <p/>
     * {@link HashAlgorithm#SHA256} is used by default. Cannot be {@code null}.
     */
    @NotNull
    @WithDefault("SHA256")
    HashAlgorithm essCertIdAlgorithm();

    /**
     * The hash algorithm which is used to calculate the TSP requests digest, which will be signed by the TSA. A JCA
     * Provider must be registered which provides an algorithm called {@code XwithY} where {@code X} is the value of this
     * property and {@code Y} is the name of the algorithm of the public key used to sign the TSP requests.
     * <p/>
     * {@link HashAlgorithm#SHA256} is used by default. Cannot be {@code null}
     */
    @NotNull
    @WithDefault("SHA256")
    HashAlgorithm signingDigestAlgorithm();

    /**
     * The OIDs of the Hash Algorithms which are accepted by the Timestamp Authority.
     * <p/>
     * SHA256 ({@code 2.16.840.1.101.3.4.2.1} and SHA512 ({@code 2.16.840.1.101.3.4.2.3}) are accepted by default. Cannot be
     * empty.
     */
    @NotEmpty
    @WithDefault("2.16.840.1.101.3.4.2.1,2.16.840.1.101.3.4.2.3")
    Set<@NotEmpty @ValidDigestAlgorithmIdentifier String> acceptedHashAlgorithms();

    /**
     * The OID of the policy under which the TSP responses are produced.
     * <p/>
     * Default is set to OID {@code 1.2}. Cannot be empty.
     */
    @NotEmpty
    @WithDefault("1.2")
    String policyOid();

    /**
     * Encapsulates the properties for configuring the TSA keystore.
     */
    KeystoreLoaderProperties keystore();

    interface KeystoreLoaderProperties {

        /**
         * The path to the PKCS#12 file containing the certificate and private key. When the path begins with {@code classpath:}
         * the keystore is read from the classpath. Loading a keystore from the classpath is a convenience feature for
         * development purposes and will not work in a GraalVM native image.
         */
        @NotEmpty
        String path();

        /**
         * The password of the PKCS#12 file containing the certificate and private key.
         * <p/>
         * No password is used by default.
         */
        Optional<String> password();

    }

}
