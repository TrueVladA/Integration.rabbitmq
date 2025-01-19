package ru.bpmcons.sbi_elma.elma;

import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListRequest;

@FunctionalInterface
public interface PaginatedRequestBuilder {
    ElmaListRequest build(int current, int step, int total);
}
