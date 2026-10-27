---
summary: A toggle made of two parts: a `track` that covers the control and a `knob` that slides from the left (off) to the right (on).
---

A skin extends it and gives its parts in its constructor; the toggle places them on every frame.
A knob of size `0` takes half the width and the whole height of the toggle.
Attaching a toggle without a knob throws an `IllegalStateException`.

Learn: [UI kit](/use/learn/going-further/ui-kit/#know-the-parts-of-each-control), [Controls](/use/learn/interactivity/controls/#choose-between-two-values).

## ToggleNode(double,double,double,double)

Creates the toggle at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## track

The node drawn behind the knob, over the whole toggle, with a `zindex` of `-1`; optional.

## knob

The node that slides along the track and shows the state; required.

## speed

How fast the knob reaches its new place: the part of the remaining way that it covers in 1/60 s, at any frame rate.
`1F` moves it at once.

## getKnobProgress()

Returns the place of the knob on this frame, from `0F` at the left (off) to `1F` at the right (on).
A skin reads it to make another part follow the knob.

## layout()

Places the track over the toggle and moves the knob one step toward the side of the state.

## detach()

Forgets the place of the knob, so that the toggle shows its state at once when it comes back.
