package dev.mieser.tsa.integration.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;

/**
 * @see dev.mieser.tsa.signing.api.TimeStampValidator
 */
public interface ValidateTimeStampResponseService {

    /**
     * @param timestampResponse
     *     The time stamp response to validate, not {@code null}.
     * @return The verification result.
     */
    TimeStampValidationResult validateTimeStampResponse(
        InputStream timestampResponse) throws InvalidTspResponseException, UnknownHashAlgorithmException;

}
