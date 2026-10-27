---
summary: A move of a child in a reorderable list, with the child and its index before and after, in a cancellable `Pre` and a `Post`.
---

[[ReorderableFlexNode]] fires `onReorderStart` when a drag starts, `beforeReorder` and `onReorder` each time the dragged child changes place, and `onReorderEnd` at the drop.

Learn: [Reorderable lists](/use/learn/interactivity/reorderable-lists/#react-to-a-new-order).

## getIndex()

Returns the index of the child after the move.
At `onReorderEnd`, the index where the child was dropped.

## getChild()

Returns the child that moves.

## getPrevious()

Returns the index of the child before the move.
At `onReorderEnd`, the index where the drag started.
