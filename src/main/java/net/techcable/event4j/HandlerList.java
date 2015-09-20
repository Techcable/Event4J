package net.techcable.event4j;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class HandlerList {
    private final Class<? extends Event> eventType;

    public void fire(Event event) {
        if (!eventType.isInstance(event)) return; // Sanity
        for (RegisteredListener listener : baked()) {
            listener.fire(event);
        }
    }

    public void register(RegisteredListener listener) {
        listenerSet.add(listener);
        bakedListeners = null;
    }

    public void unregister(RegisteredListener listener) {
        listenerSet.remove(listener);
        bakedListeners = null;
    }

    private final Set<RegisteredListener> listenerSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile RegisteredListener[] bakedListeners = null;
    private RegisteredListener[] baked() {
        RegisteredListener[] baked = this.bakedListeners;
        if (baked == null) {
            synchronized (this) {
                if ((baked = this.bakedListeners) == null) { // In case someone else baked while we were locking
                    baked = listenerSet.toArray(new RegisteredListener[listenerSet.size()]);
                    Arrays.sort(baked);
                    this.bakedListeners = baked;
                }
            }
        }
        return baked;
    }

    public Set<RegisteredListener> getListenerSet() {
        return Collections.unmodifiableSet(listenerSet);
    }
}