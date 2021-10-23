package dev.mieser.tsa.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.ZonedDateTime;

/**
 * Encapsulates the TSP response data as well as the TSP request data.
 */
@Data
@Builder
public class TimestampResponseData {

    /**
     * An arbitrary Identifier, may be {@code null}.
     */
    private Long id;

    /**
     * The status of the TSP response as defined in {@code PKIStatus}.
     */
    private final int status;

    /**
     * An optional reason text.
     */
    private final String statusString;

    /**
     * The reason why the request was rejected. {@code null} when the request was signed successfully.
     */
    private final Integer failureInfo;

    /**
     * The time the TSP request was received.
     */
    private final ZonedDateTime generationTime;

    /**
     * A unique serial number assigned to the TSP request.
     */
    private final BigInteger serialNumber;

    /**
     * The request this response belongs to.
     */
    private final TimestampRequestData requestData;

    /**
     * The ASN.1 encoded TSP response.
     */
    private final byte[] asnEncoded;

}
