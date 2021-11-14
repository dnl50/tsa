package dev.mieser.tsa.integration.config;

import dev.mieser.tsa.integration.IssueTimeStampServiceImpl;
import dev.mieser.tsa.integration.QueryTimeStampResponseServiceImpl;
import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
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
public class TsaIntegrationConfiguration {

    private final TimeStampAuthority timeStampAuthority;

    @Bean
    IssueTimeStampService persistentTsa(TspResponseDataRepository repository) {
        return new IssueTimeStampServiceImpl(timeStampAuthority, repository);
    }

    @Bean
    QueryTimeStampResponseService queryTimeStampResponseService(TspResponseDataRepository repository) {
        return new QueryTimeStampResponseServiceImpl(repository);
    }

}
