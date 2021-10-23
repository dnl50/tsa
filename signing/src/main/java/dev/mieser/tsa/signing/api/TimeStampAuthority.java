package dev.mieser.tsa.signing.api;


import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;

import java.io.InputStream;

public interface TimeStampAuthority {

    /**
     * @throws TsaInitializationException
     */
    void initialize();

    /**
     * @param tspRequest
     * @return
     * @throws TsaNotInitializedException
     * @throws UnknownHashAlgorithmException
     * @throws InvalidTspRequestException
     */
    TimestampResponseData signRequest(InputStream tspRequest);


}
