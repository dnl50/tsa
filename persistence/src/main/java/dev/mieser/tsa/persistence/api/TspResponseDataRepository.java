package dev.mieser.tsa.persistence.api;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.mieser.tsa.domain.TimeStampResponseData;

public interface TspResponseDataRepository {

    TimeStampResponseData save(TimeStampResponseData response);

    Optional<TimeStampResponseData> findById(Long id);

    Page<TimeStampResponseData> findAll(Pageable pageable);

}
