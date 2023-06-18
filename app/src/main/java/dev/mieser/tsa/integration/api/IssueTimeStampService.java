package dev.mieser.tsa.integration.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;

// TODO: JavaDoc
public interface IssueTimeStampService {

    TimeStampResponseData signTimestampRequest(
        InputStream tspRequestStream) throws InvalidTspRequestException, UnknownHashAlgorithmException;

}
