package ru.bpmcons.sbi_elma.ecm.dto.trait;

public interface ContractTrait {
    String getContractType();

    String getContractSeries();

    String getContractNumber();

    String getContractFullNumber();

    java.util.Date getContractDate();

    void setContractType(String contractType);

    void setContractSeries(String contractSeries);

    void setContractNumber(String contractNumber);

    void setContractFullNumber(String contractFullNumber);

    void setContractDate(java.util.Date contractDate);
}
