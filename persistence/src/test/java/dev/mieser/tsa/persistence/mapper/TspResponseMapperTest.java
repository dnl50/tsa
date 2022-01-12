package dev.mieser.tsa.persistence.mapper;

import static dev.mieser.tsa.domain.FailureInfo.BAD_ALGORITHM;
import static dev.mieser.tsa.domain.FailureInfo.SYSTEM_FAILURE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.domain.TimeStampRequestData;
import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.persistence.entity.TspRequestEntity;
import dev.mieser.tsa.persistence.entity.TspResponseEntity;

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
            .failureInfo(SYSTEM_FAILURE)
            .generationTime(generationTime)
            .receptionTime(generationTime)
            .serialNumber(5749L)
            .request(requestEntity)
            .asnEncoded("cmVz")
            .build();

        // when
        TimeStampResponseData actualResponseData = testSubject.toDomain(responseEntity);

        // then
        TimeStampRequestData expectedRequestData = TimeStampRequestData.builder()
            .hashAlgorithm(HashAlgorithm.SHA256)
            .hash("hash".getBytes(UTF_8))
            .nonce(BigInteger.valueOf(4919L))
            .certificateRequested(true)
            .tsaPolicyId("1.3.6")
            .asnEncoded("req".getBytes(UTF_8))
            .build();

        TimeStampResponseData expectedResponseData = TimeStampResponseData.builder()
            .status(ResponseStatus.REJECTION)
            .statusString("test")
            .failureInfo(SYSTEM_FAILURE)
            .generationTime(generationTime)
            .receptionTime(generationTime)
            .serialNumber(BigInteger.valueOf(5749L))
            .request(expectedRequestData)
            .asnEncoded("res".getBytes(UTF_8))
            .build();

        assertThat(actualResponseData).isEqualTo(expectedResponseData);
    }

    @Test
    void fromDomainReturnsExpectedEntity() {
        // given
        ZonedDateTime generationTime = ZonedDateTime.parse("2021-11-13T19:09:27+01:00");

        TimeStampRequestData requestData = TimeStampRequestData.builder()
            .hashAlgorithm(HashAlgorithm.SHA512)
            .hash("hash".getBytes(UTF_8))
            .nonce(BigInteger.valueOf(4919L))
            .certificateRequested(true)
            .tsaPolicyId("1.3.6")
            .asnEncoded("req".getBytes(UTF_8))
            .build();

        TimeStampResponseData responseData = TimeStampResponseData.builder()
            .status(ResponseStatus.REJECTION)
            .statusString("test")
            .failureInfo(BAD_ALGORITHM)
            .generationTime(generationTime)
            .receptionTime(generationTime)
            .serialNumber(BigInteger.valueOf(5749L))
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
            .failureInfo(BAD_ALGORITHM)
            .generationTime(generationTime)
            .receptionTime(generationTime)
            .serialNumber(5749L)
            .request(expectedRequestEntity)
            .asnEncoded("cmVz")
            .build();

        assertThat(actualResponseEntity).isEqualTo(expectedResponseEntity);
    }

}
