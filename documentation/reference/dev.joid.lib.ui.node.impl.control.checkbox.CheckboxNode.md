---
summary: A checkbox made of two parts: a `box` that covers the control and a `mark` shown while it is checked.
---

A skin extends it and gives its parts in its constructor; the checkbox places them on every frame.
A part of size `0` takes the size of the checkbox; a part with a size keeps it and is centered.
Attaching a checkbox without a mark throws an `IllegalStateException`.

Learn: [UI kit](/use/learn/going-further/ui-kit/#know-the-parts-of-each-control), [Controls](/use/learn/interactivity/controls/#turn-an-option-on-or-off).

## CheckboxNode(double,double,double,double)

Creates the checkbox at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## box

The node drawn behind the mark, over the whole checkbox, with a `zindex` of `-1`; optional.
Giving another node replaces the previous one at its place.

## mark

The node shown in the middle of the box while it is checked, and hidden otherwise; required.
Giving another node replaces the previous one at its place.

## layout()

Places the box over the whole checkbox and centers the mark, shown or hidden by the value.
