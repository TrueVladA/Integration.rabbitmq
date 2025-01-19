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
public class AndFilter extends Filter {
    @JsonProperty("and")
    private final List<Filter> and;

    public AndFilter and(Filter filter) {
        if (filter == null) {
            return this;
        }
        this.and.add(filter);
        return this;
    }

    public static AndFilter and(Filter... filter) {
        return new AndFilter(Stream.of(filter).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @Override
    public Filter optimize() {
        List<Filter> optimized = this.and.stream()
                .map(Filter::optimize)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (optimized.isEmpty()) {
            return null;
        }
        if (optimized.size() == 1) {
            return optimized.get(0);
        }
        return new AndFilter(optimized);
    }
}
