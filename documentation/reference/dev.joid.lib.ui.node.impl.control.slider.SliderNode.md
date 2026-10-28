---
summary: A slider made of two parts: a `track` that covers the control and a `thumb` placed at the value.
---

A skin extends one of its subclasses and gives its parts in its constructor; the slider places them on every frame.
A thumb of size `0` is a square of the height of the slider, and the inset is half the width of the thumb unless the skin gives another one.
Attaching a slider without a thumb throws an `IllegalStateException`.

Learn: [UI kit](/use/learn/going-further/ui-kit/#know-the-parts-of-each-control), [Controls](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## SliderNode(double,double,double,double)

Creates the slider at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a subclass.

## track

The node drawn behind the thumb, over the whole slider, with a `zindex` of `-1`; optional.

## thumb

The node centered on the place of the value; required.

## speed

How fast the thumb reaches a value set from the code or a signal: the part of the remaining way that it covers in 1/60 s, at any frame rate.
`1F` moves it at once; during a drag, the thumb follows the pointer at once, up to half a step past the last accepted value.

## update()

Gives a thumb of size `0` the size of a square of the height of the slider.

## layout()

Places the track over the slider and moves the thumb toward the place of the value.

## detach()

Forgets the place of the thumb, so that the slider shows its value at once when it comes back.
