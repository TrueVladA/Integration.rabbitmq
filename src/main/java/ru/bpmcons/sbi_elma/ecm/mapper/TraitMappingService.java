package ru.bpmcons.sbi_elma.ecm.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TraitMappingService {
    private final List<TraitMapper<?>> mappers;

    @NonNull
    public Object mapRequired(@NonNull GeneralizedDoc doc, @NonNull Object object, @Nullable Object existing) {
        for (TraitMapper<?> mapper : mappers) {
            if (mapper.isApplicable(object)) {
                //noinspection unchecked
                ((TraitMapper<Object>) mapper).mapRequired(doc, object, existing);
            }
        }
        return object;
    }

    @NonNull
    public Object mapRest(@NonNull GeneralizedDoc doc, @NonNull Object object, @Nullable Object existing) {
        for (TraitMapper<?> mapper : mappers) {
            if (mapper.isApplicable(object)) {
                //noinspection unchecked
                ((TraitMapper<Object>) mapper).mapRest(doc, object, existing);
            }
        }
        return object;
    }
}
