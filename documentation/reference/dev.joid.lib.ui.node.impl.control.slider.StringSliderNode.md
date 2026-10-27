---
summary: A slider over a list of texts, such as quality levels, given as texts or as the constants of an enum.
---

A skin extends it and gives its parts as for any [[SliderNode]]; `values(String...)` and `value(String)` of [[BaseChoiceNode]] take the texts themselves.

Learn: [Controls](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## StringSliderNode(double,double,double,double)

Creates the slider at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## values(Enum[])

Gives the options the names of enum constants, in the order given.

## value(Enum)

Selects the option named after an enum constant.
Throws an `IllegalArgumentException` when it is not one of the options.
