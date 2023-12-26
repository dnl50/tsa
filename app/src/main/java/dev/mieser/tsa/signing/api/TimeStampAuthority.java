package dev.mieser.tsa.signing.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;

/**
 * Interface abstraction of a <a href="https://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a> Time Stamp Authority.
 */
public interface TimeStampAuthority {

    /**
     * Initializes the Time Stamp Authority.
     *
     * @throws TsaInitializationException
     *     When an error occurs while initializing the TSA.
     */
    void initialize();

    /**
     * @param tspRequestInputStream
     *     The input stream of an ASN.1 DER encoded TSP response, not {@code null}.
     * @return The TSP response data, including the ASN.1 DER encoded response.
     * @throws InvalidTspRequestException
     *     When the specified input stream does not contain a valid ASN.1 DER encoded TSP request.
     */
    TimeStampResponseData signRequest(InputStream tspRequestInputStream) throws InvalidTspRequestException;

}
