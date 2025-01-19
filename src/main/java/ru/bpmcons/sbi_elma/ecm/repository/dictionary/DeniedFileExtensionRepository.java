package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.DeniedFileExtension;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

import java.util.Optional;

public interface DeniedFileExtensionRepository extends DictionaryRepository<DeniedFileExtension> {
    Optional<DeniedFileExtension> findByFileExtension(String ext);
}
