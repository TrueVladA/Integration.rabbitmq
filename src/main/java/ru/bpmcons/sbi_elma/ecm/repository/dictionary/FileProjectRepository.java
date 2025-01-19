package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.FileProject;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

public interface FileProjectRepository extends DictionaryRepository<FileProject> {
    FileProject findById(String id);

    FileProject findBySysName(String name);
}
