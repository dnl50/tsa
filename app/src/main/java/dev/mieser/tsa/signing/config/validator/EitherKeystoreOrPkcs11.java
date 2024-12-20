package dev.mieser.tsa.signing.config.validator;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom type-level validation constraint, which supports for the {@link dev.mieser.tsa.signing.config.TsaProperties}.
 * Validates that either a Keystore or PKCS#11 has been configured.
 *
 * @see EitherKeystoreOrPkcs11Validator
 */
@Target(TYPE_USE)
@Retention(RUNTIME)
@Constraint(validatedBy = EitherKeystoreOrPkcs11Validator.class)
@Documented
public @interface EitherKeystoreOrPkcs11 {

    String message() default "Either a Keystore or a PKCS#11 device must be configured.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
