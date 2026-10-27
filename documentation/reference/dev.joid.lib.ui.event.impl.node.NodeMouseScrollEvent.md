---
summary: The event of `handleMouseScroll` on a node, with the position of the pointer and the turn of the wheel.
---

A node that scrolls its content consumes the wheel while it can still scroll in that direction.

Learn: [Layout](/use/learn/describing-the-ui/layout/#scroll-a-node).

## create(N,double,double,double,double,IConsumableEvent)

Creates the event for a node, the position of the pointer and the notches on each axis.
`source` is the input that the event belongs to, or `null` for an input of its own.

## getMouseX()

Returns the horizontal position of the pointer, in design pixels of the UI.

## getMouseY()

Returns the vertical position of the pointer, in design pixels of the UI.

## getNotchesX()

Returns the horizontal turn of the wheel or of the touchpad, in notches, fractions included.

## getNotchesY()

Returns the vertical turn of the wheel, in notches, fractions included: positive when the wheel turns up, negative when it turns down.
A scrolling node moves its content by 30 design pixels per notch, times its `scrollSpeed`.
