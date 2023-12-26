package dev.mieser.tsa.persistence.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;

import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.persistence.impl.TspPanacheRepository;
import dev.mieser.tsa.persistence.impl.TspResponseDataRepositoryImpl;
import dev.mieser.tsa.persistence.impl.mapper.TspResponseMapperImpl;

class PersistenceConfig {

    @Produces
    @ApplicationScoped
    TspResponseDataRepository tspResponseDataRepository(TspPanacheRepository tspPanacheRepository) {
        return new TspResponseDataRepositoryImpl(new TspResponseMapperImpl(), tspPanacheRepository);
    }

}
