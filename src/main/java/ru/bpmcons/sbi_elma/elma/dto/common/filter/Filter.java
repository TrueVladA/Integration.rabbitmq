package ru.bpmcons.sbi_elma.elma.dto.common.filter;

import org.springframework.lang.Nullable;

public abstract class Filter {
    @Nullable
    public abstract Filter optimize();
}
