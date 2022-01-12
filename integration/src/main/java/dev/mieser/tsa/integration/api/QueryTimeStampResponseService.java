package dev.mieser.tsa.integration.api;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.mieser.tsa.domain.TimeStampResponseData;

/**
 * Interface abstraction of a service used to query TSP responses.
 */
public interface QueryTimeStampResponseService {

    Page<TimeStampResponseData> findAll(Pageable pageable);

    Optional<TimeStampResponseData> findById(Long id);

}
