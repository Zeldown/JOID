---
title: "Rebuilding the tree"
description: "Build the children of a node again when a signal changes, and keep a node behind a placeholder until its data is ready."
genre: learn
sidebar:
  order: 2
---

A setter changes a property of a node that exists.
When a change adds or removes nodes, such as a list that grows, the children of a node must be built again.
When the data is not there yet, a node must wait for it.

**You will learn**
- How to build the children again when a signal changes
- How to choose what a change does to a node
- How to keep a node behind a placeholder until its data is ready

## Build the children again when a list changes

The list lives in a [[ListSignal]]:

```java file="com/example/state/RebuildUI.java" region="signal"
```

The [body](/use/learn/describing-the-ui/nodes/) of the list builds one row per item, and [[Node#watch]] runs it again on each change of `items`:

```java file="com/example/state/RebuildUI.java" region="watch"
```

The buttons only change the signal:

```java file="com/example/state/RebuildUI.java" region="buttons"
```

<figure>

![The blue buttons "Add" and "Remove" above two dark grey rows, "First item" and "Second item"; a click on Add adds the row "Item 3", and a click on Remove takes it away](/captures/state-rebuild.webp)

<figcaption>
At each click, the watch removes the rows and builds them again from `items`.
</figcaption>
</figure>

A body runs once, when the node is created.
Without the `watch` line, "Add" changes the signal and nothing on screen.
With it, each change of `items` runs `CLEAR_CHILDREN`, which removes the children, then `BODY`, which runs the body again.

The body reads the list with `peek()`, which reads without following: the watch already decides when the body runs again.
Read with `peek()` what only decides which nodes the body builds, and keep `get()` for the values that a setter follows.

Use `watch` only when nodes come and go.
A text, a color, a size or a visibility that depends on a signal follows it through its setter ([Signals](/use/learn/interactivity/signals/)), as the `visible(...)` of the "Remove" button does.

## Choose what a change does

`watch(signal, properties...)` applies each [[WatchProperty]] in the order you list them, on each change of the signal:

| Property | What it does |
|---|---|
| `WatchProperty.CLEAR_CHILDREN` | Removes every child of the node |
| `WatchProperty.BODY` | Runs the body of the node again |
| A lambda `(node, signal) -> ...` | Runs your own action with the node and the signal |

`WatchProperty` is a functional interface, so an action of your own is a lambda:

```java fragment
.watch(this.size, (node, signal) -> node.width(60D + this.size.peek() * 60D))
```

[[Node#onWatch]] calls your listener after the properties of each watch of the node ran; `watch(signal)` with no property only calls it.
When a property throws, JOID warns once and the next properties of the watch still apply.

`watch(signal, condition, properties...)` tests the condition on each change, and ignores the change while the condition returns `false`.
A watch listens while its node is in an open UI: when the node comes back after a change that it missed, the watch applies once.

To build again on several signals, watch one derived value that reads them all ([Derived values](/use/learn/state/derived-values/)): the body runs once per change of the result, whatever the number of signals that changed.

## Wait for data before showing a node

A profile comes from a server, a file or a long computation.
Until it arrives, the node that shows it waits, and a placeholder takes its place.

The signal starts without a value:

```java file="com/example/state/WaitUI.java" region="signal"
```

[[Node#wait]] gives the condition the node waits for, and [[Node#skeleton]] gives the node drawn in its place meanwhile:

```java file="com/example/state/WaitUI.java" region="wait"
```

Here, a [task](/use/learn/state/tasks/) of the UI sets the value after two seconds:

```java file="com/example/state/WaitUI.java" region="load"
```

<figure>

![A blue rectangle in place of the profile card, replaced after two seconds by a dark card with the text "Name: Ada Lovelace"](/captures/state-wait.webp)

<figcaption>
While `profile` holds no value, JOID draws the skeleton in place of the card; once it holds one, the card and its children appear.
</figcaption>
</figure>

The body runs at once, like any body: the text exists from the start and follows `profile`.
Until every condition passes, the node is not mounted: JOID draws the skeleton in place of the node and its children, and the skeleton receives the mouse in its place.
JOID tests the conditions on every frame, so a node waits for anything you can test: a signal that holds a value, a future that completed, a moment on the clock.
When a condition fails again later, the skeleton comes back.

A node with several `wait(...)` mounts when they all pass, and a node whose parent is not mounted is not mounted either.
[[Node#isMounted]] tells whether the node and its parents passed their conditions, and [`onMount`](/use/learn/describing-the-ui/node-lifecycle/) runs each time the node mounts.

Without a skeleton, the node draws its loading shape instead: a rectangle, a circle for a [[CircleNode]], in [[Color#LOADING]], a grey that pulses slowly ([Colors](/use/learn/style-and-motion/styling/#use-the-colors-that-change-on-their-own)).
A [[FlexNode]], a [[GridNode]] or a [[ContainerNode]] draws nothing, so the loading shapes of its children show the layout to come.

## Recap

- **`watch`** applies its properties on each change of a signal; `CLEAR_CHILDREN` then `BODY` builds the children again.
- Use `watch` only when **nodes come and go**; a value that depends on a signal follows it through its setter.
- In the body, read with **`peek()`** what decides which nodes to build.
- A **`WatchProperty`** is a lambda `(node, signal) -> ...`; a condition makes the watch ignore the changes while it returns `false`.
- **`wait`** keeps a node unmounted until its predicate returns `true`, tested on every frame.
- The **skeleton** takes the place of a waiting node; without one, the node draws its loading shape.

## Try it

**Numbered rows.**
In `RebuildUI`, number the rows: "1. First item", "2. Second item"…

<details>
<summary>Solution</summary>

The loop can count: the body runs again on each change, so the numbers follow.

```java fragment
for (int index = 0; index < this.items.peek().size(); index++) {
	final String text = (index + 1) + ". " + this.items.peek().get(index);
```

Then build the row text with [[Text#create]]: `Text.create(text, info)`.

</details>

**A minimum wait.**
In `WaitUI`, keep the skeleton for at least three seconds after the UI opens, even when the profile arrives sooner.

<details>
<summary>Solution</summary>

Add a second condition, fed by a task:

```java fragment
final BooleanSignal shown = new BooleanSignal(false);
Task.create(3000L).onRun(event -> shown.set(true)).attach(this);
```

Then add `.wait(card -> shown.get())` to the card: it mounts when both conditions pass.

</details>
