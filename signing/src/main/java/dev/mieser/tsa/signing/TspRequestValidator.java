package dev.mieser.tsa.signing;

import dev.mieser.tsa.domain.HashAlgorithm;
import org.bouncycastle.tsp.TimeStampRequest;

/**
 * Service for validating incoming TSP requests.
 */
public class TspRequestValidator {

    /**
     * @param request The request to validate, not {@code null}.
     * @return {@code true} iff a {@link HashAlgorithm} with the algorithm OID specified in the request exists.
     */
    public boolean isKnownHashAlgorithm(TimeStampRequest request) {
        return HashAlgorithm.fromObjectIdentifier(request.getMessageImprintAlgOID().getId()).isPresent();
    }

}
