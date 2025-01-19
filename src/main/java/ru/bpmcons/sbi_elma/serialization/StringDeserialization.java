package ru.bpmcons.sbi_elma.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StringDeserialization extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonToken type = jsonParser.currentToken();
        switch (type) {
            case VALUE_NULL:
                return null;
            case VALUE_STRING:
                String string = deserializationContext.readValue(jsonParser, String.class);
                if (string.isEmpty()) {
                    return null;
                } else {
                    return string;
                }
            default:
                throw new IllegalArgumentException();
        }
    }
}
