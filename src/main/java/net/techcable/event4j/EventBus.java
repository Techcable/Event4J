package net.techcable.event4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class EventBus {
    private final ConcurrentMap<Class<? extends Event>, HandlerList> handlers = new ConcurrentHashMap<>();

    public void fire(Event event) {
        if (event == null) throw new NullPointerException("Null event");
        HandlerList handlerList = getHandler(event);
        if (handlerList == null) return;
        final boolean sync = event.isSynchronized();
        if (sync) lockType(event);
        try {
            handlerList.fire(event);
        } finally {
            if (sync) unlockType(event);
        }
    }

    private HandlerList getHandler(Event event) {
        return handlers.get(event.getClass());
    }

    public void unregister(Listener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!RegisteredListener.isEventHandler(method)) continue; // Not a handler
            RegisteredListener registeredListener = new RegisteredListener(listener, method);
            handlers.get(registeredListener.getEventType()).unregister(registeredListener);
        }
    }

    public void register(Listener listener) {
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
    private final Map<Class<? extends Event>, Lock> locks = Collections.synchronizedMap(new HashMap<Class<? extends Event>, Lock>());
    private void lockType(Event e) {
        Lock lock = locks.get(e.getClass());
        if (lock == null) return;
        lock.unlock();
    }

    private void unlockType(Event e) {
        Lock lock = locks.get(e.getClass());
        if (lock == null) return;
        lock.unlock();
    }
}