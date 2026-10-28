---
title: "The frame lifecycle"
description: "What JOID does during one frame, in which order, and where your own code runs in it."
genre: in-depth
sidebar:
  order: 1
---

You need this page only if you want to know when your code runs: when a click reaches your listener, when `update()` runs, when a followed value takes its new value, and in which order the nodes draw.

<figure>

![Three columns of boxes joined by arrows. Each frame: Input, Resize, Tasks, Update, Background, Draw UIs, Present, where Input and Present are outlined because your loop runs them, as the legend at the bottom says. Draw UIs opens onto a column for each UI: Tasks, Backdrop, Transition, Nodes, Tooltip. Nodes opens onto a column for each node: Values, Layout, Hover, Scroll and drag, Effects, Draw.](/captures/depth-frame-lifecycle.webp)

<figcaption>
One frame zooms in twice: the frame, then each UI, then each node.
A column runs from top to bottom, on one thread.
</figcaption>
</figure>

## Your loop drives the frame

JOID has no loop and no thread of its own.
Your application owns the window and calls [[Backend#frame]] once per turn of its loop, between polling the window events and presenting the image ([Choosing a backend](/use/learn/running/backends/)).

The thread that registers the backend and calls `frame()` is the render thread.
Every step on this page runs on it, one after the other: nothing in a frame runs at the same time as your listeners.
[Threads](/use/learn/running/threads/) explains how work done on another thread gets back to it.

## Input arrives between two frames

Polling the window events hands each event to JOID at once, before `frame()`.
JOID offers it to the open UIs from the top one down ([Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack)), and inside a UI as [Events](/use/learn/interactivity/events/) explains.

Your input listeners (`handleMousePressed`, `onClick`, shortcuts…) therefore run before the frame, and what they change is drawn by the `frame()` call of the same turn.

## One frame, step by step

`frame()` runs these steps:

1. **Resize**, only when the size of the window changed since the last frame: every open UI fits its view to the window.
   `init()` does not run again: it runs once, when [[JOID#open]] adds the UI.
2. **Tasks**: the work that other threads sent to the render thread runs, in the order it was sent.
   A [[Signal]] written from another thread takes its value here.
3. **Update**: every open UI updates, hidden or not.
   It reads its own followed values, updates its nodes in their order, then runs its own `update()`.
   A node reads its followed values, fires `onAnimate` for each animator whose value changed, updates its children, then runs its own `update()` and its `onUpdate` listeners.
4. **Background**: the UI bridge with the lowest priority, by default the window UI bridge, clears the window to its background color.
5. **Draw UIs**: every UI bridge draws its visible UIs, from the lowest priority up ([Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own)).
   Inside a bridge, the overlays drawn under the screens come first, then the screens and popups, then the overlays drawn above them.
6. **End**: JOID closes the frame of the render bridge, and your loop presents the image.

Before it draws its UIs, a bridge also releases the GPU memory of the resources that the garbage collector reclaimed and of the off-screen images that no effect used for 5 seconds ([Performance](/use/in-depth/performance/)).

## Inside one UI

Drawing a UI runs, in order:

1. The [[Task]]s of the UI and of its nodes whose delay is over.
   A UI that is not drawn runs none.
2. The backdrop of a UI whose [[UIData]] sets `background = true`, faded by the opening or closing transition.
3. The start of a running [transition](/use/learn/style-and-motion/transitions/).
4. The nodes attached to the UI, in their order, each one with its whole subtree.
5. The ghost of a node being dragged, then the tooltip of the hovered node, above every node.
6. The end of the transition.

The UI reads the position of the mouse once, when it starts drawing: hover tests and tooltips of the whole frame use that position.

## Inside one node

Drawing a node runs, in order:

1. **Values**: the node and its children read their followed values again; a value that differs from the current one replaces it.
   This is when a setter given a lambda runs it, and when a setter given an expression that reads signals takes their new value ([When does JOID compute this value again?](/use/learn/interactivity/signals/#ask-when-does-joid-compute-this-value-again)).
2. **Layout**: a [layout node](/use/learn/describing-the-ui/layout/) places its children.
3. **Hover**: JOID tests whether the mouse is over the node and fires `onHoverStart` or `onHoverEnd` when the answer changes, and `onHover` while the mouse stays over it.
4. **Scroll and drag**: a scrolling container moves its content, a dragged node moves toward the mouse, and a node in motion is shifted by whole pixels ([Pixel alignment](/use/in-depth/pixel-alignment/)).
   `onMount` fires on the frame the node becomes mounted.
5. **Effects**: the [effects](/use/learn/style-and-motion/effects/) of the node open, by priority.
6. **Draw**: the children with a negative `zindex`, the node itself followed by its `onDraw` listeners, the other children, then its [layers](/use/learn/going-further/drawing/); the `onRender` listeners run once all of that is drawn.
7. The effects close, in reverse order.

A hidden node, or a node outside the visible area of a container that clips its content, skips all of these steps except the first; it still updates in step 3 of the frame.
[Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/) describes each listener; this list only places them in the frame.

## Where your code runs

| Your code | When it runs |
|---|---|
| Input listeners, shortcuts | When the event arrives, before the frame |
| `set(...)` on a signal, on the render thread | At once: the bound controls and the subscribers follow, and the nodes read it at their next update or draw |
| `set(...)` on a signal, from another thread | Tasks step of the next frame |
| `update()`, `onUpdate`, `onAnimate` | Update step, once per frame, before anything draws |
| The tasks of a UI | Start of the draw of the UI, once their delay is over |
| A lambda given to a setter | At the update and at the draw of its node, on every frame |
| `onHoverStart`, `onHover`, `onHoverEnd`, `onMount` | Draw of the node, before the effects |
| `onDraw`, `onRender`, layers | Draw of the node, inside the effects |

## What it means for your code

- Change state in listeners and in `update()`, and let the nodes read it: a value written in `update()` is drawn in the same frame.
- A lambda given to a setter runs on every frame for every node that uses it, hidden or not: keep it to reading and combining values ([Performance](/use/in-depth/performance/)).
- Draw by hand in `onDraw` or in a [layer](/use/learn/going-further/drawing/), never in `update()`: JOID draws only inside the draw step, and a draw call outside a frame throws.
- Every duration JOID measures comes from the [clock of JOID](/use/learn/running/testing/#keep-every-run-identical), never from the system.

:::caution
An exception thrown while a bridge draws ends the draw of its UIs for that frame: JOID prints it once and draws again on the next frame.
A draw that throws on every frame leaves the rest of that bridge undrawn.
:::
