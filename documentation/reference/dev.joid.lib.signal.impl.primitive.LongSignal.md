---
summary: Signal of a `long` with arithmetic that writes its result, for large counts, sizes and timestamps.
---

It works like [[IntegerSignal]] with `long` values: the operations overflow without error and `divide` rounds toward zero.

Learn: [Signals](/use/learn/interactivity/signals/).

## LongSignal()

Creates a signal whose value and default value are `0`.

## LongSignal(long)

Creates a signal whose value and default value are `value`.

## increment()

Adds `1` to the value, then returns this signal.

## decrement()

Removes `1` from the value, then returns this signal.

## add(long)

Adds `value` to the value, then returns this signal.

## subtract(long)

Removes `value` from the value, then returns this signal.

## multiply(long)

Multiplies the value by `value`, then returns this signal.

## divide(long)

Divides the value by `value`, rounding toward zero, then returns this signal.
A divisor of `0` throws an `IllegalArgumentException` at the call and leaves the value unchanged.
