---
summary: Signal of an `int` with arithmetic that writes its result, for counters, scores and indexes.
---

It works like [[Signal]]; each operation reads the value, computes and writes the result, which notifies the listeners when it differs.
The operations follow the Java `int` rules: they overflow without error and `divide` rounds toward zero.
Called from another thread, an operation applies on the render thread to the value the signal holds then, so concurrent increments add up.

Learn: [Signals](/use/learn/interactivity/signals/).

## IntegerSignal()

Creates a signal whose value and default value are `0`.

## IntegerSignal(int)

Creates a signal whose value and default value are `value`.

## increment()

Adds `1` to the value, then returns this signal.

## decrement()

Removes `1` from the value, then returns this signal.

## add(int)

Adds `value` to the value, then returns this signal.

## subtract(int)

Removes `value` from the value, then returns this signal.

## multiply(int)

Multiplies the value by `value`, then returns this signal.

## divide(int)

Divides the value by `value`, rounding toward zero, then returns this signal.
A divisor of `0` throws an `IllegalArgumentException` at the call and leaves the value unchanged.
