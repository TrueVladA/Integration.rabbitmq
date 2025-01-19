package ru.bpmcons.sbi_elma.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class JsonAwarePropertyNodeNameProvider implements PropertyNodeNameProvider {
    @Override
    public String getName(Property property) {
        if (property instanceof JavaBeanProperty) {
            Class<?> cls = ((JavaBeanProperty) property).getDeclaringClass();
            Field fld = ReflectionUtils.findField(cls, property.getName());
            if (fld != null) {
                if (fld.isAnnotationPresent(JsonProperty.class)) {
                    JsonProperty annotation = fld.getAnnotation(JsonProperty.class);
                    return annotation.value();
                }
            }
        }
        return property.getName();
    }
}
