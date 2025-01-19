package ru.bpmcons.sbi_elma.validation;

import ru.bpmcons.sbi_elma.infra.version.Since;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.utils.MD5Utils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidMD5Hash.Validator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMD5Hash {
    String message() default "CRC файла - не MD5-хеш формата RFC 1864";
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Since since() default @Since();

    class Validator implements ConstraintValidator<ValidMD5Hash, String> {

        private Version since;

        @Override
        public void initialize(ValidMD5Hash constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
            since = new Version(
                    constraintAnnotation.since().major(),
                    constraintAnnotation.since().minor(),
                    constraintAnnotation.since().patch()
            );
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (MessagePropertiesHolder.checkVersion(version -> version.isNotBefore(since))) {
                return MD5Utils.isValidHash(value);
            } else {
                return true;
            }
        }
    }
}
