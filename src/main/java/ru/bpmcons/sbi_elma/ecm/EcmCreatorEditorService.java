package ru.bpmcons.sbi_elma.ecm;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.reference.CreatorEditor;
import ru.bpmcons.sbi_elma.ecm.dto.reference.Email;
import ru.bpmcons.sbi_elma.exceptions.CreatorRequiredException;
import ru.bpmcons.sbi_elma.exceptions.EditorRequiredException;
import ru.bpmcons.sbi_elma.models.dto.generralized.Creator_editor;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;
import ru.bpmcons.sbi_elma.service.SeparaterFio;

@Service
@RequiredArgsConstructor
public class EcmCreatorEditorService {
    private final EcmService ecmService;
    private final SeparaterFio separaterFio;
    private final SysNamesConstants sysNamesConstants;

    @NonNull
    public CreatorEditor findOrCreateCreator(@Nullable Creator_editor creatorEditor) {
        if (creatorEditor == null) {
            throw new CreatorRequiredException();
        }
        return findOrCreate(creatorEditor);
    }

    @NonNull
    public CreatorEditor findOrCreateEditor(@Nullable Creator_editor creatorEditor) {
        if (creatorEditor == null) {
            throw new EditorRequiredException();
        }
        return findOrCreate(creatorEditor);
    }

    private CreatorEditor findOrCreate(Creator_editor creatorEditor) {
        return ecmService.findCreatorEditor(creatorEditor.getEmail(), creatorEditor.getId_as_creator())
                .orElseGet(() -> createCreatorEditor(creatorEditor));
    }

    @NonNull
    public CreatorEditor createCreatorEditor(@NonNull Creator_editor creatorEditor) {
        return ecmService.createReference(
                sysNamesConstants.getCreatorEditor(),
                CreatorEditor.builder()
                    .source(SecurityContextHolder.getRequiredContext().getSystem().getId())
                    .externalId(creatorEditor.getId_as_creator())
                    .role(creatorEditor.getRole())
                    .email(new Email[]{Email.mainEmail(creatorEditor.getEmail())})
                    .fio(separaterFio.separate(creatorEditor.getFullname()))
                    .build()
        );
    }
}
