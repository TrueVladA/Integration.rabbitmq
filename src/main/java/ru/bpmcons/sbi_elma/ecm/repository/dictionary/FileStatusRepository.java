package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.FileStatus;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

public interface FileStatusRepository extends DictionaryRepository<FileStatus> {
    FileStatus findBySysName(String sysName);

    FileStatus findById(String id);
}
