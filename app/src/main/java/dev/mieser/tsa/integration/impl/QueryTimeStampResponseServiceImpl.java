package dev.mieser.tsa.integration.impl;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
import dev.mieser.tsa.persistence.api.Page;
import dev.mieser.tsa.persistence.api.PageRequest;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;

@RequiredArgsConstructor
public class QueryTimeStampResponseServiceImpl implements QueryTimeStampResponseService {

    private final TspResponseDataRepository responseDataRepository;

    @Override
    public Page<TimeStampResponseData> findAll(PageRequest pageRequest) {
        return responseDataRepository.findAll(pageRequest);
    }

    @Override
    public Optional<TimeStampResponseData> findById(long id) {
        return responseDataRepository.findById(id);
    }

}
