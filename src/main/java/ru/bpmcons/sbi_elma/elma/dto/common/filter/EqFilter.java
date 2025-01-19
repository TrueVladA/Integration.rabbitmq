package ru.bpmcons.sbi_elma.elma.dto.common.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.elma.dto.common.field.BoolConstField;
import ru.bpmcons.sbi_elma.elma.dto.common.field.ConstField;
import ru.bpmcons.sbi_elma.elma.dto.common.field.Field;
import ru.bpmcons.sbi_elma.elma.dto.common.field.RefField;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class EqFilter extends Filter {
    @JsonProperty("eq")
    private final List<Field> fields;

    public static EqFilter eq(Field a, Field b) {
        return new EqFilter(List.of(a, b));
    }

    public static EqFilter field(String field, String c) {
        if (c == null) {
            return null;
        }
        return eq(new RefField(field), new ConstField(c));
    }

    public static EqFilter field(String field, boolean c) {
        return eq(new RefField(field), new BoolConstField(c));
    }

    @Override
    public Filter optimize() {
        return this;
    }
}
