---
title: "Drag and drop"
description: "Make a node draggable inside an area, drop it on targets, drag a copy, and drag it from a handle only."
genre: learn
sidebar:
  order: 4
---

A window that the user moves, a card that goes into a slot, an item dragged into an inventory: one setter makes a node follow the pointer.
This page shows where the node may go, how it lands on a target, and how to drag a copy.

**You will learn**
- How to make a node draggable inside an area
- How to react when the drag starts, moves and ends
- How to drop a node on the nearest target
- How to drag a copy and keep the original in place
- How to allow the drag from a handle only

## Make a node draggable

[[Node#draggable]] takes a [[DraggableProperty]]:

```java file="com/example/interactivity/drag/DragUI.java" region="drag"
```

<figure>

![The pointer presses a blue square at the top left of a dark card, drags it to the bottom right corner and releases it, then drags it back to its place; the text goes from "Drops: 0" to "Drops: 2"](/captures/interactivity-drag.webp)

<figcaption>
The square follows the pointer with a short easing, and never leaves its parent.
</figcaption>
</figure>

A press of the left button on the node starts the drag, and consumes the press.
While the button is down, the node eases toward the pointer; after the release it finishes its way and stops.
The node moves for good: its position, and a signal bound to `x(...)` or `y(...)`, take the new value.
While it is dragged, the node is drawn above its siblings and receives the pointer first; it goes back to its place in the order at the drop.

## Choose where the node may go

The factory of the property names the area that holds the node:

| Factory | The node stays inside |
|---|---|
| `DraggableProperty.parent()` | Its parent node |
| `DraggableProperty.node(other)` | Another node |
| `DraggableProperty.custom(x, y, width, height)` | A rectangle of the design canvas |
| `DraggableProperty.ui()` | The design canvas, 1920 by 1080 |
| `DraggableProperty.screen()` | The part of the window that shows the UI |
| `DraggableProperty.free()` | Nothing: it goes anywhere |

<figure>

![The pointer drags the blue square of the first example past the right edge of the dark card, then past its bottom edge: the square stops against each edge while the pointer goes on, then follows the pointer back to its place](/captures/interactivity-drag-bounds.webp)

<figcaption>
With `parent()`, the pointer may leave the card, the square stays inside it.
</figcaption>
</figure>

A node dragged with `parent()` must have a parent: on a node attached to the UI itself, the drag throws an `IllegalStateException`.
The node moves through [[Node#move]], so [[Node#beforeMove]] can refuse a position ([Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens)).

## React to the drag

The square of the first example counts its drops:

```java file="com/example/interactivity/drag/DragUI.java" region="drop"
```

| Event | When it runs |
|---|---|
| [[Node#beforeDragStart]] | The press asks for a drag; `cancel()` refuses it |
| [[Node#onDragStart]] | The drag started |
| [[Node#onDrag]] | The pointer moved during the drag |
| [[Node#beforeDragDrop]] | The button was released; `cancel()` sends the node back where the drag started |
| [[Node#onDragSnap]] | The node was dropped on a target ([Drop on a target](#drop-on-a-target)), before `onDragDrop` |
| [[Node#onDragDrop]] | The node was dropped |

Each listener receives a [[NodeDragEvent]]; `getTarget()` is the target of the drop, or `null`.
[[Node#isDragging]] is `true` from the start to the release: in `beforeDragDrop`, the drag is already over and it reads `false`.

[[Node#startDragging]] and [[Node#stopDragging]] start and end a drag from your code, with the same events.

## Drop on a target

`snap(...)` gives the property its targets.
At the drop, the node slides onto the nearest target, centered on it:

```java file="com/example/interactivity/drag/SnapUI.java" region="snap"
```

<figure>

![The pointer drags a blue square from the top of a dark card and releases it between the dark grey slots B and C, closer to C; the square slides into the slot C and the text reads "Slot: C"](/captures/interactivity-snap.webp)

<figcaption>
The square lands on the slot whose center is the nearest to its own.
</figcaption>
</figure>

The [[DraggableProperty.DraggableSnapType]] decides which target takes the node:

| Snap type | Target |
|---|---|
| `NEAREST` | The target whose center is the nearest; there is always one |
| `OVERLAP` | The first target that the node overlaps; with none, the node goes back where the drag started |

The node only slides to the target: your `onDragSnap` listener decides what the drop means, such as moving an item into a slot of your data.

## Leave the original in place

With [[DraggableProperty.DraggableType]]`.COPY`, the user drags a copy and the original stays where it is:

```java file="com/example/interactivity/drag/CopyUI.java" region="copy"
```

<figure>

![The pointer drags a copy of a blue "Sword" chip into a dark grey slot; at the release, the copy disappears and a "Sword" chip stays in the slot, while the original is still on the left](/captures/interactivity-copy.webp)

<figcaption>
The copy disappears at the drop: the listener of `onDragSnap` puts a chip of its own in the slot.
</figcaption>
</figure>

JOID draws the copy above every node of the UI, under the pointer.
The copy disappears at the drop, whatever happens: the drop does nothing to your tree unless a listener changes it.
The events run on the original node.

## Drag from a handle only

[[DraggableProperty#enabled]] takes a predicate that JOID tests when a drag would start.
This window moves only from its blue bar:

```java file="com/example/interactivity/drag/HandleUI.java" region="handle"
```

<figure>

![The pointer presses the dark grey body of the Window card and moves: nothing moves; over the blue bar, the pointer becomes four arrows, and a drag from the bar moves the window down and to the right, then a second one brings it back](/captures/interactivity-handle.webp)

<figcaption>
Only a press on the bar starts a drag: JOID tests `enabled` at the press.
</figcaption>
</figure>

A drag that started goes to its end, drop animation included, even when the pointer leaves the bar during a fast move.
[[DraggableProperty#disabled]] turns the drag off, and `draggable(...)` again replaces the property.

## Know how a drag writes the position

:::caution
A listener that writes the bound `x` signal on every change, for example to round it, stops the easing on `x` at its first write.
Round the position once the node is dropped, with `move(...)` in [[Node#onDragDrop]], or give the node snap targets.
:::

A node dragged with `DraggableType.MOVE` must have a position that the drag can write: a `x(...)` or `y(...)` given as a lambda or as a signal that cannot be written throws an `IllegalStateException`.
Give it a value or a writable signal, or drag a copy.

## Recap

- **`draggable(property)`** makes a node follow the left button from a press on it to the release, inside the **area** of the property.
- The node moves for good and is **drawn above its siblings** during the drag.
- **`beforeDragStart`** and **`beforeDragDrop`** can refuse the start or send the node back; **`onDragStart`**, **`onDrag`**, **`onDragDrop`** and **`onDragSnap`** tell what happened.
- **`snap(targets...)`** slides the dropped node onto a target, the nearest one or the overlapped one (`OVERLAP`).
- **`DraggableType.COPY`** drags a copy that disappears at the drop; the listeners decide what it leaves.
- **`enabled(predicate)`** decides whether a drag can start, for example from a handle.

## Try it

**Count the drops in each slot.**
In `SnapUI`, count how many times the square landed in each slot.

<details>
<summary>Solution</summary>

A [[MapSignal]] keeps one count per slot name:

```java fragment
.onDragSnap(event -> {
	final String name = event.getTarget().getLabel().getText();
	counts.put(name, counts.peek().getOrDefault(name, 0) + 1);
})
```

</details>

**Back home on a miss.**
In `SnapUI`, send the square back to its start when the user drops it far from every slot.

<details>
<summary>Solution</summary>

`OVERLAP` keeps only the slots that the square covers, and sends it back when it covers none:

```java fragment
.draggable(DraggableProperty.parent().snapType(DraggableSnapType.OVERLAP).snap(left, middle, right))
```

</details>
