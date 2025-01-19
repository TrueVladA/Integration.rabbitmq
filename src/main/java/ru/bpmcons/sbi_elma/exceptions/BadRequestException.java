package ru.bpmcons.sbi_elma.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

@Getter
public class BadRequestException extends RuntimeException {
    private String body;
    private MultiValueMap<String, String> headers;
    private HttpStatus httpStatus;


    public BadRequestException(String body, MultiValueMap<String, String> headers, HttpStatus httpStatus) {
        this.body = body;
        this.headers = headers;
        this.httpStatus = httpStatus;
    }
}
