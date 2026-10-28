---
summary: The bridge that tells JOID which thread draws and runs the work of other threads on it.
---

Every entry point of JOID that must run on the render thread goes through [[ThreadUtils#runOnRenderThread]], which calls this bridge.
[Write your own when the engine owns the render thread](/build/bridges/thread/#write-your-own-when-the-engine-owns-the-render-thread) hands the work to the queue of an engine that draws on a thread of its own.

Learn: [Thread bridge](/build/bridges/thread/), [Threads](/use/learn/running/threads/).

## isRenderThread()

If `true`, the calling thread is the thread that draws JOID; it answers without locking.

## execute(Runnable)

Runs the task on the render thread, before the next frame, in the order of the calls; never on another thread.
