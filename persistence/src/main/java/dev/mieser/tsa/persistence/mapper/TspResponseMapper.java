package dev.mieser.tsa.persistence.mapper;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import java.math.BigInteger;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import dev.mieser.tsa.domain.TimestampRequestData;
import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.persistence.entity.TspRequestEntity;
import dev.mieser.tsa.persistence.entity.TspResponseEntity;

/**
 * Mapstruct {@link Mapper} to map between domain objects and entities.
 */
@Mapper
public interface TspResponseMapper {

    TimestampResponseData toDomain(TspResponseEntity entity);

    TimestampRequestData toDomain(TspRequestEntity entity);

    TspResponseEntity fromDomain(TimestampResponseData domain);

    @Mapping(target = "id", ignore = true)
    TspRequestEntity fromDomain(TimestampRequestData timestampRequestData);

    default String toBase64(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }

        return encodeBase64String(binaryData);
    }

    default byte[] fromBase64(String base64) {
        if (base64 == null) {
            return null;
        }

        return decodeBase64(base64);
    }

    default String toHexString(BigInteger bigInteger) {
        if (bigInteger == null) {
            return null;
        }

        return bigInteger.toString(16).toUpperCase();
    }

    default BigInteger fromHexString(String hex) {
        if (hex == null) {
            return null;
        }

        return new BigInteger(hex, 16);
    }

}
