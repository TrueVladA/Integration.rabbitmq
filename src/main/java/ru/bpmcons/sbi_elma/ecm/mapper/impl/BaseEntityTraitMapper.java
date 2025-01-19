package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.EcmCreatorEditorService;
import ru.bpmcons.sbi_elma.ecm.EcmService;
import ru.bpmcons.sbi_elma.ecm.dto.base.EntityBase;
import ru.bpmcons.sbi_elma.ecm.dto.reference.EcmDocumentRef;
import ru.bpmcons.sbi_elma.ecm.dto.reference.ParentDoc;
import ru.bpmcons.sbi_elma.ecm.dto.trait.BaseEntityTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class BaseEntityTraitMapper implements TraitMapper<BaseEntityTrait> {
    private final EcmCreatorEditorService creatorEditorService;
    private final EcmService ecmService;

    @Override
    public void mapRequired(GeneralizedDoc doc, BaseEntityTrait target, BaseEntityTrait existingDoc) {
        if (existingDoc != null) {
            target.setExternalId(existingDoc.getExternalId());
        } else {
            target.setExternalId(doc.getId_as_doc());
        }

        target.setSource(SecurityContextHolder.getRequiredContext().getSystem().getId());
        target.setStatus(doc.getStatus());

        target.setArchive(false);

        if (existingDoc != null) {
            target.setEditor(creatorEditorService.findOrCreateEditor(doc.getEditor()).getId());
        } else {
            target.setCreator(creatorEditorService.findOrCreateCreator(doc.getCreator()).getId());
        }
    }

    @Override
    public void mapRest(GeneralizedDoc doc, BaseEntityTrait target, BaseEntityTrait existingDoc) {
        if (doc.getParent_doc() != null) {
            EcmDocumentRef ref = ecmService.findRef(doc.getParent_doc());
            EntityBase foundDoc = ecmService.findDocument(doc.getParent_doc());
            if (foundDoc != null) {
                target.setParentDoc(ParentDoc.builder()
                        .id(foundDoc.getId())
                        .code(ref.getCode())
                        .namespace("ecm_documents")
                        .build());
            } else {
                target.setParentDoc(new ParentDoc());
            }
        }
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof BaseEntityTrait;
    }
}
