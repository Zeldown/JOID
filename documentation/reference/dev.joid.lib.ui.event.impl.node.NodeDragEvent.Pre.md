---
summary: The event of `beforeDragStart` and `beforeDragDrop`, which can refuse the start or the drop of a drag.
---

A refused start leaves the node in place; a refused drop sends it back to where the drag started.

Learn: [Drag and drop](/use/learn/interactivity/drag-and-drop/#drop-on-a-target).

## create(N,Node)

Creates the event for a dragged node and its drop target, or `null` for none.

## cancel()

Refuses the start or the drop.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isCancelled()

Returns `true` once a listener refused the step.
