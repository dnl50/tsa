package dev.mieser.tsa.signing.api;


import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.signing.api.exception.*;

import java.io.InputStream;

/**
 * Interface abstraction of a <a href="https://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a> Time Stamp Authority.
 */
public interface TimeStampAuthority {

    /**
     * @throws TsaInitializationException When an error occurs while initializing the TSA.
     */
    void initialize();

    /**
     * @param tspRequestInputStream The input stream which contains a ASN.1 DER encoded TSP response, not {@code null}.
     * @return The TSP response data, including the ASN.1 DER encoded response.
     * @throws TsaNotInitializedException    When the TSA has not yet been {@link #initialize() initialized}.
     * @throws UnknownHashAlgorithmException When the hash algorithm specified in the TSP request
     * @throws InvalidTspRequestException    When the specified input stream does not contain a valid ASN.1 DER encoded TSP request.
     * @throws TspResponseException          When an error occurs while generating the signing TSP request.
     */
    TimestampResponseData signRequest(InputStream tspRequestInputStream);


}
