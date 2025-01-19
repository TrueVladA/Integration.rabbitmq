package ru.bpmcons.sbi_elma.elma.dto.common.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrFilter extends Filter {
    @JsonProperty("or")
    private final List<Filter> or;

    public OrFilter or(Filter filter) {
        if (filter == null) {
            return this;
        }
        this.or.add(filter);
        return this;
    }

    public static OrFilter or(Filter... filter) {
        return new OrFilter(Stream.of(filter).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @Override
    public Filter optimize() {
        List<Filter> optimized = this.or.stream()
                .map(Filter::optimize)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (optimized.isEmpty()) {
            return null;
        }
        if (optimized.size() == 1) {
            return optimized.get(0);
        }
        return new OrFilter(optimized);
    }
}
