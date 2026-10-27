---
summary: The event of `handleMouseScroll` on a UI, with the position of the pointer and the turn of the wheel that no node consumed.
---

Learn: [Events](/use/learn/interactivity/events/#follow-an-input-through-the-tree).

## create(U,double,double,double,double)

Creates the event for a UI, the position of the pointer and the notches on each axis.

## getMouseX()

Returns the horizontal position of the pointer, in design pixels of the UI.

## getMouseY()

Returns the vertical position of the pointer, in design pixels of the UI.

## getNotchesX()

Returns the horizontal turn of the wheel or of the touchpad, in notches, fractions included.

## getNotchesY()

Returns the vertical turn of the wheel, in notches, fractions included: positive when the wheel turns up, negative when it turns down.
