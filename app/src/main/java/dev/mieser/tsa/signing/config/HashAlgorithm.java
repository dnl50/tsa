package dev.mieser.tsa.signing.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All predefined hash algorithms.
 */
@Getter
@RequiredArgsConstructor
public enum HashAlgorithm {

    SHA1("1.3.14.3.2.26"),

    SHA256("2.16.840.1.101.3.4.2.1"),

    SHA512("2.16.840.1.101.3.4.2.3");

    /**
     * The unique object identifier of the hash algorithm.
     */
    private final String objectIdentifier;

}
