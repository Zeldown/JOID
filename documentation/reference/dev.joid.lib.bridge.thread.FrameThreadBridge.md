---
summary: The default thread bridge of a backend, for which the thread that creates it draws while other threads queue their tasks.
---

[[Backend#register]] creates it on the thread that registers the backend.
[[UIBridgeRegistry#frame]] calls `runTasks()` at the start of every frame.

Learn: [Thread bridge](/build/bridges/thread/#the-default-fits-a-loop-on-the-thread-that-registers), [Threads](/use/learn/running/threads/).

## FrameThreadBridge()

Creates the bridge and makes the calling thread the render thread.

## isRenderThread()

If `true`, the calling thread is the thread that created the bridge.

## execute(Runnable)

Runs the task at once on the render thread; from another thread, queues it for the next `runTasks()`.

## runTasks()

Runs the queued tasks in order, including the tasks queued while it runs.
Called from another thread than the one that created the bridge, it warns once.
A task that throws a `RuntimeException` prints one warning per kind of failure, and the next tasks still run.
