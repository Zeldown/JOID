---
title: "Layout"
description: "Who places the children of a node, how to group nodes, and how a node clips or scrolls what goes beyond it."
genre: learn
sidebar:
  order: 7
---

A child sits where you put it, relative to its parent, until a layout node places it.
This page gives the rule that says which nodes place their children, then shows how to group nodes, clip them and scroll them.

**You will learn**
- Which nodes place their children and which leave them alone
- How to group nodes without drawing anything
- How to clip what goes beyond a node
- How to scroll a node with the wheel and from your code
- How to react when a node scrolls or reaches its end

## Know who places a child

Every child is positioned from the top-left corner of its parent ([Nodes and the tree](/use/learn/describing-the-ui/nodes/)).
A node never moves nor resizes its children: a [[RectNode]] with three children draws them at the `x` and `y` you gave them.

A **layout node** places its children itself, on every frame:

- [[FlexNode]] lines them up in a row or a column ([Flex layout](/use/learn/describing-the-ui/flex-layout/)).
- [[GridNode]] fills rows of cells and wraps ([Grid layout](/use/learn/describing-the-ui/grid-layout/)).
- [[ReorderableFlexNode]] is a flex layout whose children the user drags into another order ([Reorderable lists](/use/learn/interactivity/reorderable-lists/)).

A layout node adds a layout position to the position of the child.
The `x` and `y` of the child stay an offset on top of it, so the child moves by them inside its slot.
A layout node gives no slot to a child that is not visible, nor to a child whose position is `ABSOLUTE` ([Position and size](/use/learn/describing-the-ui/position-and-size/)).

To place children by a rule of your own, write a node that overrides its layout hook ([Custom nodes](/use/learn/going-further/custom-nodes/)).

## Group nodes without drawing them

A [[ContainerNode]] draws nothing.
It holds children, so that they move, hide and clip together:

```java file="com/example/describing/layout/GroupUI.java" region="group"
```

<figure>

![On a dark card, two identical groups, the second one lower and further right: each is a dark grey disc followed by a blue bar and a shorter dark grey bar](/captures/describing-layout-group.webp)

<figcaption>
Each group is one container: its position moves the disc and the two bars at once.
</figcaption>
</figure>

Give the container the size of its content when you clip it, scroll it or test it under the mouse: these use its bounds.
A container sized 0 still draws its children, since a node does not clip by default.

## Clip what goes beyond a node

[[Node#overflow]] decides what happens to the children that go past the bounds of a node.
[[OverflowProperty#HIDDEN]] clips them:

```java file="com/example/describing/layout/OverflowUI.java" region="hidden"
```

[[OverflowProperty#NONE]], the default, lets them draw over the rest of the UI:

```java file="com/example/describing/layout/OverflowUI.java" region="none"
```

<figure>

![Two dark grey panels side by side on a dark card, each holding the same blue bar that is wider than the panel: on the left the bar stops at the edge of the panel, on the right it continues over the card](/captures/describing-layout-overflow.webp)

<figcaption>
The same child, clipped by `HIDDEN` on the left and drawn whole with `NONE` on the right.
</figcaption>
</figure>

A clipped part of a child is not drawn and does not receive the mouse.
With `NONE`, the node counts the children that go past it in its own extent, so a scrolling ancestor scrolls far enough to show them.

## Scroll a node

[[OverflowProperty#SCROLL]] clips the children like `HIDDEN` and lets the user scroll through them with the wheel:

```java file="com/example/describing/layout/FeedUI.java" region="scroll"
```

<figure>

![A dark grey panel shows the blue rows 1 to 4 of a numbered list, above a blue Top button that a later section uses; the wheel moves the list up until rows 4 to 7 show, then moves it back down](/captures/describing-layout-scroll.webp)

<figcaption>
The panel keeps its size: the wheel moves its eight rows inside it.
</figcaption>
</figure>

The node measures its content on every frame: the bottom and the right edge of its farthest child, `ABSOLUTE` children aside.
The gap of the first child from the top-left corner is kept after the last child too, so the content ends with the same margin it starts with.
[[Node#hasOverflowX]] and [[Node#hasOverflowY]] tell whether the content is larger than the node on each axis.

A flex or a grid created with a length of 0 takes the length of its content, and a length that you give it is a minimum that it grows past when the content needs more room.
With an overflow other than `NONE`, the length that you give is fixed and the content scrolls; a layout node of length 0 grows with its content and never scrolls.

One notch of the wheel scrolls 30 design pixels, times [[Node#scrollSpeed]] (`1D` by default), and twice that while Ctrl is held.
The vertical wheel scrolls horizontally a node whose content overflows only in width.
The content glides to its new offset over a few frames instead of jumping.

The wheel goes to the innermost scrolling node under the mouse that can still scroll; once that node is at its end, the node around it scrolls.

## Scroll from your code

The scroll offset of an axis goes from `0D`, the start, down to minus the overflow: at `-200D`, the content shows what lies 200 design pixels below its start.

```java file="com/example/describing/layout/FeedUI.java" region="top"
```

- [[Node#scrollTo]] scrolls both axes as the wheel does, so the scroll events below fire and a `beforeScroll` listener can refuse it.
- [[Node#scrollX]] and [[Node#scrollY]] set the offset of one axis, like any setter.
  Given a writable signal, they follow it and write back the offset that the user scrolls to ([Signals](/use/learn/interactivity/signals/)).
- [[Node#updateScroll]] jumps to the requested offset at once, without the glide; [[Node#updateScrollX]] and [[Node#updateScrollY]] do it for one axis.

JOID keeps every offset between `0D` and the end of the content.
A positive offset, such as `scrollY(40D)`, comes back to `0D`, with a warning in [dev mode](/use/get-started/dev-tools/): write `scrollY(-40D)`.
[[Node#getTargetScrollY]] returns the offset that the node glides to, [[Node#getScrollY]] the offset drawn this frame, and [[Node#getMaxScrollY]] the overflow, a positive number; the same getters exist for `X`.

## React to scrolling

Four listeners follow the scroll of a node, for both axes; the event names the axis with `getAxis()`:

- [[Node#beforeScroll]] fires before the offset changes; `cancel()` keeps the node where it is, and its scrollbar thumb with it, even under a drag.
- [[Node#onScroll]] fires once the offset changed; `getPrevious()` and `getOffset()` give both offsets.
- [[Node#onScrollToEnd]] fires when a scroll heads to the end of the content.
- [[Node#onScrollEndReached]] fires when the content arrives at its end, after the glide.

Append more children when the end is reached, and the list grows as the user reads it:

```java file="com/example/describing/layout/FeedUI.java" region="more"
```

The node measures its new content on the next frame, and the wheel scrolls further.

<figure>

![The same panel of numbered blue rows above the blue Top button; the wheel, shown in a badge at the bottom right, scrolls down to row 8 at the end of the list, rows 9 to 12 appear below it and the wheel scrolls on to row 10, then the pointer clicks Top and the list glides back up to row 1](/captures/describing-layout-feed.webp)

<figcaption>
The list grows each time its end is reached, and `scrollTo(0D, 0D)` glides back to the start as the wheel does.
</figcaption>
</figure>

[Events](/use/learn/interactivity/events/) explains listeners, their phases and cancellation.

## Add a scrollbar

[[Node#scrollbar]] gives a scrolling node a scrollbar, a control that shows the offset and that the user drags:

```java file="com/example/describing/layout/ScrollbarUI.java" region="scrollbar"
```

[[ScrollbarNode]] is an abstract control: its empty subclass gets its two parts, the track and the thumb, where it is created ([Controls](/use/learn/interactivity/controls/#bind-a-control-to-a-signal) explains this form).
Its shape sets its axis: a scrollbar taller than wide scrolls vertically, one wider than tall horizontally.
The scrollbar is positioned from the scrolling node, does not scroll with its content, and is drawn only while the content overflows on its axis.

<figure>

![The panel of eight numbered blue rows, with a thin dark track along its right edge and a blue thumb at its top; the wheel, shown in a badge, scrolls the list down to rows 4 to 7 and the thumb goes down the track with it; the pointer then drags the thumb back to the top and the list follows it up to row 1](/captures/describing-layout-scrollbar.webp)

<figcaption>
The thumb follows the wheel, and dragging the thumb scrolls the list.
</figcaption>
</figure>

Its reference page lists its options, and [Give your controls a look](/use/learn/going-further/ui-kit/) turns it into a reusable skin.

## Recap

- A node never moves its children; a **layout node** (`FlexNode`, `GridNode`, `ReorderableFlexNode`) places them on every frame, and their own `x` and `y` stay an **offset**.
- A **`ContainerNode`** groups nodes and draws nothing.
- **`overflow`** decides what goes past a node: `NONE` draws it, `HIDDEN` clips it, `SCROLL` clips it and scrolls.
- The wheel scrolls **30 design pixels** per notch times `scrollSpeed`; the content glides to its offset.
- Offsets go from **`0D` down to minus the overflow**; `scrollTo` scrolls like the user, `updateScroll` jumps.
- **`onScrollEndReached`** fires when the content arrives at its end: append more there.

## Try it

**Faster wheel.**
Make the feed of `FeedUI` scroll 90 design pixels per notch.

<details>
<summary>Solution</summary>

Thirty design pixels times 3:

```java fragment
.overflow(OverflowProperty.SCROLL)
.scrollSpeed(3D)
```

</details>

**Bottom button.**
Add a second button that jumps to the end of the feed, without the glide.

<details>
<summary>Solution</summary>

The offset is clamped to the end of the content, then `updateScroll()` skips the glide:

```java fragment
.onClick(event -> feed.scrollTo(0D, -feed.getMaxScrollY()).updateScroll())
```

</details>
