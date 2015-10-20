package net.techcable.event4j;

import lombok.*;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import net.techcable.event4j.marker.EventMarker;
import net.techcable.event4j.marker.MarkedEvent;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventBus<E, L> {
    private final ConcurrentMap<Class<? extends E>, HandlerList<E, L>> handlers = new ConcurrentHashMap<>();
    @Getter
    private final Class<E> eventClass;
    @Getter
    private final Class<L> listenerClass;

    public void fire(E event) {
        if (event == null) throw new NullPointerException("Null event");
        if (!eventClass.isInstance(event))
            throw new IllegalArgumentException("Invalid event type: " + event.getClass().getName());
        HandlerList<E, L> handlerList = getHandler(event);
        if (handlerList == null) return;
        final boolean sync = event instanceof SynchronizedEvent;
        if (sync) lockType(event);
        try {
            handlerList.fire(event);
        } finally {
            if (sync) unlockType(event);
        }
    }

    private HandlerList<E, L> getHandler(E event) {
        return handlers.get(event.getClass());
    }

    public void unregister(L listener) {
        if (listenerClass.isInstance(listener)) return;
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!RegisteredListener.isEventHandler(method)) continue; // Not a handler
            RegisteredListener<E, L> registeredListener = new RegisteredListener<>(this, listener, method);
            handlers.get(registeredListener.getEventType()).unregister(registeredListener);
        }
    }

    public void register(L listener) {
        if (listener == null) throw new NullPointerException("Null listener");
        if (!listenerClass.isInstance(listener))
            throw new IllegalArgumentException("Invalid listener type: " + listener.getClass().getName());
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!RegisteredListener.isEventHandler(method)) continue; // Not a handler
            RegisteredListener<E, L> registeredListener = new RegisteredListener<>(this, listener, method);
            register(registeredListener);
        }
    }

    private void register(RegisteredListener<E, L> listener) {
        HandlerList<E, L> handlerList = handlers.computeIfAbsent(listener.getEventType(), (eventType) -> new HandlerList<>(EventBus.this));
        handlerList.register(listener);
    }

    // Locking
    private final Map<Class<?>, Lock> locks = Collections.synchronizedMap(new HashMap<>());

    private void lockType(Object e) {
        Lock lock = locks.get(e.getClass());
        if (lock == null) return;
        lock.unlock();
    }

    private void unlockType(Object e) {
        Lock lock = locks.get(e.getClass());
        if (lock == null) return;
        lock.unlock();
    }

    // Constructors

    public static Builder<Object, Object> builder() {
        return new Builder<>();
    }

    public static EventBus<Object, Object> build() {
        return builder().build();
    }

    @SuppressWarnings("unchecked") // Generics are fun!
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<E, L> {
        private Class<?> eventClass = Object.class;
        private Class<?> listenerClass = Object.class;
        private EventMarker eventMarker = m -> m.isAnnotationPresent(EventHandler.class) ? (MarkedEvent) () -> m.getAnnotation(EventHandler.class).priority() : null;

        public <E> Builder<E, L> eventClass(Class<E> eventClass) {
            this.eventClass = eventClass;
            return (Builder<E, L>) this;
        }

        public <L> Builder<E, L> listenerClass(Class<L> listenerClass) {
            this.listenerClass = listenerClass;
            return (Builder<E, L>) this;
        }

        public Builder<E, L> eventMarker(EventMarker eventMarker) {
            this.eventMarker = eventMarker;
            return this;
        }

        public EventBus<E, L> build() {
            return new EventBus<>((Class<E>) eventClass, (Class<L>) listenerClass);
        }
    }
}