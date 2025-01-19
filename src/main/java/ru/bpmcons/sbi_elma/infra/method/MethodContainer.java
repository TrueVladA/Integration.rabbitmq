package ru.bpmcons.sbi_elma.infra.method;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import ru.bpmcons.sbi_elma.infra.message.MessageErrorHandler;
import ru.bpmcons.sbi_elma.infra.version.Since;
import ru.bpmcons.sbi_elma.infra.version.Until;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.infra.version.VersionRange;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class MethodContainer implements Comparable<MethodContainer> {
    private final Method method;
    private final Object bean;
    @Getter
    private final Class<?> argumentType;
    private final VersionRange versionRange;

    public MethodContainer(Method method, Object bean) {
        this.method = method;
        this.bean = bean;
        method.setAccessible(true);
        Set<Class<?>> args = Arrays.stream(method.getParameterTypes())
                .filter(aClass -> !AsyncResultHandler.class.isAssignableFrom(aClass))
                .collect(Collectors.toSet());
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Method " + method + " does not have any args");
        }
        if (args.size() > 1) {
            throw new IllegalArgumentException("Method " + method + " has multiple argument types");
        }
        this.argumentType = args.iterator().next();
        Since since = AnnotationUtils.findAnnotation(method, Since.class);
        Until until = AnnotationUtils.findAnnotation(method, Until.class);
        this.versionRange = new VersionRange(
                since == null ? null : new Version(since.major(), since.minor(), since.patch()),
                until == null ? null : new Version(until.major(), until.minor(), until.patch())
        );
    }

    public boolean isApplicable(@Nullable Version version) {
        return versionRange.contains(version);
    }

    public void handle(Object arg, Consumer<Object> resultHandler, MessageErrorHandler errorHandler) {
        Object[] args = new Object[method.getParameterCount()];
        AsyncResultHandlerImpl handler = null;
        for (int i = 0; i < method.getParameterCount(); i++) {
            Class<?> type = method.getParameterTypes()[i];
            if (type == AsyncResultHandler.class) {
                if (handler == null) {
                    handler = new AsyncResultHandlerImpl(resultHandler);
                }
                args[i] = handler;
            } else {
                args[i] = arg;
            }
        }

        Object response;
        try {
            response = ReflectionUtils.invokeMethod(method, bean, args);
        } catch (Exception e) {
            if (handler == null || !handler.handled) {
                errorHandler.handleException(e, arg);
            } else {
                errorHandler.logException(e, arg);
            }
            return;
        }

        if (handler == null) {
            if (response != null) {
                resultHandler.accept(response);
            }
        } else if (!handler.handled) {
            throw new IllegalStateException("Should use AsyncResultHandler to return result");
        }
    }

    @Override
    public int compareTo(MethodContainer o) {
        return versionRange.compareTo(o.versionRange);
    }

    @RequiredArgsConstructor
    private static final class AsyncResultHandlerImpl implements AsyncResultHandler {
        private final Consumer<Object> resultHandler;
        private boolean handled = false;

        @Override
        public void success(Object result) {
            if (handled) {
                throw new IllegalStateException("AsyncResultHandler can be invoked only once");
            }
            resultHandler.accept(result);
            handled = true;
        }
    }
}
