package dev.mieser.tsa.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static dev.mieser.tsa.domain.ResponseStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ResponseStatusTest {

    @ParameterizedTest
    @MethodSource("valuesToEnumConstants")
    void fromIntValueReturnsExpectedStatus(int value, ResponseStatus expectedStatus) {
        // given / when
        Optional<ResponseStatus> actualStatus = ResponseStatus.fromIntValue(value);

        // then
        assertThat(actualStatus).contains(expectedStatus);
    }

    @Test
    void fromIntValueReturnsEmptyOptionalWhenNoStatusIsFound() {
        // given / when
        Optional<ResponseStatus> actualStatus = ResponseStatus.fromIntValue(-1);

        // then
        assertThat(actualStatus).isEmpty();
    }

    static Stream<Arguments> valuesToEnumConstants() {
        return Stream.of(
                arguments(0, GRANTED),
                arguments(1, GRANTED_WITH_MODS),
                arguments(2, REJECTION),
                arguments(3, WAITING),
                arguments(4, REVOCATION_WARNING),
                arguments(5, REVOCATION_NOTIFICATION)
        );
    }

}
