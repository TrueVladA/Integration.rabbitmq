package ru.bpmcons.sbi_elma.message;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.security.SecurityContext;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class MessageWorkerService {
    @Qualifier("messageExecutorWorker")
    private final ForkJoinPool pool;

    public MessageWorkerService(@Qualifier("messageExecutorWorker") ForkJoinPool pool) {
        this.pool = pool;
    }

    public <T> T runInWorker(Callable<T> task) {
        try {
            SecurityContext currentCtx = SecurityContextHolder.getContext();
            return pool.submit(() -> {
                SecurityContextHolder.setContextNullable(currentCtx);
                try {
                    return task.call();
                } finally {
                    SecurityContextHolder.resetContext();
                }
            }).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() != null && e.getCause() instanceof RuntimeException) {
                throw ((RuntimeException) e.getCause());
            }
            throw new RuntimeException(e);
        }
    }

    public void runInWorker(Runnable task) {
        try {
            SecurityContext currentCtx = SecurityContextHolder.getContext();
            pool.submit(() -> {
                SecurityContextHolder.setContextNullable(currentCtx);
                try {
                    task.run();
                } finally {
                    SecurityContextHolder.resetContext();
                }
            }).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() != null && e.getCause() instanceof RuntimeException) {
                throw ((RuntimeException) e.getCause());
            }
            throw new RuntimeException(e);
        }
    }

    public <T> void forEachParallel(@Nullable Collection<T> list, Consumer<T> consumer) {
        if (list == null || list.isEmpty()) return;
        runInWorker(() -> list.stream().parallel().forEach(consumer));
    }

    public <T> void forEachParallel(@Nullable Stream<T> list, Consumer<T> consumer) {
        if (list == null) return;
        runInWorker(() -> list.parallel().forEach(consumer));
    }

    public <T> void forEachParallel(@Nullable T[] list, Consumer<T> consumer) {
        if (list == null || list.length == 0) return;
        runInWorker(() -> Arrays.stream(list).parallel().forEach(consumer));
    }
}
