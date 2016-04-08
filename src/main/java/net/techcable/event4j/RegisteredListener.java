package net.techcable.event4j;

import lombok.*;

import java.lang.reflect.Method;

public abstract class RegisteredListener<E, L> implements Comparable<RegisteredListener> {
    protected final EventBus<E, L> eventBus;
    @Getter
    protected final L listener;
    @Getter
    protected final Method method;

    public RegisteredListener(EventBus<E, L> eventBus, Method method, L listener) {
        if (!isEventHandler(method)) throw new IllegalArgumentException("Method must be an event handler: " + toString());
        if (method.getParameterCount() != 1) throw new IllegalArgumentException("EventHandlers must have only one argument: " + toString());
        if (!eventBus.getEventClass().isAssignableFrom(method.getParameterTypes()[0])) throw new IllegalArgumentException("EventHandler must accept one argument: " + method.getParameterTypes()[0].getSimpleName());
        this.eventBus = eventBus;
        this.method = method;
        this.listener = listener;
    }

    public static boolean isEventHandler(Method method) {
        return method.isAnnotationPresent(EventHandler.class);
    }

    public abstract void fire(E event);

    public Class<? extends E> getEventType() {
        return method.getParameterTypes()[0].asSubclass(eventBus.getEventClass());
    }

    public EventPriority getPriority() {
        return method.getAnnotation(EventHandler.class).priority();
    }

    @Override
    public String toString() {
        return listener.getClass().getName() + "::" + method.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this || obj == null) return false;
        if (obj.getClass() == MHRegisteredListener.class) {
            RegisteredListener other = (RegisteredListener) obj;
            return other.getListener() == this.getListener() && other.getMethod().equals(this.getMethod()); // Reference equality for listeners
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getMethod().hashCode() ^ getListener().hashCode(); // XOR is good for combinding two hashcodes, and we don't use identity hash code since listeners may overide with a better one, and hashcodes never change
    }

    @Override
    public int compareTo(RegisteredListener other) {
        return this.getPriority().compareTo(other.getPriority());
    }

    @FunctionalInterface
    public interface Factory {
        public static final Factory METHOD_HANDLE_LISTENER_FACTORY = MHRegisteredListener::new;
        public static final Factory REFLECTION_LISTENER_FACTORY = ReflectionRegisteredListener::new;

        public <E, L> RegisteredListener<E, L> create(EventBus<E, L> bus, Method method, L listener);

        public static Factory getDefault() {
            return METHOD_HANDLE_LISTENER_FACTORY;
        }
    }
}
