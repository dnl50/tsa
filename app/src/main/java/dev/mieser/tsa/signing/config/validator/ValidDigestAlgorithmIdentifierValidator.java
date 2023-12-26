package dev.mieser.tsa.signing.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
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
                return false;
            }

            String algorithmName = algorithmNameFinder.getAlgorithmName(parsedIdentifier);
            return digestAlgorithmFinder.find(algorithmName) != null;
        } catch (Exception e) {
            return false;
        }
    }

}
