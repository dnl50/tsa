package dev.mieser.tsa.signing.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;

import dev.mieser.tsa.signing.config.DigestAlgorithmConverter;

/**
 * {@link ConstraintValidator} for the {@link ValidDigestAlgorithmIdentifier} annotation.
 * 
 * @see DefaultAlgorithmNameFinder
 * @see DefaultDigestAlgorithmIdentifierFinder
 */
@Slf4j
public class ValidDigestAlgorithmIdentifierValidator implements ConstraintValidator<ValidDigestAlgorithmIdentifier, String> {

    private final DigestAlgorithmConverter digestAlgorithmConverter = new DigestAlgorithmConverter();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        return isValidDigestAlgorithmIdentifier(value);
    }

    private boolean isValidDigestAlgorithmIdentifier(String value) {
        return digestAlgorithmConverter.convert(value) != null;
    }

}
