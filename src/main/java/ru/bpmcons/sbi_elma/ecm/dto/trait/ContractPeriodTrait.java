package ru.bpmcons.sbi_elma.ecm.dto.trait;

public interface ContractPeriodTrait {
    java.util.Date getContractStartDate();

    java.util.Date getContractEndDate();

    void setContractStartDate(java.util.Date contractStartDate);

    void setContractEndDate(java.util.Date contractEndDate);
}
