---
title: "Thread bridge"
description: "The contract of the thread bridge: tell JOID which thread draws, and run the work of other threads there."
genre: build
sidebar:
  order: 4
---

JOID is not thread-safe: it opens UIs, runs callbacks and touches GPU objects on one thread, the render thread.
The thread bridge tells the core which thread that is, and how to post work to it.
The core reads it through [[IThreadBridge]], from `BridgeHandler.THREAD`.

## The default fits a loop on the thread that registers

[[Backend#register]] registers the bridge that `createThreadBridge()` returns, a [[FrameThreadBridge]] by default:

- the thread that calls `register()` is the render thread;
- a task sent from another thread waits in a queue, and [[UIBridgeRegistry#frame]] runs the queue at the start of the next frame, in order;
- a task that throws prints one warning and the next tasks still run.

That is right when the loop that calls `frame()` runs on the thread that registered the backend, as in the usual window loop.
Before any backend, the registry holds a [[DirectThreadBridge]], which answers that every thread is the render thread and runs every task at once; a backend whose `createThreadBridge()` returns `null` keeps it.

## Write your own when the engine owns the render thread

Return your own bridge from `createThreadBridge()` when the engine draws on a thread that does not register the backend, or already has a queue of tasks for its render thread:

```java file="com/example/backend/EngineThreadBridge.java" region="thread"
```

## Required

| Member | What it does |
|---|---|
| [[IThreadBridge#isRenderThread]] | Returns `true` on the thread that draws JOID. |
| [[IThreadBridge#execute]] | Runs a task on the render thread, later, in the order of the calls. |

## Provided

Every entry point of the core that must run on the render thread goes through [[ThreadUtils#runOnRenderThread]]: it runs the task at once on the render thread, and calls `execute` from any other thread.
That covers [[JOID#open]], `JOID.close`, the [[JOID#onReady]] callbacks and the callbacks that a loading thread delivers to the UIs.
The user side of this rule is on [Threads](/use/learn/running/threads/).

## Guarantees

| Rule | Why |
|---|---|
| Run the tasks of `execute` before the next frame, in order. | A UI opened from another thread must appear on the next frame, and an open followed by a close must not swap. |
| Never run a task of `execute` on the calling thread when it is not the render thread. | The task touches the node tree and the GPU. |
| Answer `isRenderThread` without locking. | The core asks it on every callback. |
