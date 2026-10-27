---
summary: Signal of a `float` with arithmetic that writes its result, for ratios, progress and sizes.
---

It works like [[IntegerSignal]] with `float` values and the Java floating-point rules: dividing by `0F` gives an infinity or `NaN`, without exception.

Learn: [Signals](/use/learn/interactivity/signals/).

## FloatSignal()

Creates a signal whose value and default value are `0F`.

## FloatSignal(float)

Creates a signal whose value and default value are `value`.

## increment()

Adds `1F` to the value, then returns this signal.

## decrement()

Removes `1F` from the value, then returns this signal.

## add(float)

Adds `value` to the value, then returns this signal.

## subtract(float)

Removes `value` from the value, then returns this signal.

## multiply(float)

Multiplies the value by `value`, then returns this signal.

## divide(float)

Divides the value by `value`, then returns this signal.
