package ru.bpmcons.sbi_elma.security;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.CommonSystemRepository;

@Service
@RequiredArgsConstructor
public class MessageAuthenticator {
    private final CommonSystemRepository commonSystemRepository;

    public SecurityContext authenticate(MessageProperties properties) {
        return new SecurityContext(commonSystemRepository.findById(properties.getAppId()), null);
    }
}
