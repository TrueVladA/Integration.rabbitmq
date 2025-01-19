package ru.bpmcons.sbi_elma.ecm.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Role {
    @SerializeAsArray
    private String source;
    @SerializeAsArray
    @JsonProperty("contract_type")
    private String contractType;
    @SerializeAsArray
    @JsonProperty("doc_type")
    private String docType;
    @SerializeAsArray
    @JsonProperty("file_type")
    private String fileType;
    private String fos;
    private boolean create;
    private boolean read;
    private boolean update;
    private boolean delete;
    private boolean full_access;

    public void processFullAccess( ) {
        create = create | full_access;
        read = read | full_access;
        update = update | full_access;
        delete = delete | full_access;
    }

    public boolean supports(OperationName name) {
        return switch (name) {
            case UPDATE -> update;
            case CREATE -> create;
            case READ -> read;
            case DELETE -> delete;
        };
    }

    public List<String> parseFos() {
        return Arrays.stream(fos.split(";"))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
