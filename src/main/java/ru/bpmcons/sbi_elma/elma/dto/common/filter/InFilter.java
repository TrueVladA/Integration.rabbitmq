package ru.bpmcons.sbi_elma.elma.dto.common.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.elma.dto.common.field.Field;
import ru.bpmcons.sbi_elma.elma.dto.common.field.ListField;
import ru.bpmcons.sbi_elma.elma.dto.common.field.RefField;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class InFilter extends Filter {
    @JsonProperty("in")
    private final List<Field> fields;

    public static InFilter eq(Field a, Field b) {
        return new InFilter(List.of(a, b));
    }

    public static InFilter field(String field, List<String> c) {
        if (c == null || c.isEmpty()) {
            return null;
        }
        return eq(new RefField(field), new ListField(c));
    }

    @Override
    public Filter optimize() {
        return this;
    }
}
