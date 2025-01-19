package ru.bpmcons.sbi_elma.ecm.repository;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.EcmService;
import ru.bpmcons.sbi_elma.ecm.dto.dict.FileType;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.dto.reference.Role;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.FileTypeRepository;
import ru.bpmcons.sbi_elma.infra.dictionary.stats.DictionaryStatsProvider;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleRepository implements DictionaryStatsProvider {
    private final EcmService ecmService;
    private final FileTypeRepository fileTypeRepository;
    private final AtomicReference<State> state = new AtomicReference<>();

    @PostConstruct
    @Scheduled(fixedDelayString = "3600000", initialDelay = 1800000L)
    public void load() {
        FileType docRef = fileTypeRepository.findByFileTypeId("*");
        List<Role> roles = ecmService.getActiveRoles();
        State newState = new State(roles, docRef);
        state.set(newState);
        log.info("Загружена РМД " + newState);
    }

    @Override
    public String getName() {
        return "Ролевая модель доступа к файлам";
    }

    @Override
    public int getCount() {
        return state.get().getRoles().size();
    }

    public Optional<Role> findContractFileRole(String contract, FileType fileType, List<String> roleList, OperationName operationName) {
        return Optional.ofNullable(state.get().getContractFileRoles().get(contract))
                .flatMap(map -> Optional.ofNullable(map.get(fileType.getId())))
                .flatMap(roles -> roleList.stream()
                        .filter(roles::containsKey)
                        .map(roles::get)
                        .filter(role -> role.supports(operationName))
                        .findAny()
                );
    }


    public Optional<Role> findDocFileRole(String doc, FileType fileType, List<String> roleList, OperationName operationName) {
        return Optional.ofNullable(state.get().getDocFileRoles().get(doc))
                .flatMap(map -> Optional.ofNullable(map.get(fileType.getId())))
                .flatMap(roles -> roleList.stream()
                        .filter(roles::containsKey)
                        .map(roles::get)
                        .filter(role -> role.supports(operationName))
                        .findAny()
                );
    }

    public Optional<Role> findDocRole(String doc, List<String> roleList, OperationName operationName) {
        return Optional.ofNullable(state.get().getDocRoles().get(doc))
                .flatMap(roles -> roleList.stream()
                        .filter(roles::containsKey)
                        .map(roles::get)
                        .filter(role -> role.supports(operationName))
                        .findAny()
                );
    }

    public Optional<Role> findContractRole(String contract, List<String> roleList, OperationName operationName) {
        return Optional.ofNullable(state.get().getContractRoles().get(contract))
                .flatMap(roles -> roleList.stream()
                        .filter(roles::containsKey)
                        .map(roles::get)
                        .filter(role -> role.supports(operationName))
                        .findAny()
                );
    }

    @Data
    private static final class State {
        private final List<Role> roles;
        private final Map<String, Map<String, Role>> docRoles; // doc id, fos -> role
        private final Map<String, Map<String, Role>> contractRoles; // doc id, fos -> role
        private final Map<String, Map<String, Map<String, Role>>> docFileRoles; // doc id, file type, fos -> role
        private final Map<String, Map<String, Map<String, Role>>> contractFileRoles; // doc id, file type, fos -> role

        public State(List<Role> roles, FileType docRef) {
            roles.forEach(Role::processFullAccess);
            this.roles = roles;
            this.docRoles = roles.stream()
                    .filter(role -> role.getContractType() == null)
                    .filter(role -> role.getFileType().equals(docRef.getId()))
                    .collect(Collectors.toUnmodifiableMap(
                            Role::getDocType,
                            role -> role.parseFos().stream().collect(Collectors.toMap(o -> o, o -> role)),
                            State::mergeMap
                    ));
            this.contractRoles = roles.stream()
                    .filter(role -> role.getContractType() != null)
                    .filter(role -> role.getFileType().equals(docRef.getId()))
                    .collect(Collectors.toUnmodifiableMap(
                            Role::getContractType,
                            role -> role.parseFos().stream().collect(Collectors.toMap(o -> o, o -> role)),
                            State::mergeMap
                    ));
            this.docFileRoles = roles.stream()
                    .filter(role -> role.getContractType() == null)
                    .filter(role -> !role.getFileType().equals(docRef.getId()))
                    .collect(Collectors.toUnmodifiableMap(
                            Role::getDocType,
                            role -> new HashMap<>(Map.of(role.getFileType(), role.parseFos().stream().collect(Collectors.toMap(o -> o, o -> role, State::mergeRoles)))),
                            State::mergeNestedMap
                    ));
            this.docFileRoles.forEach((docType, fileInfo) -> {
                Map<String, Role> r = this.docRoles.getOrDefault(docType, new HashMap<>());
                fileInfo.forEach((s, roleMap) -> {
                    roleMap.forEach((fos, role) -> {
                        var roleTarget = r.computeIfAbsent(fos, s1 -> role);
                        roleTarget.setRead(roleTarget.isRead() || role.isRead());
                        roleTarget.setCreate(roleTarget.isCreate() || role.isCreate());
                        roleTarget.setUpdate(roleTarget.isUpdate() || role.isUpdate());
                        roleTarget.setDelete(roleTarget.isDelete() || role.isDelete());
                    });
                });
            });
            this.contractFileRoles = roles.stream()
                    .filter(role -> role.getContractType() != null)
                    .filter(role -> !role.getFileType().equals(docRef.getId()))
                    .collect(Collectors.toUnmodifiableMap(
                            Role::getContractType,
                            role -> new HashMap<>(Map.of(role.getFileType(), role.parseFos().stream().collect(Collectors.toMap(o -> o, o -> role, State::mergeRoles)))),
                            State::mergeNestedMap
                    ));
            this.contractFileRoles.forEach((contractType, fileInfo) -> {
                Map<String, Role> r = this.contractRoles.getOrDefault(contractType, new HashMap<>());
                fileInfo.forEach((s, roleMap) -> {
                    roleMap.forEach((fos, role) -> {
                        var roleTarget = r.computeIfAbsent(fos, s1 -> role);
                        roleTarget.setRead(roleTarget.isRead() || role.isRead());
                        roleTarget.setCreate(roleTarget.isCreate() || role.isCreate());
                        roleTarget.setUpdate(roleTarget.isUpdate() || role.isUpdate());
                        roleTarget.setDelete(roleTarget.isDelete() || role.isDelete());
                    });
                });
            });
        }

        @Override
        public String toString() {
            return "State{" +
                    "roles=" + roles +
                    ", docRoles=" + docRoles +
                    ", contractRoles=" + contractRoles +
                    ", docFileRoles=" + docFileRoles +
                    ", contractFileRoles=" + contractFileRoles +
                    '}';
        }

        private static <K, V> Map<K, V> mergeMap(Map<K, V> a, Map<K, V> b) {
            a.putAll(b);
            return a;
        }

        private static <K, V, S> Map<K, Map<V, S>> mergeNestedMap(Map<K, Map<V, S>> a, Map<K, Map<V, S>> b) {
            b.forEach((k, map) -> {
                if (a.containsKey(k)) {
                    a.get(k).putAll(map);
                } else {
                    a.put(k, map);
                }
            });
            return a;
        }

        private static Role mergeRoles(Role a, Role b) {
            a.setRead(a.isRead() || b.isRead());
            a.setCreate(a.isCreate() || b.isCreate());
            a.setDelete(a.isDelete() || b.isDelete());
            a.setUpdate(a.isUpdate() || b.isUpdate());
            return a;
        }
    }
}
