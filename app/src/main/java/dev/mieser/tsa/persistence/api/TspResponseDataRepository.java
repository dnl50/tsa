package dev.mieser.tsa.persistence.api;

import java.util.Optional;

import dev.mieser.tsa.domain.TimeStampResponseData;

public interface TspResponseDataRepository {

    TimeStampResponseData save(TimeStampResponseData response);

    Optional<TimeStampResponseData> findById(long id);

    Page<TimeStampResponseData> findAll(PageRequest pageRequest);

    boolean deleteById(long id);

    void deleteAll();

}
