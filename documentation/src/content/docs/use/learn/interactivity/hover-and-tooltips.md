---
title: "Hover, tooltips and cursors"
description: "Change a node under the pointer with an animation, react when the pointer enters and leaves, show tooltips and set the cursor."
genre: learn
sidebar:
  order: 3
---

The pointer over a node is the first sign that the node does something.
JOID tracks which node the pointer hovers, animates the change, and shows a tooltip or a cursor of your choice.

**You will learn**
- How to change the look of a node under the pointer, with an animation
- Which nodes count as hovered
- How to react when the pointer enters and leaves a node
- How to show a tooltip made of text, of nodes or of your own drawing
- How to set the cursor over a node

## Change a node under the pointer

[[RectNode#hoveredColor]] gives the color of the node under the pointer.
JOID blends the two colors over [[Node#hoverDuration]] milliseconds, along the curve of [[Node#hoverEquation]]:

```java file="com/example/interactivity/hover/HoverUI.java" region="color"
```

<figure>

![The pointer moves onto a dark grey "Shop" button, which turns a lighter grey while its label stays readable, then leaves it, and the button turns dark grey again](/captures/interactivity-hover.webp)

<figcaption>
The color follows the hover progress, both when the pointer enters and when it leaves.
</figcaption>
</figure>

The hover progress runs from `0F` (not hovered) to `1F` (hovered).
[[Node#hoverValue]] multiplies a value by it, to animate any property of your own.
It reads the hover animation of the node, a signal: in a node of your own, an expression that calls `super.hoverValue(...)` follows it.
On a node held in a local variable, as here, the expression stays fixed, so give the setter a lambda:

```java file="com/example/interactivity/hover/HoverUI.java" region="value"
```

<figure>

![A blue "Settings" button with a blue line under it that grows from the left while the pointer stays on the button, and shrinks back once the pointer leaves](/captures/interactivity-underline.webp)

<figcaption>
The underline is `0` px wide at rest and `220` px wide once the button is hovered.
</figcaption>
</figure>

[[CircleNode]] and [[ResourceNode]] have a `hoveredColor` too.
The [easing curves](/use/learn/style-and-motion/animation/) of [[TweenEquations]] all work as a `hoverEquation`.

## Know which node is hovered

The pointer hovers the frontmost node under it that is interactive, and every parent of that node: [[Node#isHovered]] is `true` for all of them.
[[UI#getHoveredNode]] returns the frontmost one, or `null`.

- A node set with `interactive(false)` lets the pointer through to the node behind it ([Events](/use/learn/interactivity/events/#let-the-pointer-through-a-decoration)).
- A node set with `enabled(false)` is hovered, but its hover animation stays at rest.
- Only the top screen or popup, and the top overlay that takes input, have hovered nodes ([How UIs stack](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack)).

## React when the pointer enters and leaves

```java file="com/example/interactivity/hover/EnterUI.java" region="events"
```

<figure>

![The pointer moves across a dark grey area and a blue dot follows it; the text under the area goes from "Entries: 0" to "Entries: 1" as the pointer enters](/captures/interactivity-enter.webp)

<figcaption>
The dot follows the pointer through `onHover`, and goes back to the center through `onHoverEnd`.
</figcaption>
</figure>

- [[Node#onHoverStart]] runs once when the node becomes hovered.
- [[Node#onHover]] runs on every frame while it stays hovered.
- [[Node#onHoverEnd]] runs once when it stops, also when the node is hidden or detached under the pointer.

Each listener receives a [[NodeHoverEvent]] with the position of the pointer in design pixels.
These events run for a disabled node too.

## Show a tooltip

A tooltip appears next to the pointer while the node is hovered.
[[Node#hover]] takes a line of text, or a list of lines:

```java file="com/example/interactivity/hover/TooltipUI.java" region="text"
```

<figure>

![The pointer rests on a dark grey "Wallet" button, and a dark tooltip "Coins: 120" shows next to it; two clicks bring the tooltip to "Coins: 140"; then the pointer moves to the "Shop" button, whose tooltip has two lines](/captures/interactivity-tooltip-text.webp)

<figcaption>
The supplier of a tooltip runs on every frame the tooltip shows, so the line is always up to date.
</figcaption>
</figure>

A supplier that returns `null`, or an empty list, shows no tooltip on that frame.
The UI bridge draws text tooltips, with the font of [[WindowUIBridge#hoverInfo]]: set it once when you start JOID ([Style the window UI bridge](/use/learn/running/backends/#style-the-window-ui-bridge)).

A tooltip can also be made of nodes, or of your own drawing.
[[NodeHoverElement]] shows a node, and its factory says where:

| Factory | Where the tooltip sits |
|---|---|
| `follow(node)` | At the pointer, offset by the position of the node |
| `relative(node)` | At the hovered node, offset by the position of the node |
| `fixed(node)` | At the position of the node in the design canvas |

```java file="com/example/interactivity/hover/TooltipUI.java" region="node"
```

<figure>

![The pointer moves along a dark grey "Profile" button, and a blue "Up" label stays right above the button](/captures/interactivity-tooltip-node.webp)

<figcaption>
A relative tooltip stays at the same place over the node, wherever the pointer is on it.
</figcaption>
</figure>

[[CustomHoverElement]] has the same three factories and takes your drawing: the origin of the drawing is the point that the factory names, here the pointer.

```java file="com/example/interactivity/hover/TooltipUI.java" region="custom"
```

<figure>

![The pointer moves along a dark grey "Target" button, and a blue crosshair follows the pointer](/captures/interactivity-tooltip-custom.webp)

<figcaption>
A custom tooltip draws with the [drawing API](/use/learn/going-further/drawing/), centered on the pointer.
</figcaption>
</figure>

The hovered node shows its own tooltips; a node without one shows the tooltips of its nearest parent that has some.
A node tooltip stays inside the window.
[[Node#removeHover]] removes one element, [[Node#clearHover]] every tooltip of the node.

## Change the cursor over a node

Each node of `CursorUI` gives the window its own cursor:

```java file="com/example/interactivity/hover/CursorUI.java" region="cursor"
```

<figure>

![The pointer moves over a dark card: on the Shop button it becomes a hand, on the Name field a text cursor, on the Drag me bar four arrows, and back to an arrow on the card.](/captures/interactivity-cursor.webp)

<figcaption>
The cursor tells the user what a node does before any click: press, type or drag.
</figcaption>
</figure>

[[Node#cursor]] sets the [[Cursor]] of the window over the node.
A node without a cursor shows the cursor of its parent, and the cursor of the node under a press stays until the release.
Text fields show `Cursor.TEXT`.
`POINTER` suits what reacts to a click, `TEXT` a text to type or select and `MOVE` what you drag; the [catalog](/use/catalog/#cursors) gives every cursor and its use.

## Recap

- **`hoveredColor`** blends to a color under the pointer; **`hoverDuration`** and **`hoverEquation`** shape the animation, and **`hoverValue`** applies it to any value.
- A node is **hovered** when it is the frontmost interactive node under the pointer, or one of its parents, in the top UI.
- **`onHoverStart`** and **`onHoverEnd`** run once per entry and exit, **`onHover`** on every hovered frame.
- **`hover(...)`** adds a tooltip: a line, lines, a node (`NodeHoverElement`) or a drawing (`CustomHoverElement`); its suppliers run on every frame it shows.
- **`cursor(...)`** sets the cursor; a node without one shows the cursor of its parent.

## Try it

**Grow the button under the pointer.**
Make the "Shop" button of `HoverUI` grow from `220` to `260` px wide while it is hovered.

<details>
<summary>Solution</summary>

`hoverValue` is read in a lambda, on every frame:

```java fragment
.<RectNode>width(() -> 220D + rect.hoverValue(40F))
```

`rect` is the button, kept in a local variable or given by `self(rect -> ...)`.

</details>

**A tooltip only when there is news.**
Show "New items" over the "Shop" button only while a [[BooleanSignal]] `news` is `true`.

<details>
<summary>Solution</summary>

A line that is `null` shows no tooltip:

```java fragment
.hover(() -> news.peek() ? "New items" : null)
```

</details>
