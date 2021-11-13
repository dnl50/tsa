package dev.mieser.tsa.integration.config;

import dev.mieser.tsa.integration.PersistentTsaImpl;
import dev.mieser.tsa.integration.api.PersistentTsa;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.persistence.config.PersistenceConfiguration;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.config.TsaConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import({TsaConfiguration.class, PersistenceConfiguration.class})
public class PersistentTsaConfiguration {

    private final TimeStampAuthority timeStampAuthority;

    @Bean
    PersistentTsa persistentTsa(TspResponseDataRepository repository) {
        return new PersistentTsaImpl(timeStampAuthority, repository);
    }

}
