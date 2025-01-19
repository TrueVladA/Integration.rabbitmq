package ru.bpmcons.sbi_elma.validation;

import org.springframework.util.StringUtils;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidIdentifiable.IdentifiableValidator.class)
public @interface ValidIdentifiable {
    String message() default "Документ невозможно идентифицировать";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class IdentifiableValidator implements ConstraintValidator<ValidIdentifiable, Identifiable> {

        @Override
        public boolean isValid(Identifiable identifiable, ConstraintValidatorContext constraintValidatorContext) {
            if (identifiable == null) {
                return false;
            }
            //noinspection ConstantValue
            if (!(identifiable instanceof Identifiable)) {
                throw new IllegalArgumentException("Ошибка сигнатуры, ожидается класс implements Identifiable");
            }
            constraintValidatorContext.disableDefaultConstraintViolation();
            boolean valid = true;
            if (!StringUtils.hasText(identifiable.getEcmId())) {
                valid = false;
                constraintValidatorContext.buildConstraintViolationWithTemplate("Параметр не заполнен")
                        .addPropertyNode("id_ecm_doc")
                        .addConstraintViolation();
            }
            boolean docTypeValid = identifiable.getDocType() != null && identifiable.getDocType().valid();
            boolean contractTypeValid = identifiable.getContractType() != null && identifiable.getContractType().valid();
            if (!docTypeValid && !contractTypeValid) {
                constraintValidatorContext.buildConstraintViolationWithTemplate("Объект перекодера невалиден")
                        .addPropertyNode("doc_type")
                        .addConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Объект перекодера невалиден")
                        .addPropertyNode("contract_type")
                        .addConstraintViolation();
                valid = false;
            }
            return valid;
        }
    }
}
