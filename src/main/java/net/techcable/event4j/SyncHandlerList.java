package net.techcable.event4j;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncHandlerList<E extends SynchronizedEvent, L> extends HandlerList<E, L> {
    private final Lock lock = new ReentrantLock();
    public SyncHandlerList(EventBus<E, L> eventBus) {
        super(eventBus);
    }

    @Override
    public void fire(E event) {
        lock.lock();
        try {
            super.fire(event);
        } finally {
            lock.unlock();
        }
    }
}
