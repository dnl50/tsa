package dev.mieser.tsa.integration;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class IssueTimeStampServiceImpl implements IssueTimeStampService {

    private final TimeStampAuthority timeStampAuthority;

    private final TspResponseDataRepository responseDataRepository;

    @Override
    public TimestampResponseData signTimestampRequest(InputStream tspRequestStream) {
        TimestampResponseData response = timeStampAuthority.signRequest(tspRequestStream);
        TimestampResponseData savedResponse = responseDataRepository.save(response);
        log.info("Successfully saved TSP response with serial number '{}' with ID '{}'.", savedResponse.getSerialNumber(), savedResponse.getId());
        return savedResponse;
    }

}
