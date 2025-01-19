package ru.bpmcons.sbi_elma.elma;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.elma.dto.common.*;
import ru.bpmcons.sbi_elma.elma.exception.ElmaException;
import ru.bpmcons.sbi_elma.elma.exception.ElmaResponseException;
import ru.bpmcons.sbi_elma.elma.exception.ObjectMapperException;
import ru.bpmcons.sbi_elma.properties.EcmApiConst;
import ru.bpmcons.sbi_elma.properties.EcmProperties;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElmaClient {
    private static final int PAGE_MAX_SIZE = 100;
    private final PublicApiElmaService elma;
    private final EcmProperties properties;
    private final ObjectMapper objectMapper;

    public <T> ElmaListResponse.Result<T> listRaw(String url, Class<T> clz) {
        ResponseEntity<String> response = elma.doGet(url);
        if (response.getStatusCode().isError()) {
            throw new ElmaResponseException(response.getStatusCodeValue(), response.getBody());
        }
        try {
            ElmaListResponse<T> result = objectMapper.readerWithView(ElmaJsonView.View.class).readValue(
                    objectMapper.createParser(response.getBody()),
                    objectMapper.getTypeFactory().constructParametricType(ElmaListResponse.class, clz)
            );
            if (!result.isSuccess()) {
                throw new ElmaException(result.getError());
            }
            return result.getResult();
        } catch (IOException e) {
            throw new ObjectMapperException(e);
        }
    }

    public <T> ElmaListResponse.Result<T> list(String path, String code, Class<T> clz, ElmaListRequest req) {
        ResponseEntity<String> response = elma.doPost(
                serialize(req),
                path,
                code,
                EcmApiConst.LIST
        );
        if (response.getStatusCode().isError()) {
            throw new ElmaResponseException(response.getStatusCodeValue(), response.getBody());
        }
        try {
            ElmaListResponse<T> result = objectMapper.readerWithView(ElmaJsonView.View.class).readValue(
                    objectMapper.createParser(response.getBody()),
                    objectMapper.getTypeFactory().constructParametricType(ElmaListResponse.class, clz)
            );
            if (!result.isSuccess()) {
                throw new ElmaException(result.getError());
            }
            return result.getResult();
        } catch (IOException e) {
            throw new ObjectMapperException(e);
        }
    }

    public <T> List<T> list(String path, String code, Class<T> clz, int size, PaginatedRequestBuilder builder) {
        if (size == 0) {
            return Collections.emptyList();
        }
        List<T> lst = new ArrayList<>();
        for (int chunk = 0; chunk < Math.ceil(size / ((float) PAGE_MAX_SIZE)); chunk++) {
            lst.addAll(list(
                    path,
                    code,
                    clz,
                    builder.build(chunk, PAGE_MAX_SIZE, size)
                            .size(PAGE_MAX_SIZE)
                            .from(chunk * PAGE_MAX_SIZE)
            ).getResult());
        }
        return lst;
    }

    public <T> List<T> listAll(String path, String code, Class<T> clz, PaginatedRequestBuilder builder) {
        List<T> lst = new ArrayList<>();
        for (int chunk = 0;; chunk++) {
            List<T> list = list(
                    path,
                    code,
                    clz,
                    builder.build(chunk, PAGE_MAX_SIZE, -1)
                            .size(PAGE_MAX_SIZE)
                            .from(chunk * PAGE_MAX_SIZE)
            ).getResult();
            lst.addAll(list);
            if (list.size() < PAGE_MAX_SIZE) {
                break;
            }
        }
        return lst;
    }

    @NonNull
    public <T> T createReference(@NonNull String code, @NonNull T obj) {
        ResponseEntity<String> response = elma.doPost(
                serialize(new ElmaContextRequest<>(obj)),
                properties.getPathToReferences(),
                code,
                EcmApiConst.CREATE
        );
        if (response.getStatusCode().isError()) {
            throw new ElmaResponseException(response.getStatusCodeValue(), response.getBody());
        }
        try {
            ElmaItemResponse<T> result = objectMapper.readerWithView(ElmaJsonView.View.class).readValue(
                    objectMapper.createParser(response.getBody()),
                    objectMapper.getTypeFactory().constructParametricType(ElmaItemResponse.class, obj.getClass())
            );
            if (!result.isSuccess()) {
                throw new ElmaException(result.getError());
            }
            return result.getItem();
        } catch (IOException e) {
            throw new ObjectMapperException(e);
        }
    }

    @NonNull
    public <T> T createDocument(@NonNull String code, @NonNull T obj) {
        ResponseEntity<String> response = elma.doPost(
                serialize(new ElmaContextRequest<>(obj)),
                properties.getPathToDocuments(),
                code,
                EcmApiConst.CREATE
        );
        if (response.getStatusCode().isError()) {
            throw new ElmaResponseException(response.getStatusCodeValue(), response.getBody());
        }
        try {
            ElmaItemResponse<T> result = objectMapper.readerWithView(ElmaJsonView.View.class).readValue(
                    objectMapper.createParser(response.getBody()),
                    objectMapper.getTypeFactory().constructParametricType(ElmaItemResponse.class, obj.getClass())
            );
            if (!result.isSuccess()) {
                throw new ElmaException(result.getError());
            }
            return result.getItem();
        } catch (IOException e) {
            throw new ObjectMapperException(e);
        }
    }

    @NonNull
    public <T> T updateDocument(@NonNull String code, @NonNull String id, @NonNull T obj) {
        ResponseEntity<String> response = elma.doPost(
                serialize(new ElmaContextRequest<>(obj)),
                properties.getPathToDocuments(),
                code + "/" + id,
                EcmApiConst.UPDATE
        );
        if (response.getStatusCode().isError()) {
            throw new ElmaResponseException(response.getStatusCodeValue(), response.getBody());
        }
        try {
            ElmaItemResponse<T> result = objectMapper.readerWithView(ElmaJsonView.View.class).readValue(
                    objectMapper.createParser(response.getBody()),
                    objectMapper.getTypeFactory().constructParametricType(ElmaItemResponse.class, obj.getClass())
            );
            if (!result.isSuccess()) {
                throw new ElmaException(result.getError());
            }
            return result.getItem();
        } catch (IOException e) {
            throw new ObjectMapperException(e);
        }
    }

    @NonNull
    public void setDocumentStatus(@NonNull String code, @NonNull String id, @NonNull Object status) {
        ResponseEntity<String> response = elma.doPost(
                serialize(new ElmaSetStatusRequest<>(status)),
                properties.getPathToDocuments(),
                code + "/" + id,
                EcmApiConst.SETSTATUS
        );
        if (response.getStatusCode().isError()) {
            throw new ElmaResponseException(response.getStatusCodeValue(), response.getBody());
        }
    }

    @Nullable
    public <T > T getDocument(@NonNull String code, @NonNull String id, Class<T> clazz) {
        ResponseEntity<String> response = elma.doPost(
                "",
                properties.getPathToDocuments(),
                code + "/" + id,
                EcmApiConst.GET
        );
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return null;
        }
        try {
            ElmaItemResponse<T> result = objectMapper.readerWithView(ElmaJsonView.View.class).readValue(
                    objectMapper.createParser(response.getBody()),
                    objectMapper.getTypeFactory().constructParametricType(ElmaItemResponse.class, clazz)
            );
            if (!result.isSuccess()) {
                throw new ElmaException(result.getError());
            }
            return result.getItem();
        } catch (IOException e) {
            throw new ObjectMapperException(e);
        }
    }

    private String serialize(Object req) {
        try {
            return objectMapper.writerWithView(ElmaJsonView.View.class).writeValueAsString(req);
        } catch (JsonProcessingException e) {
            throw new ObjectMapperException(e);
        }
    }
}
