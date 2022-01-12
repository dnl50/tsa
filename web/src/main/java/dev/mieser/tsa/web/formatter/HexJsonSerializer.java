package dev.mieser.tsa.web.formatter;

import java.io.IOException;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson {@link JsonSerializer} to serialize {@link BigInteger}s as uppercase Hexadecimal Strings.
 */
public class HexJsonSerializer extends StdSerializer<BigInteger> {

    private static final int HEX_RADIX = 16;

    public HexJsonSerializer() {
        super(BigInteger.class);
    }

    @Override
    public void serialize(BigInteger value, JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeString(value.toString(HEX_RADIX).toUpperCase());
        }
    }

}
