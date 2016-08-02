package net.techcable.event4j;

import lombok.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public final class MHEventExecutor<E, L> implements EventExecutor<E, L> {
    private static final MethodType METHOD_TYPE = MethodType.methodType(void.class, Object.class, Object.class);
    private final MethodHandle methodHandle;

    @SneakyThrows(IllegalAccessException.class) // Wont happen
    public MHEventExecutor(EventBus<E, L> eventBus, Method method) {
        RegisteredListener.validate(Objects.requireNonNull(eventBus, "Null eventBus"), Objects.requireNonNull(method, "Null method"));
        method.setAccessible(true);
        MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
        if (Modifier.isStatic(method.getModifiers())) {
            methodHandle = MethodHandles.dropArguments(methodHandle, 0, Object.class);
        }
        this.methodHandle = methodHandle.asType(METHOD_TYPE);
    }

    @Override
    @SneakyThrows
    public void fire(L listener, E event) {
        methodHandle.invokeExact(listener, event);
    }

}
