package ru.bpmcons.sbi_elma.infra.dictionary.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Выводит статистику по загруженным справочникам после запуска
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryStatsPrinter {
    private final List<DictionaryStatsProvider> providers;

    @PostConstruct
    public void init() {
        log.info("Загружено справочников: " + providers.stream()
                .map(c -> c.getName() + ": " + c.getCount() + " элементов")
                .collect(Collectors.joining(", ")));
    }
}
