package dev.mieser.tsa.integration.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;

import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import dev.mieser.tsa.integration.impl.IssueTimeStampServiceImpl;
import dev.mieser.tsa.integration.impl.QueryTimeStampResponseServiceImpl;
import dev.mieser.tsa.integration.impl.ValidateTimeStampResponseServiceImpl;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.TimeStampValidator;

public class IntegrationConfig {

    @Produces
    @ApplicationScoped
    IssueTimeStampService issueTimeStampService(TimeStampAuthority timeStampAuthority,
        TspResponseDataRepository responseDataRepository) {
        return new IssueTimeStampServiceImpl(timeStampAuthority, responseDataRepository);
    }

    @Produces
    @ApplicationScoped
    QueryTimeStampResponseService queryTimeStampResponseService(TspResponseDataRepository responseDataRepository) {
        return new QueryTimeStampResponseServiceImpl(responseDataRepository);
    }

    @Produces
    @ApplicationScoped
    ValidateTimeStampResponseService validateTimeStampResponseService(TimeStampValidator timeStampValidator) {
        return new ValidateTimeStampResponseServiceImpl(timeStampValidator);
    }

}
