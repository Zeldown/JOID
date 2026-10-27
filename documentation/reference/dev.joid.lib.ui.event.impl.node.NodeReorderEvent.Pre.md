---
summary: The event of `beforeReorder`, which can refuse a new place of the dragged child.
---

A refused move keeps the child at its place; the drag goes on.

Learn: [Reorderable lists](/use/learn/interactivity/reorderable-lists/#react-to-a-new-order).

## create(N,Node,int,int)

Creates the event for a list, the dragged child, its index now and the index asked for.

## cancel()

Refuses the move.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isCancelled()

Returns `true` once a listener refused the move.
