package dev.mieser.tsa.signing.config;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import dev.mieser.tsa.domain.HashAlgorithm;
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
     * The Hash Algorithms which are accepted by the Timestamp Authority. A TSP request specifying a known
     * {@link HashAlgorithm} which is not part of this set will be rejected.
     * <p/>
     * {@link HashAlgorithm#SHA256} and {@link HashAlgorithm#SHA512} are accepted by default. Cannot be empty.
     */
    @NotEmpty
    @WithDefault("SHA256,SHA512")
    Set<HashAlgorithm> acceptedHashAlgorithms();

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
         * The file system path to the PKCS#12 file containing the certificate and private key.
         */
        @NotNull
        File path();

        /**
         * The password of the PKCS#12 file containing the certificate and private key.
         * <p/>
         * No password is used by default.
         */
        Optional<String> password();

    }

}
