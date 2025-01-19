package ru.bpmcons.sbi_elma.infra.method;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.infra.message.MessageErrorHandler;
import ru.bpmcons.sbi_elma.infra.method.exception.MethodNotFoundException;
import ru.bpmcons.sbi_elma.infra.version.Version;

import java.util.*;
import java.util.function.Consumer;

@Service
public class MethodRegistry {
    private final Map<String, SortedSet<MethodContainer>> containers = new HashMap<>();

    void registerMethod(@NonNull String name, @NonNull MethodContainer container) {
        containers.computeIfAbsent(
                name.toLowerCase(Locale.ROOT),
                s -> new TreeSet<MethodContainer>().descendingSet()
        ).add(container);
    }

    private MethodContainer getContainer(String method, Version version) {
        return containers.get(method.toLowerCase(Locale.ROOT))
                .stream()
                .filter(c -> c.isApplicable(version))
                .findFirst()
                .orElseThrow(() -> new MethodNotFoundException(method));
    }

    public Class<?> getArgumentType(@NonNull String method, @Nullable Version version) {
        return getContainer(method, version).getArgumentType();
    }

    public void invoke(String method, Version version, Object argument, Consumer<Object> responseConsumer, MessageErrorHandler errorHandler) {
        getContainer(method, version).handle(argument, responseConsumer, errorHandler);
    }
}
