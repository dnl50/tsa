package dev.mieser.tsa.web.validator;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Base64EncodingValidatorIntegrationTest {

    private Locale defaultLocale;

    @BeforeEach
    void setUp() {
        this.defaultLocale = Locale.getDefault();
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    void hasExpectedEnglishValidationMessage() {
        // given / when / then
        assertValidationMessage(Locale.ENGLISH, "Not a valid Base64 String.");
    }

    @Test
    void hasExpectedGermanValidationMessage() {
        // given / when / then
        assertValidationMessage(Locale.GERMAN, "Es handelt sich nicht um einen validen Base64 String.");
    }

    private void assertValidationMessage(Locale locale, String expectedMessage) {
        // given
        Locale.setDefault(locale);

        String value = "I'm Base64 (maybe)!";
        var validationTarget = new ValidationTarget(value);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        // when
        Set<ConstraintViolation<ValidationTarget>> violations = validator.validate(validationTarget);

        assertSoftly(softly -> {
            softly.assertThat(violations).hasSize(1);
            softly.assertThat(violations).first().extracting(ConstraintViolation::getMessage).isEqualTo(expectedMessage);
        });
    }

    private record ValidationTarget(@Base64Encoded String value) {

    }

}
