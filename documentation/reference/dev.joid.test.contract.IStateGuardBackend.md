---
summary: A snapshot backend that can draw and set state like a host, for the state guard suite.
---

Learn: [The suites](/build/testing-a-backend/#the-suites), [Check the guard](/build/embedding-in-a-host/#check-the-guard).

## drawExternal()

Draws something with the API directly, as a host does, and changes state on the way.

## inject(StateTrap)

Sets that trap in the state of the context, as a host would leave it before a frame.

## supports(StateTrap)

If `true`, the context can set that trap; the suite skips the traps it cannot set.

## fill(int,int,int,int,int)

Fills that rectangle of the current target with that ARGB color through the API directly, from the bottom-left corner.

## readState()

Returns the state of the context that the guard must preserve, by name, to compare at each step.
