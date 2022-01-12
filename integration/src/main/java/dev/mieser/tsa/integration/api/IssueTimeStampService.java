package dev.mieser.tsa.integration.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimeStampResponseData;

/**
 * @see dev.mieser.tsa.signing.api.TimeStampAuthority
 * @see QueryTimeStampResponseService
 */
public interface IssueTimeStampService {

    TimeStampResponseData signTimestampRequest(InputStream tspRequestStream);

}
