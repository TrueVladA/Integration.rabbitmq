package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.infra.version.Since;
import ru.bpmcons.sbi_elma.models.ability.ContractTyped;
import ru.bpmcons.sbi_elma.models.ability.DocumentTyped;
import ru.bpmcons.sbi_elma.models.ability.FileMetadataContainer;
import ru.bpmcons.sbi_elma.models.dto.generralized.*;
import ru.bpmcons.sbi_elma.serialization.StringDeserialization;
import ru.bpmcons.sbi_elma.validation.ValidPerecoderObject;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Valid
@GroupSequenceProvider(CreateDocRequest.ValidationProvider.class)
@EqualsAndHashCode(callSuper = true)
public class CreateDocRequest extends AuthenticatedRequestBase implements ContractTyped, DocumentTyped, FileMetadataContainer {
    @NotBlank
    @JsonDeserialize(using = StringDeserialization.class)
    @JsonProperty("id_as_doc")
    private String asId;                        // ID документа / договора в АС
    @JsonDeserialize(using = StringDeserialization.class)
    @JsonProperty("doc_series")
    private String docSeries;
    @JsonDeserialize(using = StringDeserialization.class)
    @JsonProperty("doc_number")
    @NotBlank(groups = DocumentValidation.class)
    private String docNumber;                  // Номер документа
    @JsonDeserialize(using = StringDeserialization.class)
    @JsonProperty("doc_full_number")
    @NotBlank(groups = DocumentValidation.class)
    private String docFullNumber;
    @JsonDeserialize(using = StringDeserialization.class)
    @JsonProperty("contract_series")
    private String contractSeries;
    @JsonDeserialize(using = StringDeserialization.class)
    @JsonProperty("contract_number")
    @NotBlank(groups = ContractValidation.class)
    private String contractNumber;          // Номер договора
    @JsonDeserialize(using = StringDeserialization.class)
    @JsonProperty("contract_full_number")
    @NotBlank(groups = ContractValidation.class)
    private String contractFullNumber;
    @JsonProperty("doc_date")
    @NotNull(groups = DocumentValidation.class)
    private Date docDate;                   // Дата документа
    @JsonProperty("contract_date")
    @NotNull(groups = ContractValidation.class)
    private Date contractDate;              // Дата договора
    @JsonProperty("contract_start_date")
    private Date contractStartDate;        // Дата начала действия
    @JsonProperty("contract_end_date")
    private Date contractEndDate;          // Дата окончания действия
    @JsonProperty("doc_type")
    @ValidPerecoderObject(groups = DocumentValidation.class)
    private PerecoderObject docType;
    @JsonProperty("contract_type")
    @ValidPerecoderObject(groups = ContractValidation.class)
    private PerecoderObject contractType;
    @JsonDeserialize(using = StringDeserialization.class)
    @JsonProperty("doc_name")
    private String docName;                 // Наименование докумнента в АС
    @JsonDeserialize(using = StringDeserialization.class)
    private String deal;                     // Дело
    @NotNull
    private Creator_editor creator;                   // автор документа
    @JsonDeserialize(using = StringDeserialization.class)
    private String status;                   // статус документа
    @JsonProperty("parent_doc")
    private InputParentDoc parentDoc;            // родительский документ
    @JsonProperty("doc_parties")
    private DocParty[] docParties;          // Участники документа
    @JsonProperty("insurance_product")
    private PerecoderObject insuranceProduct;
    @JsonProperty("medical_doc")
    private boolean medicalDoc;
    @Valid
    @JsonProperty("file_metadata")
    private List<FileMetadata> fileMetadata;              // Метаданные файлов

    @Since(major = 1, minor = 1, patch = 18)
    @Nullable
    @JsonProperty("comment")
    private String comment;
    @Since(major = 1, minor = 1, patch = 18)
    @Nullable
    @JsonProperty("currency_num")
    private Integer currencyNum;
    @Since(major = 1, minor = 1, patch = 18)
    @Nullable
    @JsonProperty("sum")
    private BigDecimal sum;
    @Since(major = 1, minor = 1, patch = 18)
    @Nullable
    @JsonProperty("payment_purpose")
    private String paymentPurpose;

    @Since(major = 1, minor = 1, patch = 19)
    @Nullable
    @JsonProperty("damage_dks")
    private Boolean damageDks;
    @Since(major = 1, minor = 1, patch = 19)
    @Nullable
    @JsonProperty("flow")
    private String flow;

    public GeneralizedDoc toGeneralizedDoc() {
        GeneralizedDoc doc = new GeneralizedDoc();
        ru.bpmcons.sbi_elma.models.dto.jwt.JwtToken jwtToken = new ru.bpmcons.sbi_elma.models.dto.jwt.JwtToken();
        jwtToken.setAccess_token(this.getJwtToken().getAccessToken());
        doc.setJwt_token(jwtToken);
        doc.setRquid(this.getRequestId());
        doc.setId_as_doc(asId);
        doc.setDoc_series(docSeries);
        doc.setDoc_number(docNumber);
        doc.setDoc_full_number(docFullNumber);
        doc.setContract_series(contractSeries);
        doc.setContract_number(contractNumber);
        doc.setContract_full_number(contractFullNumber);
        doc.setDoc_date(docDate);
        doc.setContract_date(contractDate);
        doc.setContract_start_date(contractStartDate);
        doc.setContract_end_date(contractEndDate);
        doc.setDoc_type(docType);
        doc.setContract_type(contractType);
        doc.setDoc_name(docName);
        doc.setDeal(deal);
        doc.setCreator(creator);
        doc.setStatus(status);
        doc.setParent_doc(parentDoc);
        doc.setDoc_parties(docParties);
        doc.setInsurance_product(insuranceProduct);
        doc.setMedical_doc(medicalDoc);
        doc.setFile_metadata(fileMetadata.toArray(FileMetadata[]::new));
        doc.setComment(comment);
        doc.setCurrencyNum(currencyNum);
        doc.setSum(sum);
        doc.setPaymentPurpose(paymentPurpose);
        doc.setDamageDks(damageDks);
        doc.setFlow(flow);
        return doc;
    }

    public interface DocumentValidation {}

    public interface ContractValidation {}

    public static final class ValidationProvider implements DefaultGroupSequenceProvider<CreateDocRequest> {

        @Override
        public List<Class<?>> getValidationGroups(CreateDocRequest object) {
            ArrayList<Class<?>> seq = new ArrayList<>();
            seq.add(CreateDocRequest.class);
            if (object == null) {
                return seq;
            }
            if (object.getDocType() == null || !object.getDocType().valid()) {
                seq.add(ContractValidation.class);
            } else {
                seq.add(DocumentValidation.class);
            }
            return seq;
        }
    }
}
