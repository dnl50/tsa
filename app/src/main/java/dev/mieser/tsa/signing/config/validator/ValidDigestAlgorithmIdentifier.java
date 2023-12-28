package dev.mieser.tsa.signing.config.validator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom Jakarta Bean Validation constraint, which supports {@link String} values. Blank values are considered valid.
 * Verifies that the given {@link String} can be converted to an OID.
 * 
 * @see ValidDigestAlgorithmIdentifierValidator
 */
@Target({ FIELD, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidDigestAlgorithmIdentifierValidator.class)
@Documented
public @interface ValidDigestAlgorithmIdentifier {

    String message() default "${validatedValue} is neither an OID nor a known digest algorithm name.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
