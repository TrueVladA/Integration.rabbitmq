package ru.bpmcons.sbi_elma.elma.dto.common.filter.tf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.Filter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.tf.operator.TfOperator;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class TfFilter extends Filter {
    @JsonProperty("tf")
    private final Map<String, TfOperator> operators = new HashMap<>();

    public TfFilter andField(String field, TfOperator operator) {
        this.operators.put(field, operator);
        return this;
    }

    public static TfFilter field(String field, TfOperator operator) {
        var filter = new TfFilter();
        return filter.andField(field, operator);
    }

    @Override
    public Filter optimize() {
        if (this.operators.isEmpty()) {
            return null;
        }
        return this;
    }
}
