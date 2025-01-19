package ru.bpmcons.sbi_elma.message;

import lombok.Data;
import lombok.experimental.UtilityClass;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.feature.FeatureFlags;
import ru.bpmcons.sbi_elma.infra.version.Version;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Хранилище свойств текущего сообщения кролика. Основано на ThreadLocal, т.е. в параллельных задачах работать не будет.
 * Лучше стараться не использовать.
 */
@UtilityClass
public class MessagePropertiesHolder {
    private static final ThreadLocal<MessageProperties> ctx = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<ParsedProperties> parsedCtx = ThreadLocal.withInitial(() -> null);

    /**
     * Получить свойста текущего сообщения кролика
     * @return свойста текущего сообщения кролика
     * @throws IllegalStateException если контекст не инициализирован
     */
    @NonNull
    public static MessageProperties getMessageProperties() {
        MessageProperties properties = ctx.get();
        if (properties == null) {
            throw new IllegalStateException("Message properties not initialized");
        }
        return properties;
    }

    public static void setProperties(@NonNull MessageProperties context) {
        ctx.set(context);
    }

    public static void resetProperties() {
        ctx.remove();
    }

    @Nullable
    public static Version getVersion() {
        ParsedProperties properties = parsedCtx.get();
        if (properties == null) {
            throw new IllegalStateException("Parsed properties not initialized");
        }
        return properties.getVersion();
    }

    @NonNull
    public static Version getRequiredVersion() {
        Version version = getVersion();
        if (version == null) {
            return new Version(0, 0, 0);
        }
        return version;
    }

    @NonNull
    public static boolean checkVersion(Function<Version, Boolean> checker) {
        Version version = getVersion();
        if (version == null) {
            return false;
        }
        return checker.apply(version);
    }

    @NonNull
    public static String getMethodName() {
        ParsedProperties properties = parsedCtx.get();
        if (properties == null) {
            throw new IllegalStateException("Parsed properties not initialized");
        }
        return properties.getMethod();
    }

    @NonNull
    public static Set<FeatureFlags> getFeatureFlags() {
        ParsedProperties properties = parsedCtx.get();
        if (properties == null) {
            throw new IllegalStateException("Parsed properties not initialized");
        }
        return properties.getFlags();
    }

    public static void setParsedProperties(@Nullable Version version, @NonNull String method, @NonNull List<FeatureFlags> flags) {
        parsedCtx.set(new ParsedProperties(version, method, new HashSet<>(flags)));
    }

    public static void resetParsedProperties() {
        parsedCtx.remove();
    }

    @Data
    private static class ParsedProperties {
        private final Version version;
        private final String method;
        private final Set<FeatureFlags> flags;
    }
}
