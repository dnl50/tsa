package dev.mieser.tsa.integration.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.domain.*;
import dev.mieser.tsa.persistence.api.*;
import dev.mieser.tsa.signing.config.HashAlgorithm;

@ExtendWith(MockitoExtension.class)
class QueryTimeStampResponseServiceImplTest {

    @Mock
    private TspResponseDataRepository tspResponseDataRepositoryMock;

    private QueryTimeStampResponseServiceImpl testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new QueryTimeStampResponseServiceImpl(tspResponseDataRepositoryMock);
    }

    @Test
    void findAllDelegatesToRepository() {
        // given
        var pageRequest = new PageRequest(2, 25, new Sort(SortDirection.ASC, "field"));
        var page = new Page<TimeStampResponseData>(25, 2, 12, 300, List.of());

        given(tspResponseDataRepositoryMock.findAll(pageRequest)).willReturn(page);

        // when
        Page<TimeStampResponseData> actual = testSubject.findAll(pageRequest);

        // then
        assertThat(actual).isEqualTo(page);
    }

    @Test
    void findByIdDelegatesToRepository() {
        // given
        var id = 1337L;
        var request = TimeStampRequestData
            .builder(HashAlgorithm.SHA256.getObjectIdentifier(), "sha256".getBytes(), "asn-encoded".getBytes())
            .build();
        var response = TimeStampResponseData
            .builder(ResponseStatus.REJECTION, ZonedDateTime.now(), request, "asn-encoded".getBytes())
            .id(id)
            .failureInfo(FailureInfo.SYSTEM_FAILURE)
            .build();

        given(tspResponseDataRepositoryMock.findById(id)).willReturn(Optional.of(response));

        // when
        Optional<TimeStampResponseData> result = testSubject.findById(id);

        // then
        assertThat(result).contains(response);
    }

}
