package dev.mieser.tsa.web.formatter;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.then;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

@ExtendWith(MockitoExtension.class)
class HexJsonSerializerTest {

    private final HexJsonSerializer testSubject = new HexJsonSerializer();

    @Test
    void serializeWritesNullWhenValueIsNull(@Mock JsonGenerator jsonGeneratorMock,
        @Mock SerializerProvider serializerProviderMock) throws Exception {
        // given / when
        testSubject.serialize(null, jsonGeneratorMock, serializerProviderMock);

        // then
        then(jsonGeneratorMock).should().writeNull();
    }

    @ParameterizedTest
    @MethodSource("longToHex")
    void serializeWritesUppercaseHexStringWhenValueIsNotNull(long longValue, String hexValue,
        @Mock JsonGenerator jsonGeneratorMock, @Mock SerializerProvider serializerProviderMock) throws Exception {
        // given / when
        testSubject.serialize(BigInteger.valueOf(longValue), jsonGeneratorMock, serializerProviderMock);

        // then
        then(jsonGeneratorMock).should().writeString(hexValue);
    }

    static Stream<Arguments> longToHex() {
        return Stream.of(
            arguments(-1337L, "-539"),
            arguments(12, "C"),
            arguments(1515, "5EB"));
    }

}
