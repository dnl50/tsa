package dev.mieser.tsa.web.formatter;

import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.springframework.format.Formatter;

/**
 * {@link Formatter} to encode/decode byte arrays to/from Base64 Strings.
 */
public class Base64Formatter implements Formatter<byte[]> {

    @Override
    public byte[] parse(String text, Locale locale) {
        if (!Base64.isBase64(text)) {
            throw new IllegalArgumentException("Not a valid Base64 string.");
        }

        return Base64.decodeBase64(text);
    }

    @Override
    public String print(byte[] array, Locale locale) {
        return Base64.encodeBase64String(array);
    }

}
