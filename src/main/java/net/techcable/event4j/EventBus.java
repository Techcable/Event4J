package net.techcable.event4j;

import lombok.*;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.techcable.event4j.marker.EventMarker;
import net.techcable.event4j.marker.MarkedEvent;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventBus<E, L> {
    private final ConcurrentMap<Class<?>, HandlerList<E, L>> handlers = new ConcurrentHashMap<>();
    private final RegisteredListener.Factory listenerFactory;
    @Getter
    private final Class<E> eventClass;
    @Getter
    private final Class<L> listenerClass;

    /**
     * Fire the given event to all registered listeners.
     * <p>Any exceptions (even checked ones) are propagated upwards untouched.</p>
     * <p>This method is weakly consistent, and may not see listeners that are added while it is firing.</p>
     *
     * @param event the event to fire
     */
    public void fire(E event) {
        if (event == null) throw new NullPointerException("Null event"); // This should be optimized away by the JIT
        eventClass.cast(event); // This is a JIT intristic, and its more efficient to check here then in each individual listener
        HandlerList<E, L> handler = handlers.get(event.getClass());
        if (handler == null) return; // No events of said type (will be optimized away by JIT)
        handler.fire(event);
    }

    public void unregister(L listener) {
        if (listenerClass.isInstance(listener)) return;
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!RegisteredListener.isEventHandler(method)) continue; // Not a handler
            RegisteredListener<E,L> registeredListener = listenerFactory.create(this, method, listener);
            handlers.get(registeredListener.getEventType()).unregister(registeredListener);
        }
    }

    public void register(L listener) {
        if (listener == null) throw new NullPointerException("Null listener");
        if (!listenerClass.isInstance(listener))
            throw new IllegalArgumentException("Invalid listener type: " + listener.getClass().getName());
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!RegisteredListener.isEventHandler(method)) continue; // Not a handler
            RegisteredListener<E,L> registeredListener = listenerFactory.create(this, method, listener);
            register(registeredListener);
        }
    }

    private void register(RegisteredListener<E,L> listener) {
        HandlerList<E, L> handlerList = handlers.computeIfAbsent(listener.getEventType(), (eventType) -> new HandlerList<>(EventBus.this));
        handlerList.register(listener);
    }

    // Constructors

    public static Builder<Object, Object> builder() {
        return new Builder<>();
    }

    public static EventBus build() {
        return builder().build();
    }

    @SuppressWarnings("unchecked") // Generics are fun!
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<E, L> {
        private Class<?> eventClass = Object.class;
        private Class<?> listenerClass = Object.class;
        private EventMarker eventMarker = m -> m.isAnnotationPresent(EventHandler.class) ? (MarkedEvent) () -> m.getAnnotation(EventHandler.class).priority() : null;
        private RegisteredListener.Factory listenerFactory = RegisteredListener.Factory.getDefault();

        public <E> Builder<E, L> eventClass(Class<E> eventClass) {
            this.eventClass = Objects.requireNonNull(eventClass, "Null event class");
            return (Builder<E, L>) this;
        }

        public <L> Builder<E, L> listenerClass(Class<L> listenerClass) {
            this.listenerClass = Objects.requireNonNull(listenerClass, "Null listener class");
            return (Builder<E, L>) this;
        }

        public Builder<E, L> eventMarker(EventMarker eventMarker) {
            this.eventMarker = Objects.requireNonNull(eventMarker, "Null event marker");
            return this;
        }

        public Builder<E, L> listenerFactory(RegisteredListener.Factory factory) {
            this.listenerFactory = Objects.requireNonNull(factory, "Null listener factory");
            return this;
        }

        public EventBus<E, L> build() {
            return new EventBus<>(listenerFactory,  (Class<E>) eventClass, (Class<L>) listenerClass);
        }
    }
}