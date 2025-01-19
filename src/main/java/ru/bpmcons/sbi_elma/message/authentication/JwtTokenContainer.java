package ru.bpmcons.sbi_elma.message.authentication;

import ru.bpmcons.sbi_elma.models.request.AuthenticatedRequestBase;

public interface JwtTokenContainer {
    AuthenticatedRequestBase.JwtToken getJwtToken();
}
