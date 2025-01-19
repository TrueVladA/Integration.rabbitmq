package ru.bpmcons.sbi_elma.ecm.dto.trait;

import java.math.BigDecimal;

public interface SumTrait {
    BigDecimal getSum();

    String getCurrency();

    void setSum(BigDecimal decimal);

    void setCurrency(String currency);
}
