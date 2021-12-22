package dev.mieser.tsa.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom Bean Validation annotation validating that a {@link CharSequence} is a valid Base64 String.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = Base64EncodingValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Base64Encoded {

    /**
     * @return The validation message.
     */
    String message() default "{dev.mieser.tsa.web.validator.Base64Encoded.message}";

    /**
     * @return The groups this validation belongs to. Default is an empty array.
     */
    Class<?>[] groups() default {};

    /**
     * @return Additional payload for the validation. Ignored.
     */
    Class<? extends Payload>[] payload() default {};

}
