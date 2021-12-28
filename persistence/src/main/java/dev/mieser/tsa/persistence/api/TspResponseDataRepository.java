package dev.mieser.tsa.persistence.api;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.mieser.tsa.domain.TimestampResponseData;

public interface TspResponseDataRepository {

    TimestampResponseData save(TimestampResponseData response);

    Optional<TimestampResponseData> findById(Long id);

    Page<TimestampResponseData> findAll(Pageable pageable);

}
