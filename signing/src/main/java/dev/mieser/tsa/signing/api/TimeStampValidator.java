package dev.mieser.tsa.signing.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimestampValidationResult;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;

/**
 * Interface abstraction of a service verifying that <a href="https://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
 * Time-Stamp Protocol responses were signed by the corresponding {@link TimeStampAuthority} implementation.
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
     * @throws TsaNotInitializedException
     *     When the validator has not yet been {@link #initialize() initialized}.
     * @throws InvalidTspResponseException
     *     When the specified input stream does not contain a valid ASN.1 DER encoded TSP response.
     */
    TimestampValidationResult validateResponse(InputStream tspResponseInputStream);

}
