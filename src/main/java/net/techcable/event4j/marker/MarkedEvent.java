package net.techcable.event4j.marker;

import net.techcable.event4j.EventPriority;

@FunctionalInterface
public interface MarkedEvent {
    public EventPriority getPriority();
}
