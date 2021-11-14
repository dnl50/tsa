package dev.mieser.tsa.integration;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class QueryTimeStampResponseServiceImpl implements QueryTimeStampResponseService {

    private final TspResponseDataRepository responseDataRepository;

    @Override
    public Page<TimestampResponseData> findAll(Pageable pageable) {
        return responseDataRepository.findAll(pageable);
    }

}
