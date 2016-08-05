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

import java.lang.reflect.Method;

import net.techcable.event4j.asm.ASMEventExecutorFactory;

@FunctionalInterface
public interface EventExecutor<E, L> {
    void fire(L listener, E event);

    static <E, L> EventExecutor<E, L> empty() {
        return (listener, event) -> {
            throw new UnsupportedOperationException("Empty event executor");
        };
    }

    @FunctionalInterface
    interface Factory {
        Factory METHOD_HANDLE_LISTENER_FACTORY = MHEventExecutor::new;
        Factory REFLECTION_LISTENER_FACTORY = ReflectionEventExecutor::new;
        Factory ASM_LISTENER_FACTORY = ASMEventExecutorFactory.INSTANCE;

        default boolean canAccessMethod(Method method) {
            return true;
        }

        <E, L> EventExecutor<E, L> create(EventBus<E, L> eventBus, Method method);

        static Factory getDefault() {
            return hasAsmExecutorFactory() ? getAsmExecutorFactory(METHOD_HANDLE_LISTENER_FACTORY) : METHOD_HANDLE_LISTENER_FACTORY;
        }

        static boolean hasAsmExecutorFactory() {
            return ASMEventExecutorFactory.INSTANCE != null;
        }

        static Factory getAsmExecutorFactory(Factory fallback) {
            return !hasAsmExecutorFactory() ? null : new Factory() {
                @Override
                public <E, L> EventExecutor<E, L> create(EventBus<E, L> eventBus, Method method) {
                    if (ASMEventExecutorFactory.INSTANCE.canAccessMethod(method)) {
                        return ASMEventExecutorFactory.INSTANCE.create(eventBus, method);
                    } else {
                        return fallback.create(eventBus, method);
                    }
                }
            };
        }
    }
}
