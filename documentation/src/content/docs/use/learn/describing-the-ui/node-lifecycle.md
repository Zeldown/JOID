---
title: "Node lifecycle"
description: "When a node joins a UI, lives there frame after frame and leaves it, and the listeners that run at each moment."
genre: learn
sidebar:
  order: 3
---

A node joins a UI, lives there for many frames, then leaves.
Listeners run your code at each of these moments: to start a counter when the node appears, to draw a decoration, to stop work when it goes.

**You will learn**
- Which moments a node goes through, in which order
- How to run code when a node joins its UI and on every frame
- How to know when a node is drawn for the first time
- How to draw around a node while it is drawn
- How to release what a node started when it leaves

## Follow a node from attach to remove

<figure>

![A flow on two rows. The top row goes from left to right: attach to a UI, onInit once per attach, onUpdate before drawing, then onDraw and onRender while drawing. A loop arrow goes from onDraw back to onUpdate, labelled every frame while attached. Below onDraw, a side box onMount, first drawn frame only, hangs from it; from the loop, a last arrow goes left to onDetach after remove, at the start of the bottom row.](/captures/describing-lifecycle-flow.webp)

<figcaption>
`onInit` and `onDetach` frame the life of a node; the listeners in between run on every frame.
</figcaption>
</figure>

| Listener | When it runs |
|---|---|
| [[Node#onInit]] | The node joins a UI: it is attached to the UI, or to a node that is in a UI. |
| [[Node#onUpdate]] | Every frame, before JOID draws anything. |
| [[Node#onMount]] | The first frame where the node is drawn: it is visible and every condition of `wait(...)` holds. |
| [[Node#onDraw]] | Every frame, right after the node drew itself, before the children drawn above it. |
| [[Node#onRender]] | Every frame, after the node, its children and its layers are drawn. |
| [[Node#onDetach]] | The node leaves its UI: it is removed, its parent is removed, or the UI closes. |
| [[Node#onChildAppend]] | A child is appended to the node. |

Each listener receives an event; `event.getNode()` returns the node.
A node that is not visible skips its drawing listeners (`onMount`, `onDraw`, `onRender`), but `onUpdate` still runs.

## Run code when a node joins its UI and on every frame

`onInit` prepares the state of a node, and `onUpdate` changes it on every frame:

```java file="com/example/describing/lifecycle/LifecycleUI.java" region="timer"
```

<figure>

![A dark card that shows in light letters how long it has been in the UI, in seconds; the time grows, then starts again at 0 when the UI opens again](/captures/describing-lifecycle-frames.webp)

<figcaption>
The time starts again at 0 each time the card joins the UI, here when the UI opens again.
</figcaption>
</figure>

[[UI#getFrameTime]] gives the time since the previous frame in milliseconds, so the card adds the elapsed seconds on every frame.

JOID first updates every node of every open UI, then draws them all.
Inside a UI, the children are updated before their parent, and the `update()` of the UI runs after its nodes.

A child attached to a node that is not in a UI yet waits: its `onInit` runs when its root joins the UI, before the `onInit` of its parent.
A child appended to a node already in a UI joins at once.

:::tip
Code that changes a value on every frame belongs in an animation more often than in `onUpdate`: [Animation](/use/learn/style-and-motion/animation/) shows how to tie a property to time.
:::

## Know when a node is drawn for the first time

`onMount` runs on the first frame that draws the node.
It comes after `onInit`, and it waits for the conditions of [[Node#wait]]: an image node, for example, waits until its image is loaded ([Images and media](/use/learn/describing-the-ui/media/)).
[Rebuilding the tree](/use/learn/state/rebuilding/) shows how to add your own conditions and what JOID draws in the meantime.

[[Node#isAttached]] tells whether the node is in a UI, and [[Node#isMounted]] whether its conditions hold.

## Draw around a node

`onDraw` runs in the coordinates where the node draws itself, so the node gives the position of what you draw:

```java file="com/example/describing/lifecycle/LifecycleUI.java" region="draw"
```

<figure>

![A dark card with the word Underlined in light letters and a blue line right under the word](/captures/describing-lifecycle-draw.webp)

<figcaption>
`onDraw` adds a line under the text, in the same coordinates as the text.
</figcaption>
</figure>

`onRender` runs later, once the children are drawn, to draw over them.
[Drawing shapes and text](/use/learn/going-further/drawing/) lists the shapes you can draw, and [Custom nodes](/use/learn/going-further/custom-nodes/) shows how to make a node that draws itself.

## Release what a node started

What `onInit` starts outside JOID, `onDetach` stops:

```java file="com/example/describing/lifecycle/LifecycleUI.java" region="release"
```

The card checks something every 5 seconds while it is in the UI, and stops when it leaves.
`onDetach` runs for the node and for each of its children, children first, when you remove the node, when you remove its parent and when the UI closes.

A node that leaves keeps its listeners: attached again, it runs `onInit` again, then `onMount` on its next drawn frame.
Moving a node to another parent detaches it, then attaches it again.

:::caution
The listeners of JOID run on the render thread, and the code of an executor does not.
Give the result back to the render thread before it changes a node: [Threads](/use/learn/running/threads/) shows how.
:::

JOID stops by itself what it owns: the signals that a node follows, its hover and drag state, and the tasks of the node ([Tasks and background updates](/use/learn/state/tasks/#know-how-long-a-task-lives)).

## Recap

- **`onInit`** runs each time the node joins a UI; **`onDetach`** runs each time it leaves.
- **`onUpdate`** runs on every frame, before anything is drawn; children update before their parent.
- **`onMount`** runs on the first drawn frame, once the conditions of **`wait(...)`** hold.
- **`onDraw`** runs after the node draws itself, **`onRender`** after its children.
- A **hidden node** skips the drawing listeners but still updates.
- The children of a detached branch **wait for their root** to join a UI.
- Stop in **`onDetach`** what you start in `onInit` outside JOID.

## Try it

**Count the visits.**
Show how many times the frame card of `LifecycleUI` joined the UI.

<details>
<summary>Solution</summary>

`onInit` runs at every attach: increment a field there instead of resetting it.

```java fragment
.onInit(event -> this.visits++)
```

</details>

**Pause the timer.**
Make the timer of `LifecycleUI` stop while the card is hidden with `visible(false)`.

<details>
<summary>Solution</summary>

`onUpdate` runs for a hidden node too, while the drawing listeners do not: add the frame time in `onDraw` instead.

```java fragment
.onDraw(event -> this.open += super.getFrameTime() / 1000D)
```

</details>
