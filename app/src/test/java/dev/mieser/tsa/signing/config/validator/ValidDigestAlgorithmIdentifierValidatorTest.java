package dev.mieser.tsa.signing.config.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidDigestAlgorithmIdentifierValidatorTest {

    @EmptySource
    @NullSource
    @ParameterizedTest
    void blankStringsAreConsideredValid(String blankIdentifier) {
        // given
        var holder = new AlgorithmHolder(blankIdentifier);

        // when
        Set<ConstraintViolation<AlgorithmHolder>> violations = validate(holder);

        // then
        assertThat(violations).isEmpty();
    }

    @ValueSource(strings = {
        "Hello there!",
        "0.4.0.127.0.7.1.1.4.1.3" // SHA256WITHPLAIN-ECDSA (not a digest algorithm)
    })
    @ParameterizedTest
    void invalidOid(String invalidOid) {
        // given
        var holder = new AlgorithmHolder(invalidOid);

        // when
        Set<ConstraintViolation<AlgorithmHolder>> violations = validate(holder);

        // then
        assertThat(violations).singleElement()
            .extracting(ConstraintViolation::getMessage)
            .asString()
            .isEqualTo("%s is not a valid digest algorithm OID.", invalidOid);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2.16.840.1.101.3.4.2.1", // SHA256,
        "2.16.840.1.101.3.4.2.3", // SHA512
        "1.2.840.113549.2.5" // MD5
    })
    void validOid(String validOid) {
        // given
        var holder = new AlgorithmHolder(validOid);

        // when
        Set<ConstraintViolation<AlgorithmHolder>> violations = validate(holder);

        // then
        assertThat(violations).isEmpty();
    }

    private <T> Set<ConstraintViolation<T>> validate(T object) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(object);
        }
    }

    private record AlgorithmHolder(@ValidDigestAlgorithmIdentifier String identifier) {

    }

}
