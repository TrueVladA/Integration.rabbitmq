package ru.bpmcons.sbi_elma.message;

import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;

/**
 * Трекер начала обработки запроса. Основано на ThreadLocal, т.е. в параллельных задачах работать не будет.
 */
@UtilityClass
public class RequestTimingTracker {
    private static final ThreadLocal<ZonedDateTime> requestStart = ThreadLocal.withInitial(() -> null);

    /**
     * Получить время начала обработки сообщения
     * @return время начала обработки сообщения
     */
    @NonNull
    public static ZonedDateTime getStartTime() {
        ZonedDateTime startTime = requestStart.get();
        if (startTime == null) {
            throw new IllegalStateException("Message properties not initialized");
        }
        return startTime;
    }

    public static void start() {
        requestStart.set(ZonedDateTime.now());
    }

    public static void reset() {
        requestStart.remove();
    }
}
