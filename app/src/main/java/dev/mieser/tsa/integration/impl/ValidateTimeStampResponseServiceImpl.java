package dev.mieser.tsa.integration.impl;

import java.io.IOException;
import java.io.InputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import dev.mieser.tsa.signing.api.exception.InvalidCertificateException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;

@Slf4j
@RequiredArgsConstructor
public class ValidateTimeStampResponseServiceImpl implements ValidateTimeStampResponseService {

    private final TimeStampValidator timeStampValidator;

    @Override
    public TimeStampValidationResult validateTimeStampResponse(InputStream timestampResponse) throws InvalidTspResponseException {
        return timeStampValidator.validateResponse(timestampResponse);
    }

    @Override
    public TimeStampValidationResult validateTimeStampResponse(InputStream timestampResponse,
        InputStream x509Certificate) throws InvalidTspResponseException, InvalidCertificateException {
        try (timestampResponse; x509Certificate) {
            return timeStampValidator.validateResponse(timestampResponse, x509Certificate);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close input stream", e);
        }
    }

}
