package dev.mieser.tsa.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.persistence.entity.TspResponseEntity;
import dev.mieser.tsa.persistence.mapper.TspResponseMapper;

/**
 * {@link TspResponseDataRepository} using a Spring Data JPA Repository to persist the data.
 */
@RequiredArgsConstructor
public class TspResponseDataRepositoryImpl implements TspResponseDataRepository {

    private final TspResponseMapper tspResponseMapper;

    private final TspResponseEntityRepository repository;

    @Override
    public TimestampResponseData save(TimestampResponseData response) {
        TspResponseEntity savedEntity = repository.save(tspResponseMapper.fromDomain(response));
        return tspResponseMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<TimestampResponseData> findById(Long id) {
        return repository.findById(id).map(tspResponseMapper::toDomain);
    }

    @Override
    public Page<TimestampResponseData> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(tspResponseMapper::toDomain);
    }

}
