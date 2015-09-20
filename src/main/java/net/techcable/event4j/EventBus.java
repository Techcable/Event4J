package net.techcable.event4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventBus {
    private final ConcurrentMap<Class<?>, HandlerList> handlers = new ConcurrentHashMap<>();

    public void fire(Object event) {
        if (event == null) throw new NullPointerException("Null event");
        HandlerList handlerList = getHandler(event);
        if (handlerList == null) return;
        handlerList.fire(event);
    }

    private HandlerList getHandler(Object event) {
        return handlers.get(event.getClass());
    }


    public void unregister(Listener l) {
        for (RegisteredListener registeredListener : getRegisteredListeners(l)) {
            handlers.get(registeredListener.getEventType()).unregister(registeredListener);
        }
    }

    private final Object registryLock = new Object();
    private Set<RegisteredListener> getRegisteredListeners(Listener listener) {
        synchronized (registryLock) {
            return handlers.values().stream()
                    .flatMap(handlerList -> handlerList.getListenerSet().stream())
                    .filter((registeredListener) -> registeredListener.getListener().equals(listener))
                    .collect(Collectors.toSet());
        }
    }

    public void register(Listener listener) {
        if (listener == null) throw new NullPointerException("Null listener");
        Class<?> c = listener.getClass();
        while (Listener.class.isAssignableFrom(c)) {
            for (Method method : c.getDeclaredMethods()) {
                if (!RegisteredListener.isEventHandler(method)) continue; // Not a handler
                RegisteredListener registeredListener = new RegisteredListener(listener, method);
                if (!isEventClass(registeredListener.getEventType())) throw new IllegalArgumentException(registeredListener + " must have an event as an argument!");
                register(registeredListener);
            }
            c = c.getSuperclass();
        }
    }

    private void register(RegisteredListener listener) {
        // Register the listener for the event type and all its superclasses (except object) and abstract types
        Class<?> c = listener.getEventType();
        while (c != Object.class && !Modifier.isAbstract(c.getModifiers())) {
            HandlerList handlerList = handlers.computeIfAbsent(c, HandlerList::new);
            synchronized (registryLock) {
                handlerList.register(listener);
            }
            c = c.getSuperclass();
        }
    }

    private static boolean isEventClass(Class<?> c) {
        return c != Object.class && !Modifier.isAbstract(c.getModifiers());
    }
}