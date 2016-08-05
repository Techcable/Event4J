package net.techcable.event4j;

import lombok.*;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

@RequiredArgsConstructor
public class HandlerList<E, L> {
    private final EventBus<E, L> eventBus;

    public void fire(E event) {
        for (RegisteredListener<E, L> listener : baked()) {
            listener.fire(event);
        }
    }

    public void register(RegisteredListener<E, L> listener) {
        listenerSet.add(listener);
        bakedListeners = null;
    }

    public void unregister(RegisteredListener listener) {
        listenerSet.remove(listener);
        bakedListeners = null;
    }

    private final SortedSet<RegisteredListener<E, L>> listenerSet = Collections.synchronizedSortedSet(new TreeSet<>());
    private volatile RegisteredListener<E, L>[] bakedListeners = null;

    @SuppressWarnings("unchecked")
    private RegisteredListener<E, L>[] baked() {
        RegisteredListener<E, L>[] baked = this.bakedListeners;
        if (baked == null) baked = bakeListeners(); // Separate method to assist inlining
        return baked;
    }

    private RegisteredListener<E, L>[] bakeListeners() {
        RegisteredListener<E, L>[] baked;
        synchronized (this) {
            if ((baked = this.bakedListeners) == null) { // In case someone else baked while we were locking
                //noinspection unchecked - ur mum's unchecked
                baked = listenerSet.toArray(new RegisteredListener[listenerSet.size()]);
                this.bakedListeners = baked;
            }
        }
        return baked;
    }

    public SortedSet<RegisteredListener<E, L>> getListenerSet() {
        return Collections.unmodifiableSortedSet(listenerSet);
    }
}
