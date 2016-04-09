package net.techcable.event4j;

import lombok.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

public final class MHEventExecutor<E, L> implements EventExecutor<E,L> {
    protected final MethodHandle methodHandle;

    @SneakyThrows(IllegalAccessException.class) // Wont happen
    public MHEventExecutor(EventBus<E, L> eventBus, Method method) {
        RegisteredListener.validate(Objects.requireNonNull(eventBus, "Null eventBus"), Objects.requireNonNull(method, "Null method"));
        method.setAccessible(true);
        this.methodHandle = MethodHandles.lookup().unreflect(method);
    }

    @Override
    @SneakyThrows
    public void fire(L listener, E event) {
        methodHandle.invoke(listener, event);
    }

}
