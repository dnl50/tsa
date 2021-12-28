package dev.mieser.tsa.integration.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimestampResponseData;

/**
 * @see dev.mieser.tsa.signing.api.TimeStampAuthority
 * @see QueryTimeStampResponseService
 */
public interface IssueTimeStampService {

    TimestampResponseData signTimestampRequest(InputStream tspRequestStream);

}
