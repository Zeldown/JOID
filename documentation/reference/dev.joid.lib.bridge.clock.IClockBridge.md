---
summary: The bridge that gives JOID the time read by animations, tweens, media and drags.
---

`BridgeHandler.CLOCK` holds a [[SystemClockBridge]] by default; a host whose time can pause, or a test, registers its own above it.

Learn: [Clock bridge](/build/bridges/clock/).

## nanoTime()

Returns the current time in nanoseconds, from any origin, at the same rate as `currentTimeMillis()`; JOID uses only differences of it.

## currentTimeMillis()

Returns the current time in milliseconds; it never goes backwards.
