---
title: "Clock bridge"
description: "The contract of the clock bridge: the time that drives animations, media and drags."
genre: build
sidebar:
  order: 5
---

Every time measure of JOID reads the clock bridge: animations and tweens, tasks, videos and animated images, the press time of a drag, the repeat of a held key.
The core reads it through [[IClockBridge]], from [[BridgeHandler#CLOCK]].
By default, a [[SystemClockBridge]] returns the time of the JVM.

A backend rarely registers a clock.
A host does when its own time can pause or slow down, so that the interface follows it.
Tests register a [[ManualClockBridge]], which moves only when they call `advance(milliseconds)`: every animation then gives the same frame on every run.

## Required

| Member | What it returns |
|---|---|
| [[IClockBridge#currentTimeMillis]] | The current time, in milliseconds: JOID reads it as a date, or as the phase of a cycle such as `Color.RAINBOW()` and the blink of a text cursor. |
| [[IClockBridge#nanoTime]] | The current time in nanoseconds, from any origin: JOID measures every duration as a difference of it. |

## Guarantees

| Rule | Why |
|---|---|
| Never go backwards. | A duration is the difference of two readings: a clock that goes back gives a negative one, and an animation or a scroll in progress breaks. |
| Advance both methods at the same rate. | Durations read the nanoseconds and cycles the milliseconds: two drifting clocks put a color cycle out of step with the animations around it. |

## Use a manual clock in a test

```java fragment
final ManualClockBridge clock = new ManualClockBridge(BridgeHandler.CLOCK.get().currentTimeMillis());
BridgeHandler.CLOCK.register(clock);
clock.advance(16L);
```

Start it at the current time, so that the dates and the cycles that read the milliseconds do not jump.
Whatever time you give it, its nanoseconds go on from those of the clock it replaces, so a duration measured across the registration stays right.

`time(milliseconds)` sets the clock to an absolute time.
Unregister it at the end of the test with `BridgeHandler.CLOCK.unregister(clock)`, once its UIs are closed: the system clock, still in the registry below it, takes over, and its time is behind the manual clock by all that the test advanced beyond the real time.
The snapshot runner of the testkit registers one manual clock for its whole run and sets it back to the same time before every scenario.
