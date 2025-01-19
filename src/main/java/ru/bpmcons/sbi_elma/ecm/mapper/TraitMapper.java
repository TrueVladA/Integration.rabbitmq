package ru.bpmcons.sbi_elma.ecm.mapper;

import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

public interface TraitMapper<T> {
    /**
     * Заполняем только необходимые для создания поля
     */
    default void mapRequired(GeneralizedDoc doc, T target, @Nullable T existingDoc) {}

    /**
     * Заполняем оистальные поля
     */
    default void mapRest(GeneralizedDoc doc, T target, @Nullable T existingDoc) {}

    boolean isApplicable(Object target);
}
