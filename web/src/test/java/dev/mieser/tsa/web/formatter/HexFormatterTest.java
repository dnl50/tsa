package dev.mieser.tsa.web.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigInteger;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HexFormatterTest {

    private final HexFormatter testSubject = new HexFormatter();

    @Test
    void parseReturnsNullWhenTextIsBlank() {
        // given / when
        BigInteger value = testSubject.parse("\t", Locale.GERMAN);

        // then
        assertThat(value).isNull();
    }

    @ParameterizedTest
    @MethodSource("longToHex")
    void parseReturnsExpectedValue(long longValue, String hexValue) {
        // given / when
        BigInteger value = testSubject.parse(hexValue, Locale.CANADA);

        // then
        assertThat(value).isEqualTo(longValue);
    }

    @ParameterizedTest
    @MethodSource("longToHex")
    void printReturnsExpectedString(long longValue, String hexValue) {
        // given / when
        String printedValue = testSubject.print(BigInteger.valueOf(longValue), Locale.ENGLISH);

        // then
        assertThat(printedValue).isEqualTo(hexValue);
    }

    static Stream<Arguments> longToHex() {
        return Stream.of(
            arguments(-1337L, "-539"),
            arguments(12, "C"),
            arguments(1515, "5EB"));
    }

}
