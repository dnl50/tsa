package dev.mieser.tsa.persistence.config;

import dev.mieser.tsa.persistence.TspResponseDataRepositoryImpl;
import dev.mieser.tsa.persistence.TspResponseEntityRepository;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.persistence.mapper.TspResponseMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackageClasses = TspResponseEntityRepository.class)
@EnableJpaRepositories(basePackageClasses = TspResponseEntityRepository.class)
public class PersistenceConfiguration {

    @Bean
    TspResponseDataRepository tspResponseDataRepository(TspResponseEntityRepository repository) {
        return new TspResponseDataRepositoryImpl(tspResponseMapper(), repository);
    }

    @Bean
    TspResponseMapper tspResponseMapper() {
        return Mappers.getMapper(TspResponseMapper.class);
    }

}
