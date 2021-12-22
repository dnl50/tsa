package dev.mieser.tsa.web.validator;

import org.apache.commons.codec.binary.Base64;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@link ConstraintValidator} validating that a {@link Base64Encoded} annotated {@link CharSequence} is a valid Base64 String.
 */
public class Base64EncodingValidator implements ConstraintValidator<Base64Encoded, CharSequence> {

    /**
     * @param value   The {@link CharSequence} to validate.
     * @param context The context in which the constraint is evaluated.
     * @return {@code true}, iff the specified value is {@code null} or a valid Base64 String.
     */
    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return Base64.isBase64(value.toString());
    }

}
