---
title: "Tasks and background updates"
description: "Run code after a delay or on a period with a task, and update signals from another thread."
genre: learn
sidebar:
  order: 4
---

Some values change with time: a clock, a message that hides itself, a result that a worker thread computes.
A task runs your code on the render thread after a delay or on a period, and a signal set from another thread hands its value to the interface at the next frame.

**You will learn**
- How to run code on a period with a task
- How to run code once after a delay, and restart that delay
- How long a task lives
- How to set a signal from another thread

## Run code on a period

A [[Task]] runs its [[Task#onRun]] listeners once its delay has passed, and again after each period given to [[Task#every]]:

```java file="com/example/state/TimerUI.java" region="every"
```

<figure>

![A dark card reading "Open for 0 s" above a blue button "Save"; the count grows by one each second, a click on Save shows "Saved" next to the button, and "Saved" disappears three seconds later](/captures/state-tasks.webp)

<figcaption>
The periodic task counts the seconds since the opening.
A click on "Save" shows "Saved", and a second task hides it three seconds later.
</figcaption>
</figure>

`Task.create(delay)` takes the delay in milliseconds, and `every(period)` the period in milliseconds; without `every`, the task runs once.
`attach(ui)` or `attach(node)` starts the delay, and the owner runs the task at the start of a frame that it draws, on the render thread, with [[UI#getCurrent]] set to its UI.
The time is the clock of JOID, the one that drives animations, so a [snapshot test](/use/learn/running/testing/) moves it with its `wait` command.

A frame that comes late runs a due task once, never twice: a period counts from the run before.

## Run code once, after a delay

A one-shot task detaches itself after its run.
Attaching it again starts its delay over, so one task hides a message three seconds after the last click, however many clicks came before:

```java file="com/example/state/TimerUI.java" region="once"
```

```java file="com/example/state/TimerUI.java" region="click"
```

[[Task#detach]] cancels a task before it runs, and stops a periodic one; a listener stops its own task with `event.getTask().detach()`.

## Know how long a task lives

A task runs while its owner is drawn:
- attached to a UI, while the UI is open and drawn; [[UI#getTaskList]] lists them;
- attached to a node, while the node is in an open UI: it does not run while the node is detached, but its timer keeps counting, so a run that fell due meanwhile happens on the first frame after the node comes back.

A task attached in `init()` lasts one opening: closing the UI or [reloading](/use/get-started/dev-tools/) it detaches the task, and the next `init()` attaches a fresh one.
A task attached to the UI in its constructor comes back at each opening, with its delay started over.

## Set a signal from another thread

JOID draws and calls your listeners on the render thread ([Threads](/use/learn/running/threads/)).
A download or a long computation belongs on another thread, and its result comes back through a signal:

```java file="com/example/state/LoaderUI.java" region="load"
```

```java file="com/example/state/LoaderUI.java" region="fetch"
```

<figure>

![A dark card with a blue button "Load" above the text "Not loaded"; the pointer clicks Load, and the text reads "Loading...", then "Loaded"](/captures/state-loader.webp)

<figcaption>The future completes on a worker thread, and the text shows its result at the next frame.</figcaption>
</figure>

A `set` made on another thread does not change the signal at once: JOID applies it on the render thread at the start of the next frame, then runs the listeners there.
The writes of one thread apply in their order, an `increment()` applies to the value that the signal holds at that time, and the writes inside one `batch` apply together.

On the worker thread, the signal still holds its old value right after the `set`:

```java fragment
this.status.set("Loaded");
this.status.peek(); // ✗ still "Loading..." on the worker thread, until the next frame
```

Hand the value itself to the code that needs it, and let the nodes read the signal.
A write from another thread reaches signals only: changing a node or opening a UI from there goes through [[ThreadUtils#runOnRenderThread]] ([Threads](/use/learn/running/threads/)).

## Recap

- A **`Task`** runs its `onRun` listeners after its delay, once, or on the period of **`every`**, in milliseconds of the JOID clock.
- A task runs on the **render thread**, while its UI or its node is drawn.
- **Attaching** a task again starts its delay over; **`detach()`** cancels it.
- A task attached in **`init()`** lasts one opening; one attached in the constructor comes back at each opening.
- A **`set` from another thread** applies at the next frame, on the render thread, in order.

## Try it

**A countdown.**
Show "Starts in 3", "Starts in 2", "Starts in 1", then "Go".

<details>
<summary>Solution</summary>

A periodic task counts an [[IntegerSignal]] named `left` down from 3, and stops itself:

```java file="com/example/state/CountdownUI.java" region="countdown"
```

Then show `left.map(value -> value > 0 ? "Starts in " + value : "Go")`.

</details>

**A status from a thread.**
A thread of yours parses a file and must show "Parsed 120 lines".
What does the thread call?

<details>
<summary>Solution</summary>

`status.set("Parsed " + count + " lines")` on a signal that a text node follows: JOID applies it at the next frame.

</details>
