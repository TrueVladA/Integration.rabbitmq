package ru.bpmcons.sbi_elma.infra.dictionary.stats;

import org.springframework.lang.NonNull;

/**
 * Компонент справочника, предоставляющий статистику по нему
 */
public interface DictionaryStatsProvider {
    /**
     * Имя словаря
     */
    @NonNull
    String getName();

    /**
     * Кол-во элементов в словаре
     */
    int getCount();
}
