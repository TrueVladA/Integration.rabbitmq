package ru.bpmcons.sbi_elma.models.ability;

import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;

public interface DocumentTyped extends EntityTyped {
    @Nullable
    PerecoderObject getDocType();
}
