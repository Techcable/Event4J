package net.techcable.event4j;

import lombok.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class ReflectionRegisteredListener<E, L> extends RegisteredListener<E,L> {

    public ReflectionRegisteredListener(EventBus<E, L> eventBus, Method method, L listener) {
        super(eventBus, method, listener);
        getMethod().setAccessible(true);
    }

    @Override
    @SneakyThrows
    public final void fire(E event) {
        getMethod().invoke(listener, event);
    }

}
