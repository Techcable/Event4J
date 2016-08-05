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

import org.junit.Assert;
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
            Assert.assertFalse(event.awesome);
            event.awesome = true; // We are awesome, bum bum bum bum, bum bum bum
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

    private static class EvilEvent {
    }


    @Test
    public void testPriorityOrdering() {
        EventBus<Object, Object> eventBus = EventBus.build();
        eventBus.register(new Object() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void onLowest(TestEvent event) {
                Assert.assertEquals(0, event.value++);
            }

            @EventHandler(priority = EventPriority.LOW)
            public void onLow(TestEvent event) {
                Assert.assertEquals(1, event.value++);
            }

            @EventHandler(priority = EventPriority.NORMAL)
            public void onNormal(TestEvent event) {
                Assert.assertEquals(2, event.value++);
            }

            @EventHandler(priority = EventPriority.HIGH)
            public void onHigh(TestEvent event) {
                Assert.assertEquals(3, event.value++);
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            public void onHighest(TestEvent event) {
                Assert.assertEquals(4, event.value++);
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onMonitor(TestEvent event) {
                Assert.assertEquals(5, event.value++);
            }
        });
        TestEvent testEvent = new TestEvent();
        eventBus.fire(testEvent);
        Assert.assertEquals("Test event should've been fired 6 times", 6, testEvent.value);
    }
}
