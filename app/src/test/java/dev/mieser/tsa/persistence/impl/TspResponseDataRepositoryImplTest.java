package dev.mieser.tsa.persistence.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.domain.TimeStampRequestData;
import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.signing.config.HashAlgorithm;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestTransaction
class TspResponseDataRepositoryImplTest {

    private final TspResponseDataRepository testSubject;

    @Inject
    TspResponseDataRepositoryImplTest(TspResponseDataRepository testSubject) {
        this.testSubject = testSubject;
    }

    @Test
    void saveAssignsId() {
        // given
        var request = new TimeStampRequestData(HashAlgorithm.SHA256.getObjectIdentifier(),
            "sha256".getBytes(UTF_8),
            BigInteger.TEN,
            false,
            null,
            "request".getBytes(UTF_8));
        var response = new TimeStampResponseData(null,
            ResponseStatus.GRANTED,
            "Success!",
            null,
            ZonedDateTime.parse("2023-07-09T13:37:00+04:00"),
            ZonedDateTime.parse("2023-07-09T13:37:01+04:00"),
            BigInteger.TWO,
            request,
            "response".getBytes(UTF_8));

        // when
        TimeStampResponseData savedResponse = testSubject.save(response);

        // then
        assertThat(savedResponse.getId()).isNotNull();
    }

}
