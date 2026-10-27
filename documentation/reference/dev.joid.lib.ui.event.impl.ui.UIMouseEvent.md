---
summary: The event of a press, a release or a drag on a UI, with the position of the pointer and the button.
---

[[UI#handleMousePressed]], [[UI#handleMouseReleased]] and [[UI#handleMouseDragged]] receive it, for example to close a menu on a press in an empty area.

Learn: [Events](/use/learn/interactivity/events/#follow-an-input-through-the-tree).

## create(U,double,double,MouseButton,long)

Creates the event for a UI, the position of the pointer, the button and the time since the press.

## getMouseX()

Returns the horizontal position of the pointer, in design pixels of the UI.

## getMouseY()

Returns the vertical position of the pointer, in design pixels of the UI.

## getDeltaTime()

Returns, for a drag, the time in ms since the button went down; `0` for a press or a release.

## getButton()

Returns the [[MouseButton]] of the event; for a drag, the button held.
