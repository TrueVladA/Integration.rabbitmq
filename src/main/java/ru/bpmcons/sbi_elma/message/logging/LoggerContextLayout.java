package ru.bpmcons.sbi_elma.message.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerContextLayout extends JsonLayout {
    private static final ThreadLocal<Map<String, String>> store = ThreadLocal.withInitial(ConcurrentHashMap::new);

    public static void set(@NonNull String key, @Nullable String value) {
        if (value == null) {
            store.get().remove(key);
        } else {
            store.get().put(key, value);
        }
    }

    public static void reset() {
        store.remove();
    }

    @Override
    protected void addCustomDataToJsonMap(Map<String, Object> map, ILoggingEvent iLoggingEvent) {
        map.putAll(store.get());
    }
}
