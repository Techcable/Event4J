package net.techcable.event4j;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Event {
    private final boolean sync;

    public Event() {
        this(false);
    }

    public final boolean isSynchronized() {
        return sync;
    }
}
