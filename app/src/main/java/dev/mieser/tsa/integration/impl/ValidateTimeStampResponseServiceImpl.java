package dev.mieser.tsa.integration.impl;

import java.io.InputStream;

import lombok.RequiredArgsConstructor;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;

@RequiredArgsConstructor
public class ValidateTimeStampResponseServiceImpl implements ValidateTimeStampResponseService {

    private final TimeStampValidator timeStampValidator;

    @Override
    public TimeStampValidationResult validateTimeStampResponse(InputStream timestampResponse) throws InvalidTspResponseException {
        return timeStampValidator.validateResponse(timestampResponse);
    }

}
