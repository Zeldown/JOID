---
title: "Threads"
description: "Know which thread JOID runs on, and bring the results of other threads back to it."
genre: learn
sidebar:
  order: 3
---

JOID is not thread-safe: it reads your nodes while it draws, without locks.
Slow work belongs on other threads, and its results must come back to the one thread JOID uses.

**You will learn**
- Which thread JOID runs on, and what other threads may call
- How to bring a result back from a worker thread
- How to wait for a future on the render thread
- Where to run code later or periodically

## Know which thread JOID runs on

The **render thread** is the thread that calls `register()` on the backend, then `frame()` in your loop.
JOID calls your callbacks there (`init()`, `onClick`, `onChange`…), so code inside them can change anything.

Nodes, UIs and drawing belong to the render thread: create and change them there only.
A change from another thread can land in the middle of a frame and draw half of it, or get lost.

A few calls are safe from any thread, because they hand their work to the render thread by themselves:

| Call | From another thread |
|---|---|
| [[JOID#open]], `JOID.close`, `JOID.forceOpen`, `JOID.forceClose` | open or close the UI at the start of the next frame |
| [[JOID#onReady]] | runs the callback at the start of the next frame, once JOID is ready |
| `set(...)` and the other writes of a [[Signal]] | apply the write at the start of the next frame ([Tasks](/use/learn/state/tasks/#set-a-signal-from-another-thread) explains what that means for your state) |
| [[ThreadUtils#runOnRenderThread]] | runs your task at the start of the next frame |

## Bring a result back from a worker thread

A download, a disk read or a long computation on the render thread freezes every UI until it ends.
Run it on another thread.
A result that goes into a signal needs nothing more, since the write moves to the render thread:

```java file="com/example/running/ThreadsUI.java" region="signal"
```

A result that changes a node directly must not touch it from the worker thread:

```java file="com/example/running/ThreadsUI.java" region="wrong"
```

### Bring a result back to the render thread

Hand the change to [[ThreadUtils#runOnRenderThread]]:

```java file="com/example/running/ThreadsUI.java" region="render-thread"
```

On the render thread, `runOnRenderThread(task)` runs the task at once.
From another thread, it queues the task, and the next frame runs the queue first, before the UIs update, in the order the tasks arrived.
A task that throws prints `[JOID] A task sent to the render thread from another thread failed (…)` once, and the next tasks still run.

## Wait for a future on the render thread

The stages of a `CompletableFuture` run on the thread that completes it, often a worker thread.
[[RenderThreadFuture#from]] wraps a future so that it completes on the render thread instead, and so do the stages that follow it:

```java file="com/example/running/ThreadsUI.java" region="future"
```

`join()` or `get()` of a `RenderThreadFuture`, called on the render thread, waits for the source future and completes at once, without waiting for the next frame.
Waiting there freezes every UI until the work ends, so keep it for startup.
A stage such as `exceptionally` or `whenComplete` receives the exception that failed the work, never a `CompletionException` around it.

JOID follows the same rule for its own loads: the future of [[MsdfFontLoader#load]] is a `RenderThreadFuture`, and `onLoad` and `onError` of a [[Resource]] run on the render thread.

:::caution
The render thread runs the queue only when your loop calls `frame()`.
A program that registers the backend, then waits with `join()` on another thread before its loop starts, never sees the future complete.
:::

## Run code later or periodically

A [[Task]] runs code after a delay, once or periodically, on the clock of JOID and on the render thread, while it stays attached to a UI or a node.
[Tasks](/use/learn/state/tasks/#run-code-on-a-period) shows how to create and attach one.

## Recap

- The **render thread** registers the backend and calls `frame()`; JOID calls your callbacks there.
- Change nodes and UIs **only on the render thread**.
- `JOID.open`, `JOID.close`, `JOID.onReady` and **signal writes** are safe from any thread: they apply at the start of the next frame.
- From a worker thread, hand a node change to **`runOnRenderThread(task)`**.
- **`RenderThreadFuture.from(future)`** completes a future, and the stages after it, on the render thread.
- A `Task` runs code later or periodically, on the clock of JOID ([Tasks](/use/learn/state/tasks/)).

## Try it

**A long computation.**
A button starts a computation that takes two seconds, and a text node must show `Done` at the end.
Write the click callback, with `label` a [[TextNode]] of the UI.

<details>
<summary>Solution</summary>

```java fragment
.onClick(event -> CompletableFuture
	.runAsync(this::compute)
	.thenRun(() -> ThreadUtils.runOnRenderThread(() -> label.text(Text.create("Done", info)))))
```

The computation runs on a worker thread, and the text node changes on the render thread.
With a signal that the text node reads, `.thenRun(() -> done.set(true))` is enough.

</details>

**A font that never arrives.**
After `register()`, a program calls `MsdfFontLoader.load(file).join()` on a worker thread before its loop starts, and the worker waits forever.
Why?

<details>
<summary>Solution</summary>

The future completes on the render thread, at the start of a frame, and no frame runs before the loop.
Call `join()` on the render thread itself, which completes it at once, or start the loop and use the font in `thenAccept`.

</details>
