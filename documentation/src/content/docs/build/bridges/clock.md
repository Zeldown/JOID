---
title: "Clock bridge"
description: "The contract of the clock bridge: the time that drives animations, media and drags."
genre: build
sidebar:
  order: 5
---

Every time measure of JOID reads the clock bridge: animations and tweens, videos and animated images, the press time of a drag.
The core reads it through [[IClockBridge]], from `BridgeHandler.CLOCK`.
By default, a [[SystemClockBridge]] returns the time of the JVM.

A backend rarely registers a clock.
A host does when its own time can pause or slow down, so that the interface follows it.
Tests register a [[ManualClockBridge]], which moves only when they call `advance(milliseconds)`: every animation then gives the same frame on every run.

## Required

| Member | What it returns |
|---|---|
| [[IClockBridge#currentTimeMillis]] | The current time, in milliseconds. |
| [[IClockBridge#nanoTime]] | The current time in nanoseconds, from any origin, at the same rate as the milliseconds: JOID uses only differences of it. |

## Guarantees

| Rule | Why |
|---|---|
| Never go backwards. | Animations compute a delta from the previous frame. |
| Advance both methods at the same rate. | Media read the nanoseconds and animations the milliseconds: two drifting clocks desynchronize a video and its controls. |

## Use a manual clock in a test

```java fragment
final ManualClockBridge clock = new ManualClockBridge(0L);
BridgeHandler.CLOCK.register(clock);
clock.advance(16L);
```

`time(milliseconds)` sets the clock to an absolute time.
Unregister it at the end of the test with `BridgeHandler.CLOCK.unregister(clock)`: the system clock, still in the registry below it, takes over.
The snapshot runner of the testkit registers one manual clock for its whole run and sets it back to the same time before every scenario.
