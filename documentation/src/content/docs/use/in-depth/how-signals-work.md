---
title: "How signals work"
description: "How a signal records who reads it, when a derived value computes again, and how a setter follows a plain Java expression."
genre: in-depth
sidebar:
  order: 2
---

You need this page only if you want to know why an expression is followed or stays fixed, or what a signal costs.
[Signals](/use/learn/interactivity/signals/) and [Derived values](/use/learn/state/derived-values/) teach how to use them.

<figure>

![Three columns of boxes. 1. Compute: total.get() runs price * quantity, records price v1 and quantity v1, and keeps 12 with the two versions. 2. Change: quantity.set(4) stores a new value, the version of quantity grows to v2, and total is marked stale only if it has subscribers. 3. Read again: total.get() on the next draw finds quantity v2 instead of v1, so a version differs, and total computes 24; the next reads return 24 at once.](/captures/depth-signal-tracking.webp)

<figcaption>
A signal counts its changes; a derived value remembers the versions it read and computes again only when one of them moved.
</figcaption>
</figure>

## Versions and reads

A [[Signal]] holds a value and a version.
`set(...)` stores a value that differs from the current one (`equals` decides) and increments the version; an equal value changes nothing.

`get()` returns the value and reports the read to the computation in progress on the current thread.
`peek()` returns the value without reporting it.

## How a derived value computes

A [[ComputedSignal]], made by `map(...)` or [[Signal#from]], runs its function while it records the signals that the function reads, with their versions.
It keeps the result and these versions:

1. When it is read again, it compares the version of each signal it read with the version it kept.
2. If none moved, it returns the kept result at once: a frame without change costs one comparison per dependency.
3. If one moved, it runs its function again and records the reads again, so a condition that reads other signals on another branch follows them from then on.
4. A result equal to the previous one keeps the version of the derived value, so the values derived from it find nothing to do.

The comparison goes down the chain: a value derived from a derived value first brings its source up to date, then compares versions.

## How subscribers hear a change

A derived value that nobody listens to is pulled: it computes only when something reads it.
A derived value with subscribers (`onChange`, a `watch`, a bound control) is pushed:

1. `set(...)` marks every derived value that has subscribers, or that one of them reads, as stale, at once and transitively.
2. When the change ends, or the whole [[Signal#batch]], each stale value computes again and notifies its subscribers only if its result changed.
3. A value that reads `a` and `b`, which both read `x`, computes once after a change of `x`, never with a mix of old and new values.

A signal holds a reference to a derived value only while that derived value is pushed.
A pulled derived value is referenced by its readers alone, so the garbage collector frees it with the last node that reads it.

## How a setter follows an expression

A setter can receive a [[Text]] built from a signal, as in `text(Text.create("Clicks: " + clicks.get(), info))`, not the signal itself.
JOID still follows it:

1. While JOID runs `init()` of a UI, the body of a node or the init of a node, every `get()` made outside a derived value goes to a short list of recent reads of the thread.
2. The value overload of a setter passes its value to `Signal.from(value)`.
   With no recent read, the value is a constant, and nothing more happens.
3. Otherwise, JOID finds the line of your code that called the setter, reads the bytecode of its class, and isolates the instructions that compute the argument.
4. JOID runs these instructions again: the result must equal the value received, and the run must read the same signals.
5. When both match, the expression becomes a derived value that runs these instructions on each change; otherwise the value stays fixed, and the dev jar prints `[JOID] … but cannot follow it: …` with the reason and the fix.

The rules of [What JOID follows](/use/learn/interactivity/signals/#what-joid-follows) come from this replay.
The replay runs outside your method: a field of your class is read again each time, and a local variable that holds a signal is found from the reads.
Any other local variable has no value in the replay: inside a text, JOID finds it in the text it received, but in a calculation or a condition nothing gives it back, and the expression stays fixed.
A lambda written inside the expression cannot be replayed, so such an expression stays fixed.

JOID reads the bytecode of a class once and keeps the analysis of each call, so the cost comes the first time a UI opens.
After that, a change costs the replay of the followed expressions that read the signal, and a frame without change costs one comparison of versions per followed value.

## Threads

The computation in progress and the list of recent reads are kept per thread, so a derived value depends only on the reads made on its own thread.
A `set(...)` made on another thread is sent to the render thread and applied at the start of the next frame ([The frame lifecycle](/use/in-depth/frame-lifecycle/)).

## What it means for your code

- Write values that depend on signals as expressions: [Signals](/use/learn/interactivity/signals/).
- Use `map` or `Signal.from(() -> ...)` when the replay cannot follow your code: [Derived values](/use/learn/state/derived-values/).
- Keep lambdas for values that change on every frame without a signal, such as the progress of a video.
- See when the nodes read their values in a frame: [The frame lifecycle](/use/in-depth/frame-lifecycle/).
