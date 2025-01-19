package ru.bpmcons.sbi_elma.models.dto.generralized;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.infra.version.Since;
import ru.bpmcons.sbi_elma.utils.HideFromLogs;
import ru.bpmcons.sbi_elma.validation.ValidMD5Hash;
import ru.bpmcons.sbi_elma.validation.ValidPerecoderObject;

import javax.validation.Valid;
import java.util.Date;

@Data
@Valid
public class FileMetadata {
    private String id_ecm_filemetadata;
    private String id_as_filemetadata;
    private String app_id;
    private String file_name;
    @ValidPerecoderObject
    private PerecoderObject file_type;
//    private String file_type;
    private String categories;              // Структурированная запись. категория 1/категория 2 и тд
    private Date create_date;
    private Date update_date;
    private String version_number;
    private boolean current_version;
    private boolean medical_doc;
    private String url_as;
    @HideFromLogs
    private String url_file;
    private String url_preview;
    @HideFromLogs
    private String token_file;
    @HideFromLogs
    private String token_preview;
    private String doc_size;
    private String upload_status;
    @ValidMD5Hash(since = @Since(major = 1, minor = 1, patch = 20))
    private String crc;
    private Signature esign;
    @Nullable
    private String project;

    @Getter(onMethod = @__(@Since(major = 1, minor = 1, patch = 21)))
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Since(major = 1, minor = 1, patch = 21)
    private boolean archive;
}
