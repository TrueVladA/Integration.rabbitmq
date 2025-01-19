package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.FileType;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

import java.util.Optional;

public interface FileTypeRepository extends DictionaryRepository<FileType> {
    FileType findById(String id);

    Optional<FileType> findByIdOptional(String id);

    FileType findByFileTypeId(String id);

    Optional<FileType> findByFileTypeIdOptional(String id);
}
