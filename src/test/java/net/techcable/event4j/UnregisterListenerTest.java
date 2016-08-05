package net.techcable.event4j;

import org.junit.Assert;
import org.junit.Test;

public class UnregisterListenerTest {
    private final EventBus<Object, Object> bus = EventBus.build();

    @Test
    public void testUnregister() {
        bus.register(this);
        bus.unregister(this);
        bus.fire(new TestEvent());
    }

    @EventHandler
    public void onEvent(TestEvent evt) {
        Assert.fail("Event listener wasn't unregistered");
    }
}
