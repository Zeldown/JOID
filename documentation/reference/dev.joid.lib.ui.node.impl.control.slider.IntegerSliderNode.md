---
summary: A slider over a range of integers.
---

A skin extends it and gives its parts as for any [[SliderNode]].

Learn: [Controls](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## IntegerSliderNode(double,double,double,double)

Creates the slider at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## range(int,int)

Gives the options every integer from `min` to `max`, both included.
Throws an `IllegalArgumentException` when `min` is above `max`, or when the range holds more values than `Integer.MAX_VALUE`, as `range(0, Integer.MAX_VALUE)` does.

## range(int,int,int)

Gives the options from `min` to `max` by `step`; `max` is an option only when a step lands on it.
Throws an `IllegalArgumentException` when the step is not positive, when `min` is above `max`, or when the range holds more values than `Integer.MAX_VALUE`.
The options are computed on demand, so a large range costs nothing.

## fallback(Integer)

Returns the option nearest to the value asked for, the smaller one on a tie, whatever the order of the options.
