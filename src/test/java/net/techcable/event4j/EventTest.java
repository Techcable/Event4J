package net.techcable.event4j;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class EventTest {
    @Parameterized.Parameters(name = "{0}")
    public static String[] executorFactories() {
        return new String[]{
                "ASMExecutorFactory",
                "MethodHandleExecutorFactory",
                "ReflectionExecutorFactory"
        };
    }

    private final EventBus<Object, Object> eventBus;
    private TestListener testListener;

    public EventTest(String executorFactoryName) {
        final EventExecutor.Factory executorFactory;
        switch (executorFactoryName) {
            case "ASMExecutorFactory":
                executorFactory = EventExecutor.Factory.getAsmExecutorFactory(EventExecutor.Factory.METHOD_HANDLE_LISTENER_FACTORY);
                break;
            case "MethodHandleExecutorFactory":
                executorFactory = EventExecutor.Factory.METHOD_HANDLE_LISTENER_FACTORY;
                break;
            case "ReflectionExecutorFactory":
                executorFactory = EventExecutor.Factory.METHOD_HANDLE_LISTENER_FACTORY;
                break;
            default:
                throw new IllegalArgumentException("Unknown executor factory name: " + executorFactoryName);
        }
        eventBus = EventBus.builder().executorFactory(executorFactory).build();
    }

    @Before
    public void register() {
        this.testListener = new TestListener();
        eventBus.register(testListener);
    }

    @Test
    public void testEvent() {
        TestEvent event = new TestEvent();
        eventBus.fire(event);
        assertTrue(event.awesome);
    }

    @Test(expected = RuntimeException.class)
    public void testException() {
        eventBus.fire(new EvilEvent());
    }

    @Test
    public void testUnregister() {
        eventBus.unregister(testListener);
    }

    @Test
    public void testPrivateEvent() {
        eventBus.fire(new PrivateEvent());
    }

    public static class TestListener {
        @EventHandler
        public void onTest(TestEvent event) {
            event.awesome = true; // We are awesome
        }

        @EventHandler
        public void onEvil(EvilEvent evilEvent) {
            throw new RuntimeException("EVILLL");
        }

        @EventHandler
        public static void onStupid(TestEvent event) {
            // I'm a stupid person who uses static events
        }

        @EventHandler
        private void onPrivate(PrivateEvent event) {

        }
    }

    private static class PrivateEvent {
    }

    public static class EvilEvent {
    }

    public static class TestEvent {
        private boolean awesome;
    }
}
