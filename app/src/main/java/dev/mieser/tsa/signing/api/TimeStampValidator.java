package dev.mieser.tsa.signing.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;

/**
 * Interface abstraction of a service verifying that <a href="https://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
 * Time-Stamp Protocol responses were signed by this {@link TimeStampAuthority} implementation.
 */
public interface TimeStampValidator {

    /**
     * Initializes this validator.
     *
     * @throws TsaInitializationException
     *     When an error occurs while initializing the validator.
     */
    void initialize();

    /**
     * Verifies whether a TSP response was issued by the corresponding TSA.
     *
     * @param tspResponseInputStream
     *     The input stream of an ASN.1 DER encoded TSP request, not {@code null}.
     * @return The verification result of the TSP response.
     * @throws InvalidTspResponseException
     *     When the specified input stream does not contain a valid ASN.1 DER encoded TSP response.
     */
    TimeStampValidationResult validateResponse(InputStream tspResponseInputStream) throws InvalidTspResponseException;

}
