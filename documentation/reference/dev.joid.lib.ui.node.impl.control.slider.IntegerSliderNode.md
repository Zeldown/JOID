---
summary: A slider over a range of integers.
---

A skin extends it and gives its parts as for any [[SliderNode]].

Learn: [Controls](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## IntegerSliderNode(double,double,double,double)

Creates the slider at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## range(int,int)

Gives the options every integer from `min` to `max`, both included.
Throws an `IllegalArgumentException` when `min` is above `max`.

## range(int,int,int)

Gives the options from `min` to `max` by `step`; `max` is an option only when a step lands on it.
Throws an `IllegalArgumentException` when the step is not positive or when `min` is above `max`.
