package dev.mieser.tsa.web.formatter;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.math.BigInteger;
import java.util.Locale;

import org.springframework.format.Formatter;

/**
 * {@link Formatter} to represent {@link BigInteger}s as uppercase Hexadecimal Strings.
 */
public class HexFormatter implements Formatter<BigInteger> {

    private static final int HEX_RADIX = 16;

    @Override
    public BigInteger parse(String text, Locale locale) {
        if (isBlank(text)) {
            return null;
        }

        return new BigInteger(text, HEX_RADIX);
    }

    @Override
    public String print(BigInteger value, Locale locale) {
        return value.toString(HEX_RADIX).toUpperCase();
    }

}
