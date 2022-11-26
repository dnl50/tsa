package dev.mieser.tsa.web.validator;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Base64EncodingValidatorTest {

    private final Base64EncodingValidator testSubject = new Base64EncodingValidator();

    @Test
    void isValidReturnsTrueWhenValueIsNull(@Mock ConstraintValidatorContext contextMock) {
        // given / when
        boolean valid = testSubject.isValid(null, contextMock);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void isValidReturnsTrueWhenValueIsBase64(@Mock ConstraintValidatorContext contextMock) {
        // given
        String value = "dGVzdA==";

        // when
        boolean valid = testSubject.isValid(value, contextMock);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void isValidReturnsFalseWhenValueIsNotBase64(@Mock ConstraintValidatorContext contextMock) {
        // given
        String value = "I'm Base64, trust me, bro";

        // when
        boolean valid = testSubject.isValid(value, contextMock);

        // then
        assertThat(valid).isFalse();
    }

}
