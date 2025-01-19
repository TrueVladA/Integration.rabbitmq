package ru.bpmcons.sbi_elma.infra.dictionary.repository;

/**
 * Репозиторий словарей. Синтаксис методов - урезанный Spring Data.
 * 1. Методы начинаются с <code>find</code>
 * 2. Если дальше идёт <code>All</code> - то возвращается {@link java.util.List}
 * 3. Если дальше идёт <code>By</code>, то после него ожидается фильтр
 * 4. Фильтр - название поля в геттере. Т.е. поле test, геттер getTest, фильтр Test.
 * 5. And - разделяет фильтры, и ставит между ними условие &&.
 * 6. Метод может заканчиваться на Optional, этот суффикс обрезается
 *
 * Если метод default, он игнорируется.
 * @param <T> тип словаря
 */
public interface DictionaryRepository<T> { }
