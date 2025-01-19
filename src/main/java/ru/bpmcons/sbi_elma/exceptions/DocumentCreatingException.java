package ru.bpmcons.sbi_elma.exceptions;

public class DocumentCreatingException extends ServiceResponseException {
    public DocumentCreatingException() {
        super(424, "Этот документ уже создаётся");
    }
}
