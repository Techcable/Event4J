package net.techcable.event4j;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class EventBus {
    private final ConcurrentMap<Class<?>, HandlerList> handlers = new ConcurrentHashMap<>();

    public void fire(Object event) {
        if (event == null) throw new NullPointerException("Null event");
        HandlerList handlerList = getHandler(event);
        if (handlerList == null) return;
        final boolean sync = event instanceof SynchronizedEvent;
        if (sync) lockType(event);
        try {
            handlerList.fire(event);
        } finally {
            if (sync) unlockType(event);
        }
    }

    private HandlerList getHandler(Object event) {
        return handlers.get(event.getClass());
    }

    public void unregister(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!RegisteredListener.isEventHandler(method)) continue; // Not a handler
            RegisteredListener registeredListener = new RegisteredListener(listener, method);
            handlers.get(registeredListener.getEventType()).unregister(registeredListener);
        }
    }

    public void register(Object listener) {
        if (listener == null) throw new NullPointerException("Null listener");
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!RegisteredListener.isEventHandler(method)) continue; // Not a handler
            RegisteredListener registeredListener = new RegisteredListener(listener, method);
            register(registeredListener);
        }
    }

    private void register(RegisteredListener listener) {
        HandlerList handlerList = handlers.computeIfAbsent(listener.getEventType(), HandlerList::new);
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
}