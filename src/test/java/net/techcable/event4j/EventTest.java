package net.techcable.event4j;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EventTest {
    private final EventBus eventBus = EventBus.build();
    private boolean awesome;
    private TestListener testListener;

    @Before
    public void register() {
        this.testListener = new TestListener();
        testListener.hasCalled = false;
        eventBus.register(testListener);
    }

    @Test
    public void testEvent() {
        awesome = false;
        eventBus.fire(new TestEvent());
        assertTrue(this.awesome);
    }

    @Test(expected = RuntimeException.class)
    public void testException() {
        eventBus.fire(new EvilEvent());
    }

    @Test
    public void testUnregister() {
        eventBus.unregister(testListener);
    }

    public class TestListener {

        boolean hasCalled;

        @EventHandler(priority = EventPriority.HIGHEST)
        public void callZero(TestEvent event) {
            hasCalled = true;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void callOne(TestEvent event) {
            assertTrue(hasCalled);
        }

        @EventHandler
        public void onTest(TestEvent event) {
            EventTest.this.awesome = true; // We are awesome
        }

        @EventHandler
        public void onEvil(EvilEvent evilEvent) {
            throw new RuntimeException("EVILLL");
        }
    }

    public class EvilEvent {
    }

    public class TestEvent {
    }
}
