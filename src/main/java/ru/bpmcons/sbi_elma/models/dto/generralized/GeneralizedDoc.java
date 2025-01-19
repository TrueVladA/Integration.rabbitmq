/**
 * Copyright 2022 Practice BPM
 *
 */

package ru.bpmcons.sbi_elma.models.dto.generralized;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.infra.version.Since;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;
import ru.bpmcons.sbi_elma.models.dto.jwt.JwtToken;
import ru.bpmcons.sbi_elma.models.dto.responseMq.CodeMessage;
import ru.bpmcons.sbi_elma.serialization.StringDeserialization;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeneralizedDoc extends CodeMessage implements Identifiable {
    private String rquid;
    private JwtToken jwt_token;
    @JsonDeserialize(using = StringDeserialization.class)
    private String id_ecm_doc;                  // ID документа в ecm
    @JsonDeserialize(using = StringDeserialization.class)
    private String id_as_doc;                        // ID документа / договора в АС
    @JsonDeserialize(using = StringDeserialization.class)
    private String app_id;
    @JsonDeserialize(using = StringDeserialization.class)
    private String app_sysname;
    @JsonDeserialize(using = StringDeserialization.class)
    private String doc_series;
    @JsonDeserialize(using = StringDeserialization.class)
    private String doc_number;                  // Номер документа
    @JsonDeserialize(using = StringDeserialization.class)
    private String doc_full_number;
    @JsonDeserialize(using = StringDeserialization.class)
    private String contract_series;
    @JsonDeserialize(using = StringDeserialization.class)
    private String contract_number;          // Номер договора
    @JsonDeserialize(using = StringDeserialization.class)
    private String contract_full_number;
    private Date doc_date;                   // Дата документа
    private Date contract_date;              // Дата договора
    private Date contract_start_date;        // Дата начала действия
    private Date contract_end_date;          // Дата окончания действия
    private PerecoderObject doc_type;
    private PerecoderObject contract_type;
    @JsonDeserialize(using = StringDeserialization.class)
    private String doc_name;                 // Наименование докумнента в АС
    @JsonDeserialize(using = StringDeserialization.class)
    private String deal;                     // Дело
    private Creator_editor creator;                   // автор документа
    private Creator_editor editor;
    @JsonDeserialize(using = StringDeserialization.class)
    private String status;                   // статус документа
    private InputParentDoc parent_doc;            // родительский документ
    private DocParty[] doc_parties;          // Участники документа
    private PerecoderObject insurance_product;
    private boolean medical_doc;
    private FileMetadata[] file_metadata;              // Метаданные файлов

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

    @Override
    @JsonIgnore
    public PerecoderObject getDocType() {
        return doc_type;
    }

    @Override
    @JsonIgnore
    public PerecoderObject getContractType() {
        return contract_type;
    }

    @Override
    @JsonIgnore
    public String getEcmId() {
        return id_ecm_doc;
    }
}













