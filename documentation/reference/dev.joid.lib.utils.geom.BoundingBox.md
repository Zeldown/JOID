---
summary: Mutable rectangle stored by its minimum and maximum corners, in design pixels.
---

`expand(...)` and `contract(...)` change this box; `copy()` first to keep the original.

Learn: [Layout](/use/learn/describing-the-ui/layout/), [Drag and drop](/use/learn/interactivity/drag-and-drop/).

## create(double,double,double,double)

Returns the box whose top-left corner is (`x`, `y`) and whose size is `width` × `height`.

## copy()

Returns another box with the same corners.

## expand(double)

Moves every side outward by `value`, then returns this box.

## contract(double)

Moves every side inward by `value`, then returns this box.

## getWidth()

Returns the width, `maxX - minX`.

## getHeight()

Returns the height, `maxY - minY`.

## getMinX()

Returns the left edge.

## getMinY()

Returns the top edge.

## getMaxX()

Returns the right edge.

## getMaxY()

Returns the bottom edge.

## setMinX(double)

Moves the left edge.

## setMinY(double)

Moves the top edge.

## setMaxX(double)

Moves the right edge.

## setMaxY(double)

Moves the bottom edge.
