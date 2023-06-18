package dev.mieser.tsa.persistence.impl.entity;

import java.util.Base64;

import jakarta.persistence.AttributeConverter;

/**
 * JPA {@link AttributeConverter}, which converts between binary data and its Base64 representation.
 */
public class Base64Converter implements AttributeConverter<byte[], String> {

    @Override
    public String convertToDatabaseColumn(byte[] attribute) {
        if (attribute == null) {
            return null;
        }

        return Base64.getEncoder().encodeToString(attribute);
    }

    @Override
    public byte[] convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return Base64.getDecoder().decode(dbData);
    }

}
