package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.FileMetadataTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.methods.CreateOrUpdateFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class FileMetadataTraitMapper implements TraitMapper<FileMetadataTrait> {
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;

    @Override
    public void mapRequired(GeneralizedDoc doc, FileMetadataTrait target, FileMetadataTrait existingDoc) {
        FileMetadata[] meta = createOrUpdateFileMetadata.createOrUpdateFileMetadata(
                existingDoc == null ? null : existingDoc.getFileMetadata(),
                doc,
                SecurityContextHolder.getRequiredContext().getSystem(),
                MessagePropertiesHolder.getVersion()
        );
        doc.setFile_metadata(meta);
        target.setFileMetadata(createOrUpdateFileMetadata.createTableFileMetadata(meta));
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof FileMetadataTrait;
    }
}
