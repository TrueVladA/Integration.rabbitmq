package ru.bpmcons.sbi_elma.models.dto.responseMq;

import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignResponse extends CodeMessage {
    private List<Item> links;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Item {
        private String url;
        private Map<String, String> properties;
    }
}
