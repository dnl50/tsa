package dev.mieser.tsa.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static dev.mieser.tsa.domain.FailureInfo.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FailureInfoTest {

    @Test
    void fromIntValueReturnsEmptyOptionalWhenNoConstantDefined() {
        // given
        int unknownFailureInfo = 2;

        // when
        Optional<FailureInfo> failureInfo = FailureInfo.fromIntValue(unknownFailureInfo);

        // then
        assertThat(failureInfo).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("valueToFailureInfo")
    void fromIntValueReturnsExpectedConstant() {
        // given
        int unknownFailureInfo = 2;

        // when
        Optional<FailureInfo> failureInfo = FailureInfo.fromIntValue(unknownFailureInfo);

        // then
        assertThat(failureInfo).isEmpty();
    }

    static Stream<Arguments> valueToFailureInfo() {
        return Stream.of(
                arguments(1 << 7, BAD_ALGORITHM),
                arguments(1 << 5, BAD_REQUEST),
                arguments(1 << 2, BAD_DATA_FORMAT),
                arguments(1 << 9, TIME_NOT_AVAILABLE),
                arguments(1 << 8, UNACCEPTED_POLICY),
                arguments(1 << 23, UNACCEPTED_EXTENSION),
                arguments(1 << 22, ADD_INFO_NOT_AVAILABLE),
                arguments(1 << 30, SYSTEM_FAILURE)
        );
    }

}
