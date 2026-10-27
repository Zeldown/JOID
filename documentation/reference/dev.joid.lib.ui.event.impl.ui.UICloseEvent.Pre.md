---
summary: The event of `beforeClose`, which can keep a UI open.
---

Learn: [UIs](/use/learn/describing-the-ui/uis/).

## create(U)

Creates the event for a UI about to close.

## cancel()

Keeps the UI open: the closing does not start, and `onClose` does not fire.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isCancelled()

If `true`, a listener kept the UI open.
