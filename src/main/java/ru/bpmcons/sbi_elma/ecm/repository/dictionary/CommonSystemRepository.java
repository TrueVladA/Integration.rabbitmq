package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.ecm.dto.dict.CommonSystem;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

import java.util.List;
import java.util.Optional;

public interface CommonSystemRepository extends DictionaryRepository<CommonSystem> {
    CommonSystem findById(String id);

    Optional<CommonSystem> findByIdOptional(@Nullable String id);

    List<CommonSystem> findAll();
}
