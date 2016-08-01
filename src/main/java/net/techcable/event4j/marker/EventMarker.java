package net.techcable.event4j.marker;

import java.lang.reflect.Method;

@FunctionalInterface
public interface EventMarker {
    default boolean isMarked(Method m) {
        return mark(m) != null;
    }

    MarkedEvent mark(Method m);
}
