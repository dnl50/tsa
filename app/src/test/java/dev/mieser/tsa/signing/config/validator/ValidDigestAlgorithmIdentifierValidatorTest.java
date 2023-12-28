package dev.mieser.tsa.signing.config.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;
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

    @Test
    void invalidValue() {
        // given
        String invalidValue = "Hello there!";
        var holder = new AlgorithmHolder(invalidValue);

        // when
        Set<ConstraintViolation<AlgorithmHolder>> violations = validate(holder);

        // then
        assertThat(violations).singleElement()
            .extracting(ConstraintViolation::getMessage)
            .asString()
            .isEqualTo("%s is neither an OID nor a known digest algorithm name.", invalidValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2.16.840.1.101.3.4.2.1",
        "SHA256",
        "2.16.840.1.101.3.4.2.3", // SHA512
        "SHA512",
        "1.2.840.113549.2.5", // MD5
        "MD5"
    })
    void validOidOrName(String validOid) {
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
