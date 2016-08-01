package net.techcable.event4j;

import lombok.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class ReflectionEventExecutor<E, L> implements EventExecutor<E, L> {
    private final Method method;

    public ReflectionEventExecutor(EventBus<E, L> eventBus, Method method) {
        RegisteredListener.validate(Objects.requireNonNull(eventBus, "Null eventBus"), Objects.requireNonNull(method, "Null method"));
        method.setAccessible(true);
        this.method = method;
    }

    @Override
    @SneakyThrows
    public void fire(L listener, E event) {
        try {
            method.invoke(listener, event);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}
