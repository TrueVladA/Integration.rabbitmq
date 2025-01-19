package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;

public class FilesNotFoundException extends ServiceResponseException {
    public FilesNotFoundException(List<String> ids) {
        super(HttpStatus.CONFLICT.value(), "В запросе несуществующие файлы с id_ecm: " + ids.stream().reduce((s, s2) -> s + ", " + s2).orElse(""));
    }

    public FilesNotFoundException(String ecmId, List<String> ids) {
        super(HttpStatus.CONFLICT.value(), "Запрошено удаление файлов, не существующих в документе с id_ecm=" + ecmId + ". id_ecm файлов: " + ids.stream().reduce((s, s2) -> s + ", " + s2).orElse(""));
    }
}
