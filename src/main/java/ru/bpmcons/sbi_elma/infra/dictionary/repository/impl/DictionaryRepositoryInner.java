package ru.bpmcons.sbi_elma.infra.dictionary.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import ru.bpmcons.sbi_elma.elma.ElmaClient;
import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListRequest;
import ru.bpmcons.sbi_elma.exceptions.WrongSysNameException;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.metadata.MethodMetadata;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.metadata.RepositoryClassMetadata;
import ru.bpmcons.sbi_elma.infra.dictionary.stats.DictionaryStatsProvider;
import ru.bpmcons.sbi_elma.properties.EcmProperties;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DictionaryRepositoryInner implements DictionaryStatsProvider {
    private final RepositoryClassMetadata metadata;
    private final ElmaClient elmaClient;
    private final EcmProperties ecmProperties;
    private final AtomicReference<State> state = new AtomicReference<>();

    @Override
    public String getName() {
        return metadata.getDisplayName();
    }

    @Override
    public int getCount() {
        return state.get().data.size();
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    @Scheduled(fixedDelayString = "3600000", initialDelay = 1800000L)
    public void loadData() {
        List<Object> references = (List<Object>) elmaClient.listAll(
                ecmProperties.getPathToReferences(),
                metadata.getDictName(),
                metadata.getEntity(),
                (current, step, total) -> new ElmaListRequest()
        );
        state.set(new State(
                references,
                metadata.getMethods()
                        .values()
                        .stream()
                        .filter(methodMetadata -> methodMetadata.getReturnType() != MethodMetadata.ReturnType.MULTIPLE)
                        .filter(filterItems -> !filterItems.getFilter().isEmpty())
                        .distinct()
                        .collect(Collectors.<MethodMetadata, List<MethodMetadata.FilterItem>, Map<Object, Object>>toMap(
                                MethodMetadata::getFilter,
                                meta -> references.stream()
                                        .collect(Collectors.toMap(
                                                object -> buildKey(meta, object),
                                                object -> object
                                        )),
                                (a, b) -> a
                        ))
        ));
    }

    public Object find(Method method, Object... args) {
        MethodMetadata meta = metadata.getMethods().get(method);
        if (meta.getReturnType() == MethodMetadata.ReturnType.MULTIPLE) {
            if (meta.getFilter().isEmpty()) {
                return Collections.unmodifiableList(state.get().data);
            } else {
                Object key = buildArgsKey(meta, args);
                return state.get().data
                        .stream()
                        .filter(o -> buildKey(meta, o) == key)
                        .collect(Collectors.toList());
            }
        } else {
            Object key = buildArgsKey(meta, args);
            Object value = state.get().filtered.get(meta.getFilter()).get(key);
            if (meta.getReturnType() == MethodMetadata.ReturnType.SINGLE) {
                if (value == null) {
                    throw new WrongSysNameException("Значение " + key
                            + " в справочнике " + "\"" + metadata.getDisplayName() + "\""
                            + " не существует");
                } else {
                    return value;
                }
            } else {
                return Optional.ofNullable(value);
            }
        }
    }

    private Object buildKey(MethodMetadata meta, Object obj) {
        if (meta.getFilter().size() == 1) {
            return meta.getNormalizer().normalize((String) meta.getFilter().get(0).getValue(obj));
        } else {
            return meta.getFilter()
                    .stream()
                    .map(filterItem -> filterItem.getValue(obj))
                    .map(o -> meta.getNormalizer().normalize((String) o))
                    .collect(Collectors.toList());
        }
    }

    private Object buildArgsKey(MethodMetadata meta, Object... args) {
        if (args.length == 1) {
            return meta.getNormalizer().normalize((String) args[0]);
        } else {
            return Arrays.stream(args)
                    .map(o -> meta.getNormalizer().normalize((String) o))
                    .collect(Collectors.toList());
        }
    }

    @RequiredArgsConstructor
    private static class State {
        private final List<Object> data;
        private final Map<List<MethodMetadata.FilterItem>, Map<Object, Object>> filtered;
    }
}
