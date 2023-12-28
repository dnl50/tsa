package dev.mieser.tsa.signing.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.AlgorithmNameFinder;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;

/**
 * {@link ConstraintValidator} for the {@link ValidDigestAlgorithmIdentifier} annotation.
 * 
 * @see DefaultAlgorithmNameFinder
 * @see DefaultDigestAlgorithmIdentifierFinder
 */
@Slf4j
public class ValidDigestAlgorithmIdentifierValidator implements ConstraintValidator<ValidDigestAlgorithmIdentifier, String> {

    private final AlgorithmNameFinder algorithmNameFinder = new DefaultAlgorithmNameFinder();

    private final DigestAlgorithmIdentifierFinder digestAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        return isValidDigestAlgorithmIdentifier(value);
    }

    private boolean isValidDigestAlgorithmIdentifier(String value) {
        try {
            var parsedIdentifier = new ASN1ObjectIdentifier(value);
            if (!algorithmNameFinder.hasAlgorithmName(parsedIdentifier)) {
                log.debug("No digest algorithm name for OID '{}' found.", value);
                return false;
            }

            String algorithmName = algorithmNameFinder.getAlgorithmName(parsedIdentifier);
            AlgorithmIdentifier algorithmIdentifier = digestAlgorithmFinder.find(algorithmName);
            if (algorithmIdentifier == null) {
                log.debug("No digest algorithm identifier found for '{}' (OID '{}').", algorithmName, value);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.debug("Verification for OID '{}' failed.", value, e);
            return false;
        }
    }

}
