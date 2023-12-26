package dev.mieser.tsa.integration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.mieser.tsa.integration.api.DeleteTimestampResponseService;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;

@Slf4j
@RequiredArgsConstructor
public class DeleteTimestampResponseServiceImpl implements DeleteTimestampResponseService {

    private final TspResponseDataRepository repository;

    @Override
    public boolean deleteById(long id) {
        boolean deleted = repository.deleteById(id);
        if (deleted) {
            log.info("Successfully deleted response with id '{}'.", id);
        }

        return deleted;
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
        log.info("Successfully deleted all responses.");
    }

}
