package dev.mieser.tsa.integration.api;

import dev.mieser.tsa.domain.TimestampResponseData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.Optional;

public interface PersistentTsa {

    TimestampResponseData signTimestampRequest(InputStream tspRequestStream);

    Optional<TimestampResponseData> findResponseById(long id);

    Page<TimestampResponseData> findAllResponses(Pageable pageable);

}
