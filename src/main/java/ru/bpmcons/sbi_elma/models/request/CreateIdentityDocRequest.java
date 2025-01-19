package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.models.ability.FileMetadataContainer;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.validation.ValidPerecoderObject;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@Valid
@EqualsAndHashCode(callSuper = true)
public class CreateIdentityDocRequest extends AuthenticatedRequestBase implements FileMetadataContainer {
    @JsonProperty("id_as_doc")
    private String asId;
    @JsonProperty("code_identitydoc")
    @ValidPerecoderObject
    private PerecoderObject code;
    @JsonProperty("series")
    private String series;       // Серия ДУЛ
    @JsonProperty("number")
    @NotBlank
    private String number;       // Номер ДУЛ
    @JsonProperty("full_number")
    private String fullNumber;
    @NotNull
    @JsonProperty("issue_date")
    private Date issueDate;     // Дата выдачи ДУЛ. UTC+3
    @JsonProperty("end_date")
    private Date endDate;
    @NotBlank
    private String fio;
    private String issued;
    @Valid
    @JsonProperty("file_metadata")
    private List<FileMetadata> fileMetadata;
}
