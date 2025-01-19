package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.Product;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;

import java.util.Optional;

public interface ProductRepository extends DictionaryRepository<Product> {
    Optional<Product> findByCode(String code);

    default Optional<Product> findByCode(PerecoderObject object) {
        return findByCode(object.getSingleValue());
    }
}
