package dev.mieser.tsa.persistence.api;

import dev.mieser.tsa.domain.TimestampResponseData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TspResponseDataRepository {

    TimestampResponseData save(TimestampResponseData response);

    Optional<TimestampResponseData> findById(Long id);

    Page<TimestampResponseData> findAll(Pageable pageable);

}
