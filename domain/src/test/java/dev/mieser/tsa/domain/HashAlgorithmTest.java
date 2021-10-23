package dev.mieser.tsa.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static dev.mieser.tsa.domain.HashAlgorithm.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class HashAlgorithmTest {

    @Test
    void fromObjectIdentifierReturnsEmptyOptionalWhenAlgorithmNotFound() {
        // given
        String oid = "unknown OID";

        // when
        Optional<HashAlgorithm> actualAlgorithm = HashAlgorithm.fromObjectIdentifier(oid);

        // then
        Assertions.assertThat(actualAlgorithm).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("oidToAlgorithmProvider")
    void fromObjectIdentifierReturnsExpectedAlgorithm(String oid, HashAlgorithm expectedAlgorithm) {
        // given / when
        Optional<HashAlgorithm> actualAlgorithm = HashAlgorithm.fromObjectIdentifier(oid);

        // then
        Assertions.assertThat(actualAlgorithm).contains(expectedAlgorithm);
    }

    static Stream<Arguments> oidToAlgorithmProvider() {
        return Stream.of(
                arguments("1.3.14.3.2.26", SHA1),
                arguments("2.16.840.1.101.3.4.2.1", SHA256),
                arguments("2.16.840.1.101.3.4.2.3", SHA512)
        );
    }

}
