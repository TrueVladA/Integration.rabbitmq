package ru.bpmcons.sbi_elma.ecm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.document.*;
import ru.bpmcons.sbi_elma.ecm.exception.DocTypeNotSupportedException;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;

@Service
@RequiredArgsConstructor
public class EcmDocumentResolver {
    private final SysNamesConstants sysNamesConstants;

    public Object newInstance(String docType) {
        if (docType.equals(sysNamesConstants.getExtract()) ||
                docType.equals(sysNamesConstants.getReference()) ||
                docType.equals(sysNamesConstants.getResolution())) {
            return new ExtractDocument();
        } else if (docType.equals(sysNamesConstants.getLawsuit())) {
            return new LawsuitDocument();
        } else if (docType.equals(sysNamesConstants.getTask())) {
            return new TaskDocument();
        } else if (docType.equals(sysNamesConstants.getAgreement())) {
            return new AgreementDocument();
        } else if (docType.equals(sysNamesConstants.getSubrogarion()) ||
                docType.equals(sysNamesConstants.getDamage()) ||
                docType.equals(sysNamesConstants.getOther())) {
            return new DamageDocument();
        } else if (docType.equals(sysNamesConstants.getComplaint())) {
            return new ComplaintDocument();
        } else if (docType.equals(sysNamesConstants.getLetter())) {
            return new LetterDocument();
        } else if (docType.equals(sysNamesConstants.getContract())) {
            return new Contract();
        } else if (docType.equals(sysNamesConstants.getDamageOsage())) {
            return new OsagoDamageDocument();
        } else if (docType.equals(sysNamesConstants.getFinDoc())) {
            return new FinDocument();
        } else {
            throw new DocTypeNotSupportedException(docType);
        }
    }

    public Class<?> getClass(String docType) {
        if (docType.equals(sysNamesConstants.getExtract()) ||
                docType.equals(sysNamesConstants.getReference()) ||
                docType.equals(sysNamesConstants.getResolution())) {
            return ExtractDocument.class;
        } else if (docType.equals(sysNamesConstants.getLawsuit())) {
            return LawsuitDocument.class;
        } else if (docType.equals(sysNamesConstants.getTask())) {
            return TaskDocument.class;
        } else if (docType.equals(sysNamesConstants.getAgreement())) {
            return AgreementDocument.class;
        } else if (docType.equals(sysNamesConstants.getSubrogarion()) ||
                docType.equals(sysNamesConstants.getDamage()) ||
                docType.equals(sysNamesConstants.getOther())) {
            return DamageDocument.class;
        } else if (docType.equals(sysNamesConstants.getComplaint())) {
            return ComplaintDocument.class;
        } else if (docType.equals(sysNamesConstants.getLetter())) {
            return LetterDocument.class;
        } else if (docType.equals(sysNamesConstants.getContract())) {
            return Contract.class;
        } else if (docType.equals(sysNamesConstants.getDamageOsage())) {
            return OsagoDamageDocument.class;
        } else if (docType.equals(sysNamesConstants.getFinDoc())) {
            return FinDocument.class;
        } else {
            throw new DocTypeNotSupportedException(docType);
        }
    }
}
