package ru.bpmcons.sbi_elma.utils;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SerializeAsArray.Serializer.class)
@JsonDeserialize(using = SerializeAsArray.Deserializer.class)
public @interface SerializeAsArray {

    class Serializer extends StdSerializer<String> {
        protected Serializer() {
            super(String.class);
        }

        @Override
        public void serialize(String t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (t == null) {
                jsonGenerator.writeNull();
                return;
            }

            jsonGenerator.writeStartArray();
            var serializer = serializerProvider.findTypedValueSerializer(this.handledType(), true, null);
            serializer.serialize(t, jsonGenerator, serializerProvider);
            jsonGenerator.writeEndArray();
        }
    }


    class Deserializer extends StdDeserializer<String> {
        public Deserializer() {
            super(String.class);
        }

        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            TreeNode tree = jsonParser.getCodec().readTree(jsonParser);
            if (tree instanceof NullNode) {
                return null;
            }
            if (tree.isArray()) {
                if (tree.size() == 0) {
                    return null;
                }
                TreeNode treeNode = tree.get(0);
                if (treeNode instanceof NullNode) {
                    return null;
                }
                return ((TextNode) treeNode).asText();
            }
            return tree.asToken().asString();
        }
    }
}
