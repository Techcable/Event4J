package net.techcable.event4j;

import lombok.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@EqualsAndHashCode(of = {"listener", "method"})
public class RegisteredListener<E, L> implements Comparable<RegisteredListener> {
    private final EventBus<E, L> eventBus;
    @Getter
    private final L listener;
    private final Method method;

    public RegisteredListener(EventBus<E, L> eventBus, L listener, Method method) {
        this.eventBus = eventBus;
        this.listener = listener;
        this.method = method;
        method.setAccessible(true);
        if (!isEventHandler(method)) throw new IllegalArgumentException("Method must be an event handler: " + toString());
        if (method.getParameterCount() != 1) throw new IllegalArgumentException("EventHandlers must have only one argument: " + toString());
        if (!eventBus.getEventClass().isInstance(method.getParameterTypes()[0])) throw new IllegalArgumentException("EventHandler must accept one argument: " + method.getParameterTypes()[0].getSimpleName());
    }

    public final void fire(E event) {
        try {
            method.invoke(listener, event);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Unable to access method " + toString());
        } catch (InvocationTargetException e) {
            throw new EventException(this, e.getCause());
        }
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

    public static boolean isEventHandler(Method method) {
        return method.isAnnotationPresent(EventHandler.class);
    }

    @Override
    public int compareTo(RegisteredListener other) {
        return this.getPriority().compareTo(other.getPriority());
    }
}
