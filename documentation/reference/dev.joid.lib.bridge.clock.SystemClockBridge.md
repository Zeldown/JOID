---
summary: The default clock, which returns the time of the JVM.
---

JOID registers it in `BridgeHandler.CLOCK` before any backend.

Learn: [Clock bridge](/build/bridges/clock/).

## SystemClockBridge()

Creates the clock bridge.

## nanoTime()

Returns `System.nanoTime()`.

## currentTimeMillis()

Returns `System.currentTimeMillis()`.
