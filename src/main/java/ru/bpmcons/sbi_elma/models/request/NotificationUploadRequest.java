package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileUploadStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Valid
@EqualsAndHashCode(callSuper = true)
public class NotificationUploadRequest extends RequestBase {
    @JsonProperty("id_ecm_doc")
    private String ecmId;

    @NotEmpty
    @JsonProperty("notification_file")
    private List<FileNotification> files;

    @Data
    public static class FileNotification {
        @NotBlank
        @JsonProperty("id_ecm_filemetadata")
        private String ecmId;
        @NotNull
        @JsonProperty("upload_status")
        private FileUploadStatus uploadStatus;
    }
}
