---
title: "Reorderable lists"
description: "Let the user drag the items of a row or a column into a new order, from a handle or from anywhere, with pinned items."
genre: learn
sidebar:
  order: 8
---

A playlist, a list of tasks, the slots of a toolbar: the user drags an item to a new place, and the others make room.
A [[ReorderableFlexNode]] is a [flex layout](/use/learn/describing-the-ui/flex-layout/) whose children the user reorders.

**You will learn**
- How to make a list that the user reorders
- How to react when the order changes
- How to drag the items by a handle only
- How to pin items in place

## Let the user reorder a list

```java file="com/example/interactivity/reorder/PlaylistUI.java" region="list"
```

<figure>

![The pointer drags "Ocean" down a column of five dark grey items, the dragged one lighter and casting a shadow; the items below it slide up to make room, and at the release "Ocean" lands in fourth place and the text reads "Moved: Ocean from 1 to 3"; a second drag puts it back in second place, and the text reads "Moved: Ocean from 3 to 1"](/captures/interactivity-reorder.webp)

<figcaption>
The neighbors make room during the drag; the list changes only at the release.
</figcaption>
</figure>

[[ReorderableFlexNode]] has the factories and the settings of a [[FlexNode]]: `vertical(x, y, width)`, `horizontal(x, y, height)`, `margin(...)`, `align(...)`.

- A press of the left button on a child starts the drag; the child follows the pointer along the axis of the list only, drawn above the others.
- The other children slide to make room as soon as the dragged one passes half of a neighbor.
- At the release, the child eases into its slot, and the order of the children changes for good.
- Near an edge of a parent that scrolls, the drag scrolls it.

Here each row casts a shadow while the list reports it dragged, so that it stands out from the neighbor that it covers.

A child that consumes the press, such as a button or a text field, keeps the drag from starting there.
[[ReorderableFlexNode#autoDrag]] `(false)` turns the drag on press off for the whole list.

## React to a new order

The playlist of the first example writes the move once the drag ends:

```java file="com/example/interactivity/reorder/PlaylistUI.java" region="end"
```

| Event | When it runs |
|---|---|
| [[ReorderableFlexNode#onReorderStart]] | A child starts to be dragged |
| [[ReorderableFlexNode#beforeReorder]] | The dragged child is about to take a new slot; `cancel()` keeps it in its slot |
| [[ReorderableFlexNode#onReorder]] | The dragged child took a new slot, during the drag |
| [[ReorderableFlexNode#onReorderEnd]] | The drag ended, and the order is final |

Each listener receives a [[NodeReorderEvent]]: `getChild()` is the dragged child, `getPrevious()` its index before and `getIndex()` its index after.
Save the new order in `onReorderEnd`: it runs once per drag, also when the list or the child is removed during the drag.
The dragged child runs its own [drag events](/use/learn/interactivity/drag-and-drop/#react-to-the-drag), `onDragStart` to `onDragDrop`, and its `beforeDragStart` can refuse the drag.

During a drag, [[ReorderableFlexNode#getLogicalOrder]] gives the order that the children take at the release, [[ReorderableFlexNode#isDragging]] tells which child is dragged, and [[ReorderableFlexNode#getChildIndex]] gives the slot of a child.

## Drag by a handle only

With `autoDrag(false)`, a handle starts the drag with [[ReorderableFlexNode#startDrag]]:

```java file="com/example/interactivity/reorder/HandleListUI.java" region="handle"
```

<figure>

![A column with a blue "To do" header and three dark grey tasks, each with a blue handle on its right; a drag that starts on the "Write" task moves nothing, a drag on the handle of "Ship" moves it above "Review", and a second drag puts it back](/captures/interactivity-reorder-handle.webp)

<figcaption>
A press on a handle drags its task; a press anywhere else on the task does nothing, and the header never moves.
</figcaption>
</figure>

The handle receives every press of the UI, so it tests `isHovered()` before it starts the drag ([Hear the raw input](/use/learn/interactivity/events/#hear-the-raw-input)).
[[ReorderableFlexNode#endDrag]] ends a drag from your code.

## Pin items in place

The header of `HandleListUI` stays first, whatever the user drags:

```java file="com/example/interactivity/reorder/HandleListUI.java" region="pin"
```

[[ReorderableFlexNode#pin]] keeps children in their slot: the user cannot drag them, and the other children move around them.
[[ReorderableFlexNode#unpin]] frees them again, and [[ReorderableFlexNode#isPinned]] tells whether a child is pinned.

## Recap

- A **`ReorderableFlexNode`** is a row or a column whose children the user **drags to a new slot**, with the settings of a `FlexNode`.
- The order changes for good **at the release**; **`onReorderEnd`** gives the old and the new index, once per drag.
- A child that **consumes the press** cannot be dragged by it; **`autoDrag(false)`** with **`startDrag`** drags from a handle.
- **`pin`** keeps children in their slot.

## Try it

**A horizontal toolbar.**
Turn `PlaylistUI` into a row of five square slots of 80 by 80.

<details>
<summary>Solution</summary>

The horizontal factory takes the height of the row:

```java fragment
ReorderableFlexNode.horizontal(60, 40, 80).margin(10D)
```

Each child is a [[RectNode]] of 80 by 80.

</details>

**Keep the last one.**
In `PlaylistUI`, keep "Outro" at the end of the list.

<details>
<summary>Solution</summary>

Pin it once the children exist:

```java fragment
list.pin(list.getChildren().get(list.getChildren().size() - 1));
```

</details>
