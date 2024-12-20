package dev.mieser.tsa.signing.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import dev.mieser.tsa.signing.config.TsaProperties;

/**
 * {@link ConstraintValidator} for the {@link EitherKeystoreOrPkcs11} annotation.
 * 
 * @see EitherKeystoreOrPkcs11
 */
public class EitherKeystoreOrPkcs11Validator implements ConstraintValidator<EitherKeystoreOrPkcs11, TsaProperties> {

    @Override
    public boolean isValid(TsaProperties value, ConstraintValidatorContext context) {
        boolean keystoreConfigured = value.keystore().isPresent();
        boolean pkcs11Configured = value.pkcs11().isPresent();

        return (keystoreConfigured && !pkcs11Configured) || (!keystoreConfigured && pkcs11Configured);
    }

}
