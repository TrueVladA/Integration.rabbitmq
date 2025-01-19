package ru.bpmcons.sbi_elma.security.file.extensions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.bpmcons.sbi_elma.models.ability.FileMetadataContainer;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DeniedFileExtension;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DeniedFileExtensionRepository;
import ru.bpmcons.sbi_elma.exceptions.CheckRequiredParametersException;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CheckDeniedFileExtensionsAspectTest {
    private DeniedFileExtensionRepository repository;
    private CheckDeniedFileExtensionsAspect service;

    @BeforeEach
    public void init() {
        repository = mock(DeniedFileExtensionRepository.class);
        service = new CheckDeniedFileExtensionsAspect(repository);
    }

    @Test
    void shouldCheckMetadata() {
        setDeniedExtensions("exe", "com");

        assertThrows(CheckRequiredParametersException.class,
                () -> service.authorize(buildContainer(Map.of(
                    "a", "test.exe",
                    "b", "test.com",
                    "c", "test.jpg",
                    "d", "test",
                    "q", ".jpg",
                    "e", "."
                ))),
                "В документе присутствуют файлы с запрещённым расширением: a : 'exe', b : 'com', d : '<нет расширения>', e : '<нет расширения>'");
    }

    private void setDeniedExtensions(String... exts) {
        for (String ext : exts) {
            when(repository.findByFileExtension(ext)).thenReturn(Optional.of(new DeniedFileExtension()));
        }
        when(repository.findByFileExtension(anyString())).thenReturn(Optional.empty());
    }

    private FileMetadataContainer buildContainer(Map<String, String> files) {
        List<FileMetadata> list = files.entrySet().stream()
                .map(stringStringEntry -> {
                    FileMetadata metadata = new FileMetadata();
                    metadata.setId_as_filemetadata(stringStringEntry.getKey());
                    metadata.setFile_name(stringStringEntry.getValue());
                    return metadata;
                })
                .collect(Collectors.toList());
        return () -> list;
    }
}