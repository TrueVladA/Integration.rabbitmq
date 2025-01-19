package ru.bpmcons.sbi_elma.ecm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.dict.CommonSystem;
import ru.bpmcons.sbi_elma.ecm.dto.dict.PartyRole;
import ru.bpmcons.sbi_elma.ecm.dto.dict.PartyType;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.*;
import ru.bpmcons.sbi_elma.models.dto.generralized.DictValues;
import ru.bpmcons.sbi_elma.models.dto.generralized.DocParty;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;
import ru.bpmcons.sbi_elma.service.SeparaterFio;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EcmPartiesConverter {
    private final SeparaterFio separaterFio;
    private final IdentityDocService identityDocService;
    private final OpfRepository opfRepository;
    private final VipRepository vipRepository;
    private final PartyRoleRepository partyRoleRepository;
    private final PartyTypeRepository partyTypeRepository;
    private final CommonSystemRepository commonSystemRepository;

    public ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties convert(DocParty[] parties) {
        String system = SecurityContextHolder.getRequiredContext().getSystem().getId();
        return new ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties(
                Arrays.stream(parties).map(party -> toEcm(party, system)).collect(Collectors.toList())
        );
    }

    private ru.bpmcons.sbi_elma.ecm.dto.reference.DocParty toEcm(DocParty party, String system) {
        ru.bpmcons.sbi_elma.ecm.dto.reference.DocParty ret = new ru.bpmcons.sbi_elma.ecm.dto.reference.DocParty();

        PartyType partyType = null;
        if (party.getType() != null && party.getType().valid()) {
            partyType = partyTypeRepository.findBySysName(party.getType());
            ret.setPartyType(partyType.getId());
        }

        if (party.getFio() != null && !party.getFio().isBlank() && partyType != null && !partyType.getSysName().equals("Legal")) {
            ret.setFio(separaterFio.separate(party.getFio()));
        }

        ret.setBirthdate(party.getBirthdate());
        ret.setDul(party.getIdentity_doc());
        ret.setFullName(party.getFullname());
        ret.setExternalId(party.getId_as_party());
        ret.setShortName(party.getShortname());
        ret.setInn(party.getINN());
        ret.setEmail(party.getEmail());

        if (party.getIdentity_doc_id() != null) {
            identityDocService.checkIdentityDocById(party.getIdentity_doc_id());
            ret.setIdentitydoc(party.getIdentity_doc_id());
            if (party.getVIP() != null && !party.getVIP().isEmpty()) {
                ret.setVip(vipRepository.findBySysNameAndSource(party.getVIP(), system).getId());
            }
        } else if (party.getIdentity_doc_obj() != null
                && party.getIdentity_doc_obj().getApp_id() != null
                && !party.getIdentity_doc_obj().getApp_id().isEmpty()) {
            CommonSystem commonSystem = commonSystemRepository.findById(party.getIdentity_doc_obj().getApp_id());
            String[] dulIfNotExists = identityDocService.createIdentityDoc(commonSystem,
                    party.getIdentity_doc_obj());
            ret.setIdentitydoc(dulIfNotExists[0]);

            if (party.getVIP() != null && !party.getVIP().isEmpty()) {
                ret.setVip(vipRepository.findBySysNameAndSource(party.getVIP(), commonSystem.getId()).getId());
            }
        }

        if (party.getOpf() != null && !party.getOpf().isEmpty()) {
            ret.setOpf(opfRepository.findByCode(party.getOpf()).getId());
        }

        if (party.getRole() != null && party.getRole().getDictValues().length > 0) {
            ret.setPartyRoles(
                    Arrays.stream(party.getRole().getDictValues())
                            .filter(Objects::nonNull)
                            .map(DictValues::getDictValue)
                            .filter(Objects::nonNull)
                            .map(dictValue -> partyRoleRepository.findBySysName(dictValue.getValue()))
                            .map(PartyRole::getId)
                            .collect(Collectors.toList())
            );
        }

        return ret;
    }
}
