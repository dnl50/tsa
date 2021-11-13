package dev.mieser.tsa.integration;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.integration.api.PersistentTsa;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.Optional;

@RequiredArgsConstructor
public class PersistentTsaImpl implements PersistentTsa {

    private final TimeStampAuthority timeStampAuthority;

    private final TspResponseDataRepository responseDataRepository;

    @Override
    public TimestampResponseData signTimestampRequest(InputStream tspRequestStream) {
        TimestampResponseData response = timeStampAuthority.signRequest(tspRequestStream);
        return responseDataRepository.save(response);
    }

    @Override
    public Optional<TimestampResponseData> findResponseById(long id) {
        return responseDataRepository.findById(id);
    }

    @Override
    public Page<TimestampResponseData> findAllResponses(Pageable pageable) {
        return responseDataRepository.findAll(pageable);
    }

}
