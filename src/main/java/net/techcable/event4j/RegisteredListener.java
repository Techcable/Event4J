package net.techcable.event4j;

import lombok.*;

import java.lang.reflect.Method;
import java.util.Objects;

public final class RegisteredListener<E, L> implements Comparable<RegisteredListener> {
    @Getter
    protected final EventBus<E, L> eventBus;
    @Getter
    protected final L listener;
    @Getter
    protected final Method method;
    private final EventExecutor<E, L> executor;

    public RegisteredListener(EventBus<E, L> eventBus, Method method, L listener, EventExecutor<E, L> executor) {
        this.eventBus = Objects.requireNonNull(eventBus, "Null eventBus");;
        this.method = Objects.requireNonNull(method, "Null method");;
        this.listener = Objects.requireNonNull(listener, "Null listener");
        this.executor = Objects.requireNonNull(executor, "Null executor");
        validate(eventBus, method);
    }

    public static <E, L> void validate(EventBus<E, L> eventBus, Method method) {
        Objects.requireNonNull(eventBus, "Null eventBus");
        Objects.requireNonNull(method, "Null method");
        if (!isEventHandler(method)) throw new IllegalArgumentException("Method must be an event handler: " + method.getDeclaringClass().getName() + "::" + method.getName());
        if (method.getParameterCount() != 1) throw new IllegalArgumentException("EventHandlers must have only one argument: " + method.getDeclaringClass().getName() + "::" + method.getName());
        if (!eventBus.getEventClass().isAssignableFrom(method.getParameterTypes()[0])) throw new IllegalArgumentException("EventHandler must accept one argument: " + method.getParameterTypes()[0].getSimpleName());
        if (!eventBus.getListenerClass().isAssignableFrom(method.getDeclaringClass())) throw new IllegalArgumentException("Listener " + method.getDeclaringClass() + " must be instanceof " + eventBus.getListenerClass());
    }

    public static boolean isEventHandler(Method method) {
        return method.isAnnotationPresent(EventHandler.class);
    }

    public void fire(E event) {
        executor.fire(listener, event);
    }

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
        if (obj.getClass() == MHEventExecutor.class) {
            RegisteredListener other = (RegisteredListener) obj;
            return other.eventBus == this.eventBus && other.getListener() == this.getListener() && other.getMethod().equals(this.getMethod()); // Reference equality for listeners
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
}
