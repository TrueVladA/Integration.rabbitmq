package ru.bpmcons.sbi_elma.exceptions;

import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public abstract class EcmDocumentAwareException extends ServiceResponseException {
    @Nullable
    private final String ecmId;
    @Nullable
    private final String asId;

    protected EcmDocumentAwareException(int code, String message, @Nullable String ecmId, @Nullable String asId) {
        super(code, message);
        this.ecmId = ecmId;
        this.asId = asId;
    }
}
