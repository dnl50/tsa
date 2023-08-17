package dev.mieser.tsa.domain;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeStampValidationResult {

    /**
     * The status of the TSP response as defined in {@code PKIStatus}.
     */
    private final ResponseStatus status;

    /**
     * An optional status description.
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
     * The Object Identifier (OID) of the hash algorithm which was used.
     */
    private final String hashAlgorithmIdentifier;

    /**
     * The hash which was signed.
     */
    private final byte[] hash;

    /**
     * The identifier of the signing certificate.
     * <p/>
     * May be null when the response does not contain a time stamp token.
     */
    private final SigningCertificateIdentifier signingCertificateIdentifier;

    /**
     * General information of the certificate which was used to sign the request.
     * <p/>
     * May be null when the signing certificate was not included in the response.
     */
    private final SigningCertificateInformation signingCertificateInformation;

    /**
     * Is the signature of the response valid?
     */
    private final boolean signatureValid;

}
