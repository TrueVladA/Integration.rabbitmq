package ru.bpmcons.sbi_elma.elma.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.Filter;

import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ElmaListRequest {
    @JsonProperty("active")
    private boolean active = true;
    @JsonProperty("fields")
    private final Map<String, Boolean> fields = new HashMap<>();
    @JsonProperty("filter")
    private Filter filter = null;
    @JsonProperty("ids")
    private List<String> ids = null;
    private Integer size = null;
    @JsonProperty("from")
    private Integer from = null;

    public ElmaListRequest allFields() {
        this.fields.put("*", true);
        return this;
    }

    public ElmaListRequest filter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public ElmaListRequest size(int size) {
        this.size = size;
        return this;
    }

    public ElmaListRequest from(int from) {
        this.from = from;
        return this;
    }

    public ElmaListRequest ids(String... ids) {
        if (ids.length == 0) {
            this.ids = null;
            return this;
        }

        if (this.ids == null) {
            this.ids = new ArrayList<>(ids.length);
        }
        this.ids.addAll(Arrays.asList(ids));
        return this;
    }

    public ElmaListRequest ids(List<String> ids) {
        if (ids.isEmpty()) {
            this.ids = null;
            return this;
        }

        if (this.ids == null) {
            this.ids = new ArrayList<>(ids.size());
        }
        this.ids.addAll(ids);
        return this;
    }
}
