---
summary: Signal of a `double` with arithmetic that writes its result, for positions, amounts and measures.
---

It works like [[IntegerSignal]] with `double` values and the Java floating-point rules: dividing by `0D` gives an infinity or `NaN`, without exception.

Learn: [Signals](/use/learn/interactivity/signals/).

## DoubleSignal()

Creates a signal whose value and default value are `0D`.

## DoubleSignal(double)

Creates a signal whose value and default value are `value`.

## increment()

Adds `1D` to the value, then returns this signal.

## decrement()

Removes `1D` from the value, then returns this signal.

## add(double)

Adds `value` to the value, then returns this signal.

## subtract(double)

Removes `value` from the value, then returns this signal.

## multiply(double)

Multiplies the value by `value`, then returns this signal.

## divide(double)

Divides the value by `value`, then returns this signal.
