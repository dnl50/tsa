package dev.mieser.tsa.web.dto.datatable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SortDirectionTest {

    @Test
    void fromDirectionReturnsNullWhenDirectionIsNull() {
        // given / when / then
        assertThat(SortDirection.fromDirection(null)).isNull();
    }

    @Test
    void fromDirectionThrowsExceptionWhenNoEnumConstantIsPresent() {
        // given / when / then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> SortDirection.fromDirection("unknown"))
            .withMessage("No direction constant found for 'unknown'.");
    }

    @ParameterizedTest
    @MethodSource("directionToEnumConstant")
    void fromDirectionReturnsExpectedConstant(String direction, SortDirection expectedSortDirection) {
        // given / when
        SortDirection actualSortDirection = SortDirection.fromDirection(direction);

        // then
        assertThat(actualSortDirection).isEqualTo(expectedSortDirection);
    }

    @Test
    void fromDirectionIsCaseInsensitive() {
        // given / when / then
        assertThat(SortDirection.fromDirection("AsC")).isEqualTo(SortDirection.ASC);
    }

    static Stream<Arguments> directionToEnumConstant() {
        return Stream.of(
            arguments("asc", SortDirection.ASC),
            arguments("desc", SortDirection.DESC));
    }

}
