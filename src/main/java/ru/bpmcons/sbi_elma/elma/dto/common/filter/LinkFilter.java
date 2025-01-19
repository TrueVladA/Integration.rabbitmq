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
public class LinkFilter extends Filter {
    @JsonProperty("link")
    private final List<Field> fields;

    public static LinkFilter link(Field a, Field b) {
        return new LinkFilter(List.of(a, b));
    }

    public static LinkFilter list(String field, String... c) {
        return link(new RefField(field), new ListField(List.of(c)));
    }

    @Override
    public Filter optimize() {
        return this;
    }
}
