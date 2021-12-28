package dev.mieser.tsa.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.persistence.entity.TspResponseEntity;
import dev.mieser.tsa.persistence.mapper.TspResponseMapper;

@ExtendWith(MockitoExtension.class)
class TspResponseDataRepositoryImplTest {

    private final TspResponseMapper tspResponseMapperMock;

    private final TspResponseEntityRepository repositoryMock;

    private final TspResponseDataRepositoryImpl testSubject;

    TspResponseDataRepositoryImplTest(@Mock TspResponseMapper tspResponseMapperMock,
        @Mock TspResponseEntityRepository repositoryMock) {
        this.tspResponseMapperMock = tspResponseMapperMock;
        this.repositoryMock = repositoryMock;

        this.testSubject = new TspResponseDataRepositoryImpl(tspResponseMapperMock, repositoryMock);
    }

    @Test
    void saveReturnsSavedEntity() {
        // given
        TimestampResponseData dataToSave = TimestampResponseData.builder().build();
        TspResponseEntity entityToSave = new TspResponseEntity();
        TspResponseEntity savedEntity = new TspResponseEntity();
        TimestampResponseData savedData = TimestampResponseData.builder().build();

        given(tspResponseMapperMock.fromDomain(dataToSave)).willReturn(entityToSave);
        given(repositoryMock.save(entityToSave)).willReturn(savedEntity);
        given(tspResponseMapperMock.toDomain(savedEntity)).willReturn(savedData);

        // when
        TimestampResponseData actualSavedData = testSubject.save(dataToSave);

        // then
        assertThat(actualSavedData).isEqualTo(savedData);
    }

    @Test
    void findByIdReturnsEmptyOptionalWhenEntityNotFound() {
        // given
        long id = 1337L;

        given(repositoryMock.findById(id)).willReturn(Optional.empty());

        // when
        Optional<TimestampResponseData> actualData = testSubject.findById(id);

        // then
        assertThat(actualData).isEmpty();
    }

    @Test
    void findByIdReturnsPopulatedOptionalWhenEntityIsPresent() {
        // given
        long id = 421L;
        TspResponseEntity entity = new TspResponseEntity();
        TimestampResponseData domain = TimestampResponseData.builder().build();

        given(repositoryMock.findById(id)).willReturn(Optional.of(entity));
        given(tspResponseMapperMock.toDomain(entity)).willReturn(domain);

        // when
        Optional<TimestampResponseData> actualData = testSubject.findById(id);

        // then
        assertThat(actualData).contains(domain);
    }

    @Test
    void findAllReturnsPagedResponses(@Mock Page<TspResponseEntity> entityPageMock,
        @Mock Page<TimestampResponseData> domainPageMock) {
        // given
        PageRequest pageRequest = PageRequest.of(1, 10);

        given(repositoryMock.findAll(pageRequest)).willReturn(entityPageMock);
        willReturn(domainPageMock).given(entityPageMock).map(notNull());

        // when
        Page<TimestampResponseData> actualDomainPage = testSubject.findAll(pageRequest);

        // then
        assertThat(actualDomainPage).isEqualTo(domainPageMock);
    }

}
