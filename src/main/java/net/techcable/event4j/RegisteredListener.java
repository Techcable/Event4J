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

import java.lang.reflect.Method;
import java.util.Objects;

import net.techcable.event4j.marker.MarkedEvent;

public final class RegisteredListener<E, L> implements Comparable<RegisteredListener> {
    @Getter
    private final L listener;
    @Getter
    private final Method method;
    private final MarkedEvent marked;
    private final EventExecutor<E, L> executor;

    public RegisteredListener(EventBus<?, ?> eventBus, Method method, L listener, EventExecutor<E, L> executor) {
        validate(eventBus, method);
        this.method = Objects.requireNonNull(method, "Null method");
        this.listener = Objects.requireNonNull(listener, "Null listener");
        this.executor = Objects.requireNonNull(executor, "Null executor");
        this.marked = Objects.requireNonNull(eventBus.getEventMarker().mark(method), "Null marked event");
    }

    public static <E, L> void validate(EventBus<E, L> eventBus, Method method) {
        Objects.requireNonNull(eventBus, "Null eventBus");
        Objects.requireNonNull(method, "Null method");
        if (!eventBus.getEventMarker().isMarked(method)) throw new IllegalArgumentException("Method must be an event handler: " + method.getDeclaringClass().getName() + "::" + method.getName());
        if (method.getParameterCount() != 1) throw new IllegalArgumentException("EventHandlers must have only one argument: " + method.getDeclaringClass().getName() + "::" + method.getName());
        if (!eventBus.getEventClass().isAssignableFrom(method.getParameterTypes()[0])) throw new IllegalArgumentException("EventHandler must accept one argument: " + method.getParameterTypes()[0].getSimpleName());
        if (!eventBus.getListenerClass().isAssignableFrom(method.getDeclaringClass())) throw new IllegalArgumentException("Listener " + method.getDeclaringClass() + " must be instanceof " + eventBus.getListenerClass());
    }

    public void fire(E event) {
        executor.fire(listener, event);
    }

    @SuppressWarnings("unchecked") // Ur mum's unchecked
    public Class<? extends E> getEventType() {
        return (Class<? extends E>) method.getParameterTypes()[0];
    }

    public int getPriority() {
        return marked.getPriority();
    }

    @Override
    public String toString() {
        return listener.getClass().getName() + "::" + method.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this || obj == null) return false;
        if (obj instanceof RegisteredListener) {
            RegisteredListener other = (RegisteredListener) obj;
            return other.getListener() == this.getListener() && other.getMethod().equals(this.getMethod()); // Reference equality for listeners
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getMethod().hashCode() ^ System.identityHashCode(getListener());
    }

    @Override
    public int compareTo(RegisteredListener other) {
        return other.getMethod().equals(this.getMethod())
                && other.getListener() == this.getListener() ? 0
                : Integer.compare(this.getPriority(), other.getPriority());
    }
}
