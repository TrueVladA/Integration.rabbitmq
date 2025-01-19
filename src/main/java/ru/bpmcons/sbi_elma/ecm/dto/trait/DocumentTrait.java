package ru.bpmcons.sbi_elma.ecm.dto.trait;

public interface DocumentTrait {
    String getDocType();

    String getDocSeries();

    String getDocNumber();

    String getDocFullNumber();

    java.util.Date getDocDate();

    void setDocType(String docType);

    void setDocSeries(String docSeries);

    void setDocNumber(String docNumber);

    void setDocFullNumber(String docFullNumber);

    void setDocDate(java.util.Date docDate);
}
