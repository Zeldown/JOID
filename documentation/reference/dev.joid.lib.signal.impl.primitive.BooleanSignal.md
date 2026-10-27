---
summary: Signal of a `boolean`, for a state that is on or off.
---

It works like [[Signal]] and adds `toggle()`.

Learn: [Signals](/use/learn/interactivity/signals/).

## BooleanSignal()

Creates a signal whose value and default value are `false`.

## BooleanSignal(boolean)

Creates a signal whose value and default value are `value`.

## toggle()

Inverts the value, then returns this signal.
Called from another thread, it inverts the value that the signal holds when the write applies on the render thread.
