package ru.bpmcons.sbi_elma.infra.dictionary.normalize;

import java.util.Locale;

/**
 * Заменяет все заглавные буквы ключа строчными
 */
public class LowercaseDictionaryKeyNormalizer implements DictionaryKeyNormalizer {
    @Override
    public String normalize(String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
