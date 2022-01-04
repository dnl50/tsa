package dev.mieser.tsa.domain;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * Encapsulates the TSP response data as well as the TSP request data.
 */
@Data
@Builder
public class TimestampResponseData {

    /**
     * An arbitrary Identifier, may be {@code null}.
     */
    private final Long id;

    /**
     * The status of the TSP response as defined in {@code PKIStatus}.
     */
    private final ResponseStatus status;

    /**
     * An optional reason text.
     */
    private final String statusString;

    /**
     * The reason why the request was rejected. {@code null} when the request was signed successfully.
     */
    private final FailureInfo failureInfo;

    /**
     * The time the TSP request was received.
     */
    private final ZonedDateTime receptionTime;

    /**
     * The time the TSP request was signed. {@code null}, when the request was not signed.
     */
    private final ZonedDateTime generationTime;

    /**
     * A unique serial number assigned to the TSP response.
     */
    private final BigInteger serialNumber;

    /**
     * The request this response belongs to.
     */
    private final TimestampRequestData request;

    /**
     * The ASN.1 encoded TSP response.
     */
    private final byte[] asnEncoded;

}
