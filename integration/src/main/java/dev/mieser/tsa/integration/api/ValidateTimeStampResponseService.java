package dev.mieser.tsa.integration.api;

import dev.mieser.tsa.domain.TimestampValidationResult;

/**
 * @see dev.mieser.tsa.signing.api.TimeStampValidator
 */
public interface ValidateTimeStampResponseService {

    /**
     * @param base64EncodedResponse
     *     The Base64 representation of an ASN.1 DER encoded TSP response, not empty.
     * @return The verification result.
     */
    TimestampValidationResult validateTimeStampResponse(String base64EncodedResponse);

}
