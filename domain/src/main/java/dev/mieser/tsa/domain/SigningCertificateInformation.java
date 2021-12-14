package dev.mieser.tsa.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.ZonedDateTime;

/**
 * Encapsulates information about the certificate which was used for signing.
 */
@Data
@Builder
public class SigningCertificateInformation {

    /**
     * The issuer of the certificate.
     */
    private final String issuer;

    /**
     * The serial number of the certificate.
     */
    private final BigInteger serialNumber;

    /**
     * The expiration date of the certificate.
     */
    private final ZonedDateTime expirationDate;

}
