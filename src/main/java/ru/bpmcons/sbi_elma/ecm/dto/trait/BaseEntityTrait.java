package ru.bpmcons.sbi_elma.ecm.dto.trait;

import ru.bpmcons.sbi_elma.ecm.dto.reference.ParentDoc;

public interface BaseEntityTrait {
    String getExternalId();

    String getSource();

    String getCreator();

    String getEditor();

    String getStatus();

    boolean isArchive();

    ParentDoc getParentDoc();

    void setExternalId(String externalId);

    void setSource(String source);

    void setCreator(String creator);

    void setEditor(String editor);

    void setStatus(String status);

    void setArchive(boolean archive);

    void setParentDoc(ParentDoc parentDoc);
}
