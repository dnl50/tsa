package dev.mieser.tsa.persistence.mapper;

import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.domain.TimestampRequestData;
import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.persistence.entity.TspRequestEntity;
import dev.mieser.tsa.persistence.entity.TspResponseEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class TspResponseMapperTest {

    private final TspResponseMapper testSubject = Mappers.getMapper(TspResponseMapper.class);

    @Test
    void toDomainReturnsExpectedDomainObject() {
        // given
        ZonedDateTime generationTime = ZonedDateTime.parse("2021-11-13T19:02:08+01:00");

        TspRequestEntity requestEntity = TspRequestEntity.builder()
                .hashAlgorithm(HashAlgorithm.SHA256)
                .hash("aGFzaA==")
                .nonce("1337")
                .certificateRequested(true)
                .tsaPolicyId("1.3.6")
                .asnEncoded("cmVx")
                .build();

        TspResponseEntity responseEntity = TspResponseEntity.builder()
                .status(ResponseStatus.REJECTION)
                .statusString("test")
                .failureInfo(12)
                .generationTime(generationTime)
                .receptionTime(generationTime)
                .serialNumber(5749L)
                .request(requestEntity)
                .asnEncoded("cmVz")
                .build();

        // when
        TimestampResponseData actualResponseData = testSubject.toDomain(responseEntity);

        // then
        TimestampRequestData expectedRequestData = TimestampRequestData.builder()
                .hashAlgorithm(HashAlgorithm.SHA256)
                .hash("hash".getBytes(UTF_8))
                .nonce(BigInteger.valueOf(4919L))
                .certificateRequested(true)
                .tsaPolicyId("1.3.6")
                .asnEncoded("req".getBytes(UTF_8))
                .build();

        TimestampResponseData expectedResponseData = TimestampResponseData.builder()
                .status(ResponseStatus.REJECTION)
                .statusString("test")
                .failureInfo(12)
                .generationTime(generationTime)
                .receptionTime(generationTime)
                .serialNumber(5749L)
                .request(expectedRequestData)
                .asnEncoded("res".getBytes(UTF_8))
                .build();

        assertThat(actualResponseData).isEqualTo(expectedResponseData);
    }

    @Test
    void fromDomainReturnsExpectedEntity() {
        // given
        ZonedDateTime generationTime = ZonedDateTime.parse("2021-11-13T19:09:27+01:00");

        TimestampRequestData requestData = TimestampRequestData.builder()
                .hashAlgorithm(HashAlgorithm.SHA512)
                .hash("hash".getBytes(UTF_8))
                .nonce(BigInteger.valueOf(4919L))
                .certificateRequested(true)
                .tsaPolicyId("1.3.6")
                .asnEncoded("req".getBytes(UTF_8))
                .build();

        TimestampResponseData responseData = TimestampResponseData.builder()
                .status(ResponseStatus.REJECTION)
                .statusString("test")
                .failureInfo(12)
                .generationTime(generationTime)
                .receptionTime(generationTime)
                .serialNumber(5749L)
                .request(requestData)
                .asnEncoded("res".getBytes(UTF_8))
                .build();

        // when
        TspResponseEntity actualResponseEntity = testSubject.fromDomain(responseData);

        // then
        TspRequestEntity expectedRequestEntity = TspRequestEntity.builder()
                .hashAlgorithm(HashAlgorithm.SHA512)
                .hash("aGFzaA==")
                .nonce("1337")
                .certificateRequested(true)
                .tsaPolicyId("1.3.6")
                .asnEncoded("cmVx")
                .build();

        TspResponseEntity expectedResponseEntity = TspResponseEntity.builder()
                .status(ResponseStatus.REJECTION)
                .statusString("test")
                .failureInfo(12)
                .generationTime(generationTime)
                .receptionTime(generationTime)
                .serialNumber(5749L)
                .request(expectedRequestEntity)
                .asnEncoded("cmVz")
                .build();

        assertThat(actualResponseEntity).isEqualTo(expectedResponseEntity);
    }

}
