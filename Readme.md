Event4J
=======
A annotation-based event system for java

## Features
- Annotation-Based
  - Just implement Listener and annotate with @EventHandler
- Lightweight
- 0-Dependency
  - Optionaly uses ASM for faster event invocation
    - ASM invocation is [an order of magnitude](benchmarks/Results.md) faster than reflection
- Fast
  - [Benchmarks](benchmarks/Results.md) available
- Thread Safe
  - Synchronous Events
    - Only one of these events may be executing at a time
    - Firing one of these events may block
  - By default, multiple events may be executed from multiple threads

# Requirements
- Java 8
  - Don't use outdated java!
- ASM (Optional)
  - Makes event invocation an order of magnitude faster
