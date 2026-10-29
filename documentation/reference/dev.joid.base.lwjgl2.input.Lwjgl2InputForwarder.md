---
summary: Reads the pending mouse and keyboard events of LWJGL 2 and forwards them to the UI bridges of JOID.
---

Learn: [Input belongs to the loop](/build/base-modules/lwjgl2/#input-forwarder).

## poll()

Drains the event queues of `Mouse` and `Keyboard` and forwards every event with `forwardMouseEvent()` and `forwardKeyboardEvent()`.
Call it once per frame, before `frame()`; the rest of the application does not receive the events that it reads.

## forwardMouseEvent()

Forwards the current event of `Mouse`, after `Mouse.next()`, to `BridgeHandler.UI`: a move, a press or a release, and the wheel in notches of 120.
Returns `true` when JOID consumed the event, which the host then drops.

## forwardKeyboardEvent()

Forwards the current event of `Keyboard`, after `Keyboard.next()`, to `BridgeHandler.UI`: a press with the character it types, a repeat as a question on its press with its character, a release, or a character alone.
Returns `true` when JOID consumed the event, which the host then drops.
