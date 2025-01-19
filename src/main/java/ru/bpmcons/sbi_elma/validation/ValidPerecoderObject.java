package ru.bpmcons.sbi_elma.validation;

import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPerecoderObject.PerecoderValidator.class)
public @interface ValidPerecoderObject {
    String message() default "Неверный объект перекодера";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class PerecoderValidator implements ConstraintValidator<ValidPerecoderObject, PerecoderObject> {

        @Override
        public boolean isValid(PerecoderObject obj, ConstraintValidatorContext constraintValidatorContext) {
            if (obj == null) {
                return false;
            }
            //noinspection ConstantValue
            if (!(obj instanceof PerecoderObject)) {
                throw new IllegalArgumentException("Ошибка сигнатуры, ожидается класс PerecoderObject");
            }
            return obj.valid();
        }
    }
}
