package net.techcable.event4j;

import lombok.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class MHRegisteredListener<E, L> extends RegisteredListener<E,L> {
    protected final MethodHandle methodHandle;

    @SneakyThrows(IllegalAccessException.class) // Wont happen
    public MHRegisteredListener(EventBus<E, L> eventBus, Method method, L listener) {
        super(eventBus, method, listener);
        method.setAccessible(true);
        this.methodHandle = MethodHandles.lookup().unreflect(method);
    }

    @Override
    @SneakyThrows
    public final void fire(E event) {
        methodHandle.invoke(listener, event);
    }

}
