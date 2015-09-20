Event4J
=======
A annotation-based event system for java

## Features
- Annotation-Based
  - Just implement Listener and annotate with @EventHandler
- Lightweight
- 0-Dependency
- Fast
  - Profiling reveals almost 0 overhead for firing events
- Thread Safe
  - Synchronous Events
    - Only one of these events may be executing at a time
    - Firing one of these events may block
  - By default, multiple events may be executed from multiple threads
