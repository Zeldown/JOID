---
summary: Creates daemon threads and runs a task on the render thread.
---

Learn: [Threads](/use/learn/running/threads/).

## daemonFactory(String)

Returns a thread factory whose threads are daemons named `name/1`, `name/2`…, so they never keep the program running.

## daemonThread(Runnable,String)

Returns a daemon thread named `name` that runs `task`, not started.

## runOnRenderThread(Runnable)

Runs `task` at once on the render thread, or hands it to the thread bridge, which runs it on the render thread at the next frame.
