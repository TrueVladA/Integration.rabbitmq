package ru.bpmcons.sbi_elma.message.filter;

import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.message.RequestTimingTracker;

/**
 * Фильтр, управляющий {@link RequestTimingTracker}
 * @param <T>
 */
public class RequestTimingTrackerMessageFilter<T> implements MessageFilter<T, T> {
    @Override
    public T filter(T message) {
        RequestTimingTracker.start();
        return message;
    }

    @Override
    public void cleanupAfterHandle() {
        RequestTimingTracker.reset();
    }
}
