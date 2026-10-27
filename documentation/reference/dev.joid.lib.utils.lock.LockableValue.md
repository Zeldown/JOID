---
summary: Mutable value that can be locked, after which its setters refuse to change it and point to the methods that return a copy.
---

[[Color]] and [[TextInfo]] extend it: lock a value kept in a constant, so code that receives it cannot change it for everyone.

Learn: [Colors and styling](/use/learn/style-and-motion/styling/), [Text](/use/learn/describing-the-ui/text/).

## lock()

Locks the value for good, then returns it.

## isLocked()

If `true`, the setters throw an `IllegalStateException`.

## copy()

Returns an unlocked copy of the value.

## mutate(String,Runnable)

Runs `mutation` on this value, then returns it; on a locked value, it throws an `IllegalStateException` whose message names the method `copy` that returns a modified copy.

## derive(UnaryOperator)

Returns the result of `mutation` applied to a copy of this value, which stays unchanged.

## describeLocked()

Returns the start of the message of a refused change, as in `This Color is locked`.
