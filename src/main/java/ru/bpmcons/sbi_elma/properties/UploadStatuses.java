package ru.bpmcons.sbi_elma.properties;

import java.util.Map;

public class UploadStatuses {

    private static final Map<String, String> statusesForRequest = Map.of("Загружается", "uploading",
            "Загружен", "uploaded",
            "Не загружен", "not_uploaded",
            "Ошибка", "error");

    private static final Map<Integer, String> statusesForResponse = Map.of(1, "uploading",
            2, "uploaded",
            3, "not_uploaded",
            4, "error");

    public static String getCode(String status) {
        return statusesForRequest.get(status);

    }

    public static String getStatus(String code) {
        return statusesForRequest.entrySet().stream().filter(entry -> entry.getValue().equals(code)).findFirst().get().getKey();
    }

    public static String getStatus(int code) {
        return statusesForResponse.get(code);
    }
}
