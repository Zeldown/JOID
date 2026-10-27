---
summary: Reads the pending mouse and keyboard events of LWJGL 2 and forwards them to the UI bridges of JOID.
---

Learn: [Input belongs to the loop](/build/backends/lwjgl2/#input-belongs-to-the-loop).

## poll()

Drains the event queues of `Mouse` and `Keyboard` and forwards every event to `BridgeHandler.UI`: moves, presses and releases, the wheel in notches of 120, key presses without the repeats, releases and typed characters.
Call it once per frame, before `frame()`; the rest of the application does not receive the events that it reads.
