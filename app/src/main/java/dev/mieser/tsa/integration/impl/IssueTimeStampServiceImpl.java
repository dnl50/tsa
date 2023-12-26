package dev.mieser.tsa.integration.impl;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.integration.api.TimeStampListener;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;

@Slf4j
@RequiredArgsConstructor
public class IssueTimeStampServiceImpl implements IssueTimeStampService {

    private final TimeStampAuthority timeStampAuthority;

    private final TspResponseDataRepository responseDataRepository;

    private final Set<TimeStampListener> registeredListeners = ConcurrentHashMap.newKeySet();

    @Override
    public TimeStampResponseData signTimestampRequest(InputStream tspRequestStream) throws InvalidTspRequestException {
        TimeStampResponseData response = timeStampAuthority.signRequest(tspRequestStream);
        TimeStampResponseData savedResponse = responseDataRepository.save(response);
        log.info("Successfully saved TSP response with serial number '{}' with ID '{}'.", savedResponse.getSerialNumber(),
            savedResponse.getId());

        notifyListeners(savedResponse);

        return savedResponse;
    }

    @Override
    public void registerListener(TimeStampListener listener) {
        registeredListeners.add(listener);
    }

    @Override
    public void unregisterListener(TimeStampListener listener) {
        registeredListeners.remove(listener);
    }

    private void notifyListeners(TimeStampResponseData response) {
        registeredListeners.forEach(listener -> {
            try {
                listener.onResponse(response);
            } catch (Exception e) {
                log.warn("Failed to notify listener.", e);
            }
        });
    }

}
