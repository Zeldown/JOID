---
summary: The event of `onDraw` and `onRender`, with the position of the pointer on the frame being drawn.
---

[[Node#onDraw]] fires right after the node drew itself, before its children in front of it; [[Node#onRender]] fires after the node, its children and its layers.
A listener draws there with [[DrawUtils]], in the coordinates of the parent.

Learn: [Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/#draw-around-a-node).

## create(N,double,double)

Creates the event for a node and the position of the pointer.

## getMouseX()

Returns the horizontal position of the pointer, in design pixels of the UI.

## getMouseY()

Returns the vertical position of the pointer, in design pixels of the UI.
