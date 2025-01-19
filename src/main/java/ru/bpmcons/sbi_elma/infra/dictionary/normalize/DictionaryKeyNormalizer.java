package ru.bpmcons.sbi_elma.infra.dictionary.normalize;

import org.springframework.lang.NonNull;

/**
 * Модифицирует ключ словаря. Например, приводит его к сточным буквам
 * @see LowercaseDictionaryKeyNormalizer
 */
public interface DictionaryKeyNormalizer {
    @NonNull
    String normalize(@NonNull String key);
}
