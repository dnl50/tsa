package dev.mieser.tsa.domain;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimestampValidationResult {

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
    private final ZonedDateTime generationTime;

    /**
     * A unique serial number assigned to the TSP response.
     */
    private final BigInteger serialNumber;

    /**
     * The nonce which was included.
     * <p/>
     * Can be {@code null}.
     */
    private final BigInteger nonce;

    /**
     * The hash algorithm which was used.
     */
    private final HashAlgorithm hashAlgorithm;

    /**
     * The hash which was signed.
     */
    private final byte[] hash;

    /**
     * General information about the certificate which was used to
     * <p/>
     * May be null when the certificate was not included in the response.
     */
    private final SigningCertificateInformation signingCertificateInformation;

    /**
     * A flag whether the response was signed by the certificate which is currently in use.
     */
    private final boolean signedByThisTsa;

}
