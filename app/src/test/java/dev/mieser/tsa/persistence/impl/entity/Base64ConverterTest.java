package dev.mieser.tsa.persistence.impl.entity;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Base64ConverterTest {

    private final Base64Converter testSubject = new Base64Converter();

    @Nested
    class ToColumn {

        @Test
        void returnsNullWhenAttributeIsNull() {
            assertThat(testSubject.convertToDatabaseColumn(null)).isNull();
        }

        @ParameterizedTest
        @MethodSource("binaryToBase64Representation")
        void returnsExpectedBase64Representation(byte[] binaryData, String expectedBase64Representation) {
            // given / when
            String result = testSubject.convertToDatabaseColumn(binaryData);

            // then
            assertThat(result).isEqualTo(expectedBase64Representation);
        }

        static Stream<Arguments> binaryToBase64Representation() {
            return Stream.of(
                arguments(new byte[0], ""),
                arguments("test".getBytes(US_ASCII), "dGVzdA=="));
        }

    }

    @Nested
    class FromColumn {

        @Test
        void returnsNullWhenColumnIsNull() {
            assertThat(testSubject.convertToEntityAttribute(null)).isNull();
        }

        @ParameterizedTest
        @MethodSource("base64ToBinaryRepresentation")
        void returnsExpectedBinaryData(String base64Representation, byte[] expectedBinaryData) {
            // given / when
            byte[] result = testSubject.convertToEntityAttribute(base64Representation);

            // then
            assertThat(result).isEqualTo(expectedBinaryData);
        }

        static Stream<Arguments> base64ToBinaryRepresentation() {
            return Stream.of(
                arguments("", new byte[0]),
                arguments("dGVzdA==", "test".getBytes(US_ASCII)));
        }

    }

}
