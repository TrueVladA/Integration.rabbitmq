package ru.bpmcons.sbi_elma.models.dto.fileMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.FileStatusRepository;
import ru.bpmcons.sbi_elma.models.dto.doc.Status;
import ru.bpmcons.sbi_elma.properties.UploadStatuses;
import ru.bpmcons.sbi_elma.utils.HideFromLogs;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class RequestFileMetadataContext {
    private String id_as;
    private String file_name;
    private String categories;
    private Date create_date;
    private Date change_date;
    private String version_number;
    private boolean current_version;
    private boolean medical_doc;
    private String url_as;
    @HideFromLogs
    private String url_file;
    @HideFromLogs
    private String url_preview;
    private String file_size;
    private String crc;
    private String[] file;
    private String[] preview;
    private boolean archive;
    private String __id;
    private Date __updatedAt;
    private String[] file_type;
    private String[] source;
    private Status __status;
    @SerializeAsArray
    private String project;
    private FileMetadataVersionTable versions;

    public FileMetadataVersion toVersion(int number, FileStatusRepository repository) {
        FileMetadataVersion ver = new FileMetadataVersion();
        ver.setVer(number);
        ver.setId_as(id_as);
        ver.setFile_name(file_name);
        ver.setCategories(categories);
        ver.setCreate_date(new Date());
        ver.setChange_date(new Date());
        ver.setVersion_number(version_number);
        ver.setUrl_as(url_as);
        ver.setUrl_file(url_file);
        ver.setUrl_preview(url_preview);
        ver.setFile_size(file_size);
        ver.setCrc(crc);
        ver.setFile(file == null ? null : file[0]);
        ver.setPreview(preview == null ? null : preview[0]);
        ver.setArchive(archive);
        ver.setSource(source[0]);
        ver.setStatus(repository.findBySysName(UploadStatuses.getStatus(__status == null ? 1 : __status.getStatus())).getId());
        ver.setProject(project);
        return ver;
    }
}
