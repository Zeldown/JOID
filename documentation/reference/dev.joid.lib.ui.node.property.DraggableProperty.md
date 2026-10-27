---
summary: How a node is dragged: the area it stays in, whether it moves or leaves a copy, and the targets it snaps to.
---
Create one with the factory of its area, then give it to [[Node#draggable]].

Learn: [Drag and drop](/use/learn/interactivity/drag-and-drop/).

## free()

A drag anywhere, with no area.

## custom(double,double,double,double)

A drag inside this rectangle of the canvas.

## parent()

A drag inside the bounds of the parent of the node.

## node(Node)

A drag inside the bounds of this node.

## ui()

A drag inside the design canvas, 1920 × 1080.

## screen()

A drag inside the window, which can show more than the canvas.

## disabled()

A property that never lets the node be dragged.

## enabled

Predicate that decides, for the node, whether a drag can start now; a drag that started goes to its end whatever it returns.

## type

`MOVE` drags the node itself; `COPY` drags a copy and leaves the node in place.

## snapType

How the target is chosen when the node is dropped.

## snap(Node[])

Adds targets: on drop, the node snaps to the center of one of them, or goes back where it started when none fits.

## getSnapNodes()

The targets; a read-only view, empty when the node has none.

## hasSnapping()

If `true`, the property has targets.

## getSnapping(Node)

The target that the node at its current place would snap to, or `null`.

## getBounds(Node)

The area that the node stays in, on the canvas.
Throws an `IllegalStateException` for a free drag, or for a `PARENT` area when the node has no parent.

## isEnabled(Node)

If `true`, the node can be dragged now.

## copy()

A copy of the property with the same area, type, predicate and targets.

## getAreaType()

The kind of area of the drag.

## getAreaNode()

The node whose bounds are the area of a `NODE` drag, or `null`.
