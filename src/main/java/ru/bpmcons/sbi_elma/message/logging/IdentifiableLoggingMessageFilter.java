package ru.bpmcons.sbi_elma.message.logging;

import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;

public class IdentifiableLoggingMessageFilter implements MessageFilter<Object, Object> {
    @Override
    public Object filter(Object message) {
        if (message instanceof Identifiable) {
            var docType = ((Identifiable) message).getDocType();
            if (docType != null && docType.valid()) {
                LoggerContextLayout.set("docType", docType.getSingleValue());
            }

            var contractType = ((Identifiable) message).getContractType();
            if (contractType != null && contractType.valid()) {
                LoggerContextLayout.set("contractType", contractType.getSingleValue());
            }

            LoggerContextLayout.set("ecmId", ((Identifiable) message).getEcmId());
        }
        return message;
    }
}
