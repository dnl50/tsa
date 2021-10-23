package dev.mieser.tsa.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Optional;

/**
 * All supported hash algorithms.
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

    /**
     * @param oid The object identifier (OID) of the algorithm.
     * @return An {@link Optional} containing the hash algorithm with the specified OID or {@link Optional#empty()},
     * if no such algorithm is defined.
     */
    public static Optional<HashAlgorithm> fromObjectIdentifier(String oid) {
        return EnumSet.allOf(HashAlgorithm.class).stream()
                .filter(hashAlgorithm -> hashAlgorithm.objectIdentifier.equals(oid))
                .findFirst();
    }

}
