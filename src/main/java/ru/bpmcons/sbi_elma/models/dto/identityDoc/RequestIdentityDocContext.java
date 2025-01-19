package ru.bpmcons.sbi_elma.models.dto.identityDoc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.models.dto.ContextFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.DocWithFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.doc.Fio;

import java.util.Date;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@Data
public class RequestIdentityDocContext extends DocWithFileMetadata {
    private String[] type_identitydoc;
    private Fio fio;
    private String series;
    private String number;
    private String full_number;
    private String issued;
    private Date issue_date;
    private Date end_date;
    private boolean archive;
    private String id_as;
    private String __id;
    private String app_id;
    private String app_sysname;
    private String[] source;
}
