---
summary: The behavior of a scrollbar without its look: it reads the scroll of the node it serves and scrolls it when the user drags along the bar.
---

Give a scrollbar to a node with [[Node#scrollbar]], never with `attach`: the node becomes its parent and the bar serves it.
The bar is horizontal when it is wider than high, and vertical otherwise.
A left press on the thumb grabs it where it was pressed; a press elsewhere on the bar centers the thumb on the pointer; a drag then scrolls the node.
Use [[ScrollbarNode]] for a look made of a track and a thumb.

Learn: [Layout](/use/learn/describing-the-ui/layout/#add-a-scrollbar), [UI kit](/use/learn/going-further/ui-kit/#draw-a-control-entirely-your-own-way).

## BaseScrollbarNode(double,double,double,double)

Creates the bar at `x`, `y` with a `width` and a `height` in design pixels, relative to the node it serves, from the constructor of a skin.

## scrollNode

The node that the bar scrolls; [[Node#scrollbar]] sets it.
A bar attached without one throws an `IllegalStateException`.

## minThumb

The smallest length of the thumb, in design pixels, so that a long content keeps a thumb large enough to grab.

## isHorizontal()

If `true`, the bar is wider than high: it then scrolls the node horizontally.

## isOverflowing()

If `true`, the content of the node goes beyond it on the axis of the bar.

## getRatio()

Returns the part of the content that the node shows on the axis of the bar, from `0D` to `1D`; `1D` while the content fits.

## getProgress()

Returns how far the node scrolled on the axis of the bar, from `0D` at the start to `1D` at the end; during a drag, the offset that the node glides to, so the thumb stays in place when a `beforeScroll` listener refuses the scroll.

## getThumbLength()

Returns the length of the thumb, in design pixels: the length of the bar times `getRatio()`, at least `minThumb` and at most the bar.

## isDragging()

If `true`, the user drags the bar.

## drawSkeleton(double,double)

Draws the bar as usual while its content waits, instead of a skeleton.

## mousePressed(NodeMouseEvent)

On a left press over the bar that no one consumed, starts the drag, scrolls the node to the pointer and consumes the press.

## mouseReleased(NodeMouseEvent)

Ends the drag on the release of the left button.

## mouseDragged(NodeMouseEvent)

Scrolls the node so that the thumb follows the pointer during the drag.

## detach()

Ends a drag in progress when the bar leaves the tree.
