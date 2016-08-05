/**
 * The MIT License
 * Copyright (c) 2015-2016 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
