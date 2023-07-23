package dev.mieser.tsa.integration.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;

public interface ValidateTimeStampResponseService {

    TimeStampValidationResult validateTimeStampResponse(
        InputStream timestampResponse) throws InvalidTspResponseException;

}
