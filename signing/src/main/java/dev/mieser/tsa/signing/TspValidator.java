package dev.mieser.tsa.signing;

import dev.mieser.tsa.domain.HashAlgorithm;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * Service for validating TSP requests/responses.
 */
public class TspValidator {

    /**
     * @param algorithmIdentifier The OID to check, not {@code null}.
     * @return {@code true} iff a {@link HashAlgorithm} with the specified OID exists.
     */
    public boolean isKnownHashAlgorithm(ASN1ObjectIdentifier algorithmIdentifier) {
        return HashAlgorithm.fromObjectIdentifier(algorithmIdentifier.getId()).isPresent();
    }

}
