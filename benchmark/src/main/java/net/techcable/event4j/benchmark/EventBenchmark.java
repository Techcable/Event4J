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
package net.techcable.event4j.benchmark;

import net.techcable.event4j.EventBus;
import net.techcable.event4j.EventExecutor;
import net.techcable.event4j.EventHandler;
import net.techcable.event4j.RegisteredListener;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class EventBenchmark {
    private EventBus<TestEvent, TestListener> methodHandleBus;
    private EventBus<TestEvent, TestListener> reflectionBus;
    private EventBus<TestEvent, TestListener> asmBus;

    @Benchmark
    public void testMethodHandleSpeed() {
        methodHandleBus.fire(new TestEvent());
    }

    @Benchmark
    public void testReflectionSpeed() {
        reflectionBus.fire(new TestEvent());
    }

    @Benchmark
    public void testASMSpeed() {
        asmBus.fire(new TestEvent());
    }

    @Setup
    public void setup(Blackhole blackhole) {
        TestListener listener = new TestListener(blackhole);
        methodHandleBus = EventBus.builder()
                .eventClass(TestEvent.class) // Specify test event as the master class to accurately test casting sped
                .listenerClass(TestListener.class) // Specify test listener as the master class to accurately test casting speed
                .executorFactory(EventExecutor.Factory.METHOD_HANDLE_LISTENER_FACTORY)
                .build();
        reflectionBus = EventBus.builder()
                .eventClass(TestEvent.class)
                .listenerClass(TestListener.class)
                .executorFactory(EventExecutor.Factory.REFLECTION_LISTENER_FACTORY)
                .build();
        asmBus = EventBus.builder()
                .eventClass(TestEvent.class)
                .listenerClass(TestListener.class)
                .executorFactory(EventExecutor.Factory.ASM_LISTENER_FACTORY.get())
                .build();
        methodHandleBus.register(listener);
        reflectionBus.register(listener);
        asmBus.register(listener);
    }

    public static class TestListener {
        private final Blackhole blackhole;

        public TestListener(Blackhole blackhole) {
            this.blackhole = blackhole;
        }

        @EventHandler
        public void onFirst(TestEvent event) {
            blackhole.consume(event);
        }

        @EventHandler
        public void onSecond(TestEvent event) {
            blackhole.consume(event);
        }
    }

    public static class TestEvent {}

}
