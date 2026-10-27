---
summary: Shows a progress from 0 to 1 by sizing its fill part over an optional track part.
---
The fill and the track are nodes of your own: the progress node sizes and places them on every frame.

Learn: [Controls](/use/learn/interactivity/controls/#show-a-progress).

## create(double,double,double,double)

Creates a progress bar at `x`, `y` with this width and height.

## ProgressNode(double,double,double,double)

Called by a subclass with the same arguments as `create`.

## progress

Share of the bar covered by the fill, from `0` to `1`; values outside are drawn as `0` or `1`.

## direction

Side the fill grows from.

## track

Node drawn under the fill, at the size of the bar; `null` draws none.

## fill

Node that covers the share of the bar given by the progress.
Throws an `IllegalStateException` when the node is attached without one.

## layout()

Sizes the track to the bar and the fill to the progress, from the side of the direction.
