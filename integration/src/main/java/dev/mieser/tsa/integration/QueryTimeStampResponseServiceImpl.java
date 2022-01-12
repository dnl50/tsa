package dev.mieser.tsa.integration;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;

@RequiredArgsConstructor
public class QueryTimeStampResponseServiceImpl implements QueryTimeStampResponseService {

    private final TspResponseDataRepository responseDataRepository;

    @Override
    public Page<TimeStampResponseData> findAll(Pageable pageable) {
        return responseDataRepository.findAll(pageable);
    }

    @Override
    public Optional<TimeStampResponseData> findById(Long id) {
        return responseDataRepository.findById(id);
    }

}
