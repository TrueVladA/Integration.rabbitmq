package ru.bpmcons.sbi_elma.s3;

import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.ecm.dto.dict.FileType;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class S3IdentityDocFileMetadata extends S3FileMetadata {

    public S3IdentityDocFileMetadata(FileType fileType, OperationName operation, String docId, String fileId) {
        super(fileType, null, null, operation, docId, fileId);
    }

    public Map<String, String> toAttributes() {
        Map<String, String> ret = super.toAttributes();
        ret.put("DUL", "true");
        return ret;
    }
}
