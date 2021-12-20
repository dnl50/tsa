package dev.mieser.tsa.integration.api;

import dev.mieser.tsa.domain.TimestampResponseData;

import java.io.InputStream;

/**
 * @see dev.mieser.tsa.signing.api.TimeStampAuthority
 * @see QueryTimeStampResponseService
 */
public interface IssueTimeStampService {

    TimestampResponseData signTimestampRequest(InputStream tspRequestStream);

}
