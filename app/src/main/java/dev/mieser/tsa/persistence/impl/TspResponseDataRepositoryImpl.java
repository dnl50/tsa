package dev.mieser.tsa.persistence.impl;

import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.persistence.api.Page;
import dev.mieser.tsa.persistence.api.PageRequest;
import dev.mieser.tsa.persistence.api.SortDirection;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.persistence.impl.entity.TspResponseEntity;
import dev.mieser.tsa.persistence.impl.mapper.TspResponseMapper;
import io.quarkus.panache.common.Sort;

/**
 * {@link TspResponseDataRepository} using a Panache Repository to persist the data.
 */
@RequiredArgsConstructor
public class TspResponseDataRepositoryImpl implements TspResponseDataRepository {

    private final TspResponseMapper tspResponseMapper;

    private final TspPanacheRepository repository;

    @Override
    public TimeStampResponseData save(TimeStampResponseData response) {
        TspResponseEntity entity = tspResponseMapper.fromDomain(response);
        repository.persist(entity);
        return tspResponseMapper.toDomain(entity);
    }

    @Override
    public Optional<TimeStampResponseData> findById(Long id) {
        return repository.findByIdOptional(id).map(tspResponseMapper::toDomain);
    }

    @Override
    public Page<TimeStampResponseData> findAll(PageRequest pageRequest) {
        var pagedQuery = repository.findAll(mapSort(pageRequest))
            .page(pageRequest.pageNumber(), pageRequest.size());
        var mappedEntries = pagedQuery.stream()
            .map(tspResponseMapper::toDomain)
            .collect(Collectors.toList());

        return new Page<>(pageRequest.size(), pageRequest.pageNumber(), pagedQuery.pageCount(), pagedQuery.count(),
            mappedEntries);
    }

    private Sort mapSort(PageRequest pageRequest) {
        var sort = pageRequest.sort();
        return sort != null ? Sort.by(sort.attributeName(), mapDirection(sort.direction())) : Sort.empty();
    }

    private Sort.Direction mapDirection(SortDirection direction) {
        return switch (direction) {
        case ASC -> Sort.Direction.Ascending;
        case DESC -> Sort.Direction.Descending;
        };
    }

}
