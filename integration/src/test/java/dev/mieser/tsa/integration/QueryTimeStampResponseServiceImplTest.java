package dev.mieser.tsa.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;

@ExtendWith(MockitoExtension.class)
class QueryTimeStampResponseServiceImplTest {

    private final TspResponseDataRepository tspResponseDataRepositoryMock;

    private final QueryTimeStampResponseServiceImpl testSubject;

    QueryTimeStampResponseServiceImplTest(@Mock TspResponseDataRepository tspResponseDataRepositoryMock) {
        this.tspResponseDataRepositoryMock = tspResponseDataRepositoryMock;

        this.testSubject = new QueryTimeStampResponseServiceImpl(tspResponseDataRepositoryMock);
    }

    @Test
    void findAllDelegatesToRepository(@Mock Page<TimestampResponseData> responseDataPageMock) {
        // given
        Pageable pageable = PageRequest.of(1, 3);

        given(tspResponseDataRepositoryMock.findAll(pageable)).willReturn(responseDataPageMock);

        // when
        Page<TimestampResponseData> actualPage = testSubject.findAll(pageable);

        // then
        assertThat(actualPage).isEqualTo(responseDataPageMock);
    }

    @Test
    void findByIdDelegatesToRepository() {
        // given
        Long id = 1337L;
        TimestampResponseData responseData = TimestampResponseData.builder().build();

        given(tspResponseDataRepositoryMock.findById(id)).willReturn(Optional.of(responseData));

        // when
        Optional<TimestampResponseData> byId = testSubject.findById(id);

        // then
        assertThat(byId).contains(responseData);
    }

}
