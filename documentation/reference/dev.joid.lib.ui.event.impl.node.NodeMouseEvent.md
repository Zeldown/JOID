---
summary: The event of a press, a release, a drag or a click on a node, with the position of the pointer and the button.
---

[[Node#onClick]], [[Node#handleMousePressed]], [[Node#handleMouseReleased]] and [[Node#handleMouseDragged]] receive it.
A `handle` listener hears every press of the UI, wherever the pointer is: test [[Node#isHovered]] to keep the presses on the node.

Learn: [Events](/use/learn/interactivity/events/#react-to-a-click).

## create(N,double,double,MouseButton,long,IConsumableEvent)

Creates the event for a node, the position of the pointer, the button and the time since the press.
`source` is the input that the event belongs to, or `null` for an input of its own.

## getMouseX()

Returns the horizontal position of the pointer, in design pixels of the UI.

## getMouseY()

Returns the vertical position of the pointer, in design pixels of the UI.

## getDeltaTime()

Returns, for a drag or a release, the time in ms since the button went down (`0` for a release whose press JOID did not see); `0` for a press or a click.

## getButton()

Returns the [[MouseButton]] of the event; for a drag, the button held.
