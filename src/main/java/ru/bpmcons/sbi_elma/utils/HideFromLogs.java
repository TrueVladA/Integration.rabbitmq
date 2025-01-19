package ru.bpmcons.sbi_elma.utils;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

@Target(value = { ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonFilter("hideFromLogs")
public @interface HideFromLogs {
    class Filter extends SimpleBeanPropertyFilter {
        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider provider, BeanPropertyWriter writer) throws Exception {
            if (!writer.getMember().hasAnnotation(HideFromLogs.class)) {
                writer.serializeAsField(bean, jgen, provider);
            } else if (!jgen.canOmitFields()) { // since 2.3
                writer.serializeAsOmittedField(bean, jgen, provider);
            }
        }

        @Override
        public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
            if (!writer.getMember().hasAnnotation(HideFromLogs.class)) {
                writer.serializeAsField(pojo, jgen, provider);
            } else if (!jgen.canOmitFields()) { // since 2.3
                writer.serializeAsOmittedField(pojo, jgen, provider);
            }
        }
    }

    class AnnotationIntrospector extends JacksonAnnotationIntrospector {
        @Override
        public Object findFilterId(Annotated a) {
            Object filterId = super.findFilterId(a);
            if (filterId != null) {
                return filterId;
            }
            if (a instanceof AnnotatedClass) {
                for (Field field : ((AnnotatedClass) a).getAnnotated().getDeclaredFields()) {
                    if (field.isAnnotationPresent(HideFromLogs.class)) {
                        return "hideFromLogs";
                    }
                }
            }
            return null;
        }
    }
}
