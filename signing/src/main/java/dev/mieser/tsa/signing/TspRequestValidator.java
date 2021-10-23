package dev.mieser.tsa.signing;

import dev.mieser.tsa.domain.HashAlgorithm;
import org.bouncycastle.tsp.TimeStampRequest;

public class TspRequestValidator {

    public boolean isKnownHashAlgorithm(TimeStampRequest request) {
        return HashAlgorithm.fromObjectIdentifier(request.getMessageImprintAlgOID().getId()).isPresent();
    }

}
