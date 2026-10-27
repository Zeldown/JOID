---
summary: A slider over a range of decimal numbers with a fixed step.
---

A skin extends it and gives its parts as for any [[SliderNode]].

Learn: [Controls](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## DoubleSliderNode(double,double,double,double)

Creates the slider at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## range(double,double,double)

Gives the options from `min` to `max` by `step`, computed in decimal so that `0.1D` steps stay exact; `max` is an option only when a step lands on it.
Throws an `IllegalArgumentException` when the step is not positive, when `min` is above `max`, or when the range holds more values than `Integer.MAX_VALUE`.
The options are computed on demand, so a large range costs nothing.

## fallback(Double)

Returns the option nearest to the value asked for, the smaller one on a tie, whatever the order of the options.
