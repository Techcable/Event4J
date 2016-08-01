package net.techcable.event4j;

import java.lang.reflect.Method;

import net.techcable.event4j.asm.ASMEventExecutorFactory;

@FunctionalInterface
public interface EventExecutor<E, L> {
    void fire(L listener, E event);

    static <E, L> EventExecutor<E, L> empty() {
        return (listener, event) -> {
            throw new UnsupportedOperationException("Empty event executor");
        };
    }

    @FunctionalInterface
    interface Factory {
        Factory METHOD_HANDLE_LISTENER_FACTORY = MHEventExecutor::new;
        Factory REFLECTION_LISTENER_FACTORY = ReflectionEventExecutor::new;
        @SuppressWarnings("unchecked") // generics r fun
        Factory ASM_LISTENER_FACTORY = ASMEventExecutorFactory.INSTANCE;

        <E, L> EventExecutor<E, L> create(EventBus<E, L> eventBus, Method method);

        static Factory getDefault() {
            return ASM_LISTENER_FACTORY != null ? ASM_LISTENER_FACTORY : METHOD_HANDLE_LISTENER_FACTORY;
        }
    }
}
