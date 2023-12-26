package dev.mieser.tsa.integration.api;

import java.util.Optional;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.persistence.api.Page;
import dev.mieser.tsa.persistence.api.PageRequest;

public interface QueryTimeStampResponseService {

    Page<TimeStampResponseData> findAll(PageRequest pageRequest);

    Optional<TimeStampResponseData> findById(long id);

}
