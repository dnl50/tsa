package dev.mieser.tsa.persistence.impl.entity;

import java.math.BigInteger;

import jakarta.persistence.AttributeConverter;

import org.apache.commons.lang3.StringUtils;

/**
 * JPA {@link AttributeConverter} to convert {@link BigInteger}s to hexadecimal Strings.
 */
public class HexAttributeConverter implements AttributeConverter<BigInteger, String> {

    private static final int HEX_RADIX = 16;

    @Override
    public String convertToDatabaseColumn(BigInteger attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.toString(HEX_RADIX).toUpperCase();
    }

    @Override
    public BigInteger convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return null;
        }

        return new BigInteger(dbData, HEX_RADIX);
    }

}
