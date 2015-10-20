package net.techcable.event4j.marker;

import java.lang.reflect.Method;

@FunctionalInterface
public interface EventMarker {
    public default boolean isMarked(Method m) {
        return mark(m) != null;
    }

    public MarkedEvent mark(Method m);
}
