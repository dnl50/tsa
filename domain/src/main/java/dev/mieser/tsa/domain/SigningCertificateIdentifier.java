package dev.mieser.tsa.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Encapsulates the signing certificate information contained in the
 * {@code SigningCertificate}/{@code SigningCertificateV2} attribute of a TSP response.
 */
@Data
@Builder
public class SigningCertificateIdentifier {

    /**
     * The Object Identifier (OID) of the hash algorithm.
     */
    private final String hashAlgorithmOid;

    /**
     * The hash of the signing certificate.
     */
    private final byte[] hash;

}
