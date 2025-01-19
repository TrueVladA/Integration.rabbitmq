package ru.bpmcons.sbi_elma.s3;

import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.FileType;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;

import java.util.HashMap;
import java.util.Map;

@Data
public class S3FileMetadata implements S3Metadata {
    @NonNull
    private final FileType fileType;
    @Nullable
    private final DocType docType;
    @Nullable
    private final ContractType contractType;
    private final OperationName operation;
    private final String docId;
    private final String fileId;

    @Override
    public Map<String, String> toAttributes() {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("FileType", fileType.getId());
        if (contractType != null) {
            ret.put("ContractType", contractType.getId());
        } else if (docType != null) {
            ret.put("DocType", docType.getId());
        }
        ret.put("FileID", fileId);
        ret.put("DocID", docId);
        ret.put("Op", operation.name());
        return ret;
    }
}
