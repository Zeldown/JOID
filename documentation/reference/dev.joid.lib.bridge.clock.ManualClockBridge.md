---
summary: A clock that moves only when you set or advance it, so that every animation gives the same frame on every run.
---

Register it above the system clock in a test, then unregister it at the end: the system clock takes over again.

Learn: [Use a manual clock in a test](/build/bridges/clock/#use-a-manual-clock-in-a-test).

## ManualClockBridge(long)

Creates a clock at that time, in milliseconds.

## advance(long)

Moves the clock forward by that many milliseconds.

## nanoTime()

Returns the time of the clock converted to nanoseconds.

## currentTimeMillis()

Returns the time of the clock, in milliseconds.

## time

Sets the clock to an absolute time, in milliseconds, and returns the clock.
