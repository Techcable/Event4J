package net.techcable.event4j;

public class EventException extends RuntimeException {
    public EventException(RegisteredListener listener, Throwable t) {
        super("Exception in " + listener, t);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this; // We dont want a stack trace for EventExceptions
    }
}
