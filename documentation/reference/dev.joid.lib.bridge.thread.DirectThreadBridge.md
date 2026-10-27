---
summary: The thread bridge that JOID holds before any backend, for which every thread is the render thread.
---

It runs every task at once, on the calling thread.
A backend whose `createThreadBridge()` returns `null` keeps it.

Learn: [Thread bridge](/build/bridges/thread/#the-default-fits-a-loop-on-the-thread-that-registers).

## DirectThreadBridge()

Creates the thread bridge.

## isRenderThread()

Returns `true` on every thread.

## execute(Runnable)

Runs the task at once, on the calling thread.
