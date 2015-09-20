package net.techcable.event4j;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@EqualsAndHashCode(of = {"listener", "method"})
public class RegisteredListener implements Comparable<RegisteredListener> {
    @Getter
    private final Listener listener;
    private final Method method;

    public RegisteredListener(Listener listener, Method method) {
        this.listener = listener;
        this.method = method;
        method.setAccessible(true);
        if (!isEventHandler(method)) throw new IllegalArgumentException("Method must be an event handler: " + toString());
        if (method.getParameterCount() != 1) throw new IllegalArgumentException("EventHandlers must have only one argument: " + toString());
        if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) throw new IllegalArgumentException(toString() + " must accept an event, not a " + method.getParameterTypes()[0].getSimpleName());
    }

    public final void fire(Object event) {
        try {
            method.invoke(listener, event);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Unable to access method " + toString());
        } catch (InvocationTargetException e) {
            throw new EventException(this, e.getCause());
        }
    }

    public Class<? extends Event> getEventType() {
        return method.getParameterTypes()[0].asSubclass(Event.class);
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
