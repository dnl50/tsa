package dev.mieser.tsa.integration.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;

public interface IssueTimeStampService {

    TimeStampResponseData signTimestampRequest(
        InputStream tspRequestStream) throws InvalidTspRequestException;

}
