package dev.mieser.tsa.signing.cert;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.EnumSet;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of public key algorithms.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PublicKeyAlgorithm {

    /**
     * Digital Signature Algorithm
     */
    DSA("DSA"),

    /**
     * Rivest–Shamir–Adleman
     */
    RSA("RSA"),

    /**
     * Elliptic Curve
     */
    EC("EC");

    /**
     * The name of the public key algorithm as specified in the <a href=
     * "https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#keypairgenerator-algorithms">JCA
     * Specification</a>.
     */
    private final String jcaName;

    /**
     * @param jcaName
     *     The JCA name of the algorithm, not blank.
     * @return The corresponding algorithm or {@link Optional#empty()} when no algorithm with that JCA name exists.
     * @throws IllegalArgumentException
     *     When the specified JCA algorithm name is blank
     */
    public static Optional<PublicKeyAlgorithm> fromJcaName(String jcaName) {
        if (isBlank(jcaName)) {
            throw new IllegalArgumentException("JCA name cannot be blank.");
        }

        return EnumSet.allOf(PublicKeyAlgorithm.class).stream()
            .filter(algorithm -> algorithm.jcaName.equals(jcaName))
            .findFirst();
    }

}
