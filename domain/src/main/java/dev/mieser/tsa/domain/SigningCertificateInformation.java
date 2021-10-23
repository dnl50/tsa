package dev.mieser.tsa.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.time.ZonedDateTime;

/**
 * Encapsulates information about the certificate used for signing TSP requests.
 */
@Getter
@RequiredArgsConstructor
public class SigningCertificateInformation {

    /**
     * The issuer of the certificate.
     */
    private final String issuer;

    /**
     * The serial number of the
     */
    private final BigInteger serialNumber;

    /**
     * The expiration date of the certificate.
     */
    private final ZonedDateTime expirationDate;

}
