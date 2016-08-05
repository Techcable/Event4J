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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public final class MHEventExecutor<E, L> implements EventExecutor<E, L> {
    private static final MethodType METHOD_TYPE = MethodType.methodType(void.class, Object.class, Object.class);
    private final MethodHandle methodHandle;

    @SneakyThrows(IllegalAccessException.class) // Wont happen
    public MHEventExecutor(EventBus<E, L> eventBus, Method method) {
        RegisteredListener.validate(Objects.requireNonNull(eventBus, "Null eventBus"), Objects.requireNonNull(method, "Null method"));
        method.setAccessible(true);
        MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
        if (Modifier.isStatic(method.getModifiers())) {
            methodHandle = MethodHandles.dropArguments(methodHandle, 0, Object.class);
        }
        this.methodHandle = methodHandle.asType(METHOD_TYPE);
    }

    @Override
    @SneakyThrows
    public void fire(L listener, E event) {
        methodHandle.invokeExact(listener, event);
    }

}
