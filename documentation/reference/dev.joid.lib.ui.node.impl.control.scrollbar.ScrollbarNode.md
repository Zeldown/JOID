---
summary: A scrollbar made of two parts: a `track` that covers the bar and a `thumb` sized and placed from the scroll of the node it serves.
---

A skin extends it and gives its parts in its constructor; the bar places them on every frame.
Attaching a scrollbar without a thumb throws an `IllegalStateException`.

Learn: [UI kit](/use/learn/going-further/ui-kit/#know-the-parts-of-each-control), [Layout](/use/learn/describing-the-ui/layout/#add-a-scrollbar).

## ScrollbarNode(double,double,double,double)

Creates the bar at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## track

The node drawn behind the thumb, over the whole bar, with a `zindex` of `-1`; optional.

## thumb

The node that shows the visible part of the content: its length is `getThumbLength()` and its place follows `getProgress()`; required.

## layout()

Places the track over the bar, and sizes and moves the thumb along the axis of the bar.
