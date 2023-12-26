package dev.mieser.tsa.rest.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import dev.mieser.tsa.persistence.api.Sort;
import dev.mieser.tsa.persistence.api.SortDirection;

class SortQueryParamConverterTest {

    private final SortQueryParamConverter testSubject = new SortQueryParamConverter();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "\t" })
    void returnsNullWhenValueIsBlank(String value) {
        // given / when
        Sort result = testSubject.fromString(value);

        // then
        assertThat(result).isNull();
    }

    @Test
    void throwsExceptionWhenValueDoesNotMatchPattern() {
        // given
        var wronglyFormattedPattern = "im-wrongly-formatted";

        // when // then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> testSubject.fromString(wronglyFormattedPattern))
            .withMessage("The value is not properly formatted.");
    }

    @ParameterizedTest
    @MethodSource("formattedSorts")
    void fromStringReturnsExpectedSort(String value, Sort expected) {
        // given / when
        Sort result = testSubject.fromString(value);

        // then
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> formattedSorts() {
        return Stream.of(
            arguments("field,asc", new Sort(SortDirection.ASC, "field")),
            arguments("field.child,desc", new Sort(SortDirection.DESC, "field.child")),
            arguments("test,AsC", new Sort(SortDirection.ASC, "test")));
    }

}
