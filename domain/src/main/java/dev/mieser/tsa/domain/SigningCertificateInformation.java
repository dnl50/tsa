package dev.mieser.tsa.domain;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;

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

    /**
     * The Base64 encoding of the ASN.1 DER X.509 encoded certificate which was used to sign the request.
     */
    private final String base64EncodedCertificate;

}
