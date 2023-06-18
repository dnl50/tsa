package dev.mieser.tsa.persistence.impl.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class HexAttributeConverterTest {

    private final HexAttributeConverter testSubject = new HexAttributeConverter();

    @Nested
    class ToColumn {

        @Test
        void returnsNullWhenValueIsNull() {
            assertThat(testSubject.convertToDatabaseColumn(null)).isNull();
        }

        @ParameterizedTest
        @MethodSource("convertedValues")
        void returnsHexString(BigInteger attributeValue, String expected) {
            // given / when
            String result = testSubject.convertToDatabaseColumn(attributeValue);

            // then
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> convertedValues() {
            return Stream.of(
                arguments(BigInteger.ZERO, "0"),
                arguments(BigInteger.valueOf(1337L), "539"),
                arguments(BigInteger.valueOf(-10), "-A"));
        }

    }

    @Nested
    class FromColumn {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "\t" })
        void returnsNullWhenColumnIsBlank(String columnValue) {
            // given / when
            BigInteger result = testSubject.convertToEntityAttribute(columnValue);

            // then
            assertThat(result).isNull();
        }

        @ParameterizedTest
        @MethodSource("convertedValues")
        void returnsExpected(String columnValue, BigInteger expected) {
            // given / when
            BigInteger result = testSubject.convertToEntityAttribute(columnValue);

            // then
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> convertedValues() {
            return Stream.of(
                arguments("0", BigInteger.ZERO),
                arguments("-434FA", BigInteger.valueOf(-275_706L)));
        }

    }

}
