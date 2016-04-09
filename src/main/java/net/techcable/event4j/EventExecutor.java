package net.techcable.event4j;

import java.lang.reflect.Method;
import java.util.Optional;

import net.techcable.event4j.asm.ASMEventExecutorFactory;

@FunctionalInterface
public interface EventExecutor<E, L> {
    public void fire(L listener, E event);

    public static <E, L> EventExecutor<E, L> empty() {
        return (listener, event) -> {
            throw new UnsupportedOperationException("Empty event executor");
        };
    }

    @FunctionalInterface
    public interface Factory {
        public static final Factory METHOD_HANDLE_LISTENER_FACTORY = MHEventExecutor::new;
        public static final Factory REFLECTION_LISTENER_FACTORY = ReflectionEventExecutor::new;
        @SuppressWarnings("unchecked") // generics r fun
        public static final Optional<Factory> ASM_LISTENER_FACTORY = (Optional) ASMEventExecutorFactory.INSTANCE;

        public <E, L> EventExecutor<E, L> create(EventBus<E, L> eventBus, Method method);

        public static Factory getDefault() {
            return ASM_LISTENER_FACTORY.orElse(METHOD_HANDLE_LISTENER_FACTORY);
        }
    }
}
