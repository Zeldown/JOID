---
summary: A step of the drag of a node, with its drop target, in a cancellable `Pre` and a `Post`.
---

[[Node#beforeDragStart]] and [[Node#beforeDragDrop]] receive a [[NodeDragEvent.Pre]] and can refuse the step; [[Node#onDragStart]], [[Node#onDrag]], [[Node#onDragDrop]] and [[Node#onDragSnap]] receive a [[NodeDragEvent.Post]].

Learn: [Drag and drop](/use/learn/interactivity/drag-and-drop/#react-to-the-drag).

## getTarget()

Returns the node that the dragged node snaps to, for `beforeDragDrop`, `onDragDrop` and `onDragSnap`.
`null` at the start and during the drag, and for a drop on no target.
