package ru.bpmcons.sbi_elma;

import ch.qos.logback.contrib.json.JsonFormatter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Setter
@Getter
public class JsonFormatLogger implements JsonFormatter {
    private ObjectMapper objectMapper = new ObjectMapper();
    private boolean prettyPrint = false;

    @Override
    public String toJsonString(Map var1) throws IOException {
        StringWriter writer = new StringWriter(512);

        JsonGenerator generator = objectMapper.getFactory().createGenerator(writer);
        if (this.isPrettyPrint()) {
            generator.useDefaultPrettyPrinter();
        }

        objectMapper.writeValue(generator, var1);
        writer.flush();
        return writer.toString().replace("\\", "");
    }
}
