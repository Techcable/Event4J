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
