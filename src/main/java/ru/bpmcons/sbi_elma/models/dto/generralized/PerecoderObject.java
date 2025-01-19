package ru.bpmcons.sbi_elma.models.dto.generralized;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.lang.Nullable;


@Data
public class PerecoderObject {
    private String dictName;
    private DictValues[] dictValues;

    public boolean valid() {
        return getDictValues() != null &&
                getDictValues()[0].getDictValue() != null &&
                getDictValues()[0].getDictValue().getValue() != null &&
                !getDictValues()[0].getDictValue().getValue().isEmpty();
    }

  
    @Nullable
    @JsonIgnore
    public String getSingleValue() {
        if (getDictValues() == null || getDictValues().length == 0) {
            return null;
        }
        DictValues value = getDictValues()[0];
        if (value == null
                || value.getDictValue() == null
                || value.getDictValue().getValue() == null
                || value.getDictValue().getValue().isBlank()) {
            return null;
        }
        return value.getDictValue().getValue();
    }
}
