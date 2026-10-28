---
summary: A clock that moves only when you set or advance it, so that every animation gives the same frame on every run.
---

Register it above the system clock in a test, then unregister it at the end, once the UIs of the test are closed: the system clock takes over again.

Learn: [Use a manual clock in a test](/build/bridges/clock/#use-a-manual-clock-in-a-test).

## ManualClockBridge(long)

Creates a clock at that time, in milliseconds.
Its nanoseconds start from those of the clock registered at that moment, so that the durations already measured go on.

## advance(long)

Moves the clock forward by that many milliseconds.

## nanoTime()

Returns the nanoseconds of the clock registered when this one was created, moved by the time set or advanced since.

## currentTimeMillis()

Returns the time of the clock, in milliseconds.

## time

Sets the clock to an absolute time, in milliseconds, and returns the clock.
