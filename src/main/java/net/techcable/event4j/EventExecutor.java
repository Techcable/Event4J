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
        Factory ASM_LISTENER_FACTORY = ASMEventExecutorFactory.INSTANCE;

        default boolean canAccessMethod(Method method) {
            return true;
        }

        <E, L> EventExecutor<E, L> create(EventBus<E, L> eventBus, Method method);

        static Factory getDefault() {
            return hasAsmExecutorFactory() ? getAsmExecutorFactory(METHOD_HANDLE_LISTENER_FACTORY) : METHOD_HANDLE_LISTENER_FACTORY;
        }

        static boolean hasAsmExecutorFactory() {
            return ASMEventExecutorFactory.INSTANCE != null;
        }

        static Factory getAsmExecutorFactory(Factory fallback) {
            return !hasAsmExecutorFactory() ? null : new Factory() {
                @Override
                public <E, L> EventExecutor<E, L> create(EventBus<E, L> eventBus, Method method) {
                    if (ASMEventExecutorFactory.INSTANCE.canAccessMethod(method)) {
                        return ASMEventExecutorFactory.INSTANCE.create(eventBus, method);
                    } else {
                        return fallback.create(eventBus, method);
                    }
                }
            };
        }
    }
}
