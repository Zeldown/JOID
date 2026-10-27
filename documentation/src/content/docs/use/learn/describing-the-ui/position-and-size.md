---
title: "Position and size"
description: "Place and size a node from its parent, keep a point of it fixed, choose what is on top, hide or disable it, and copy it."
genre: learn
sidebar:
  order: 5
---

A node has a position and a size in design pixels, measured from its parent.
This page shows how to size a node from its parent, which point of a node its position names, which node is drawn on top, and how to hide, disable or copy a node.

**You will learn**
- How to size and place a node from its parent
- How to keep the center or a side of a node in place
- How to take a node out of the positions of its parent
- How to choose what is drawn on top
- How to hide, disable and copy a node

## Size a node from its parent

The lambda of `body` receives the parent, already sized, so its children can use its size:

```java file="com/example/describing/position/PanelUI.java" region="parent"
```

<figure>

![A light grey panel with a grey header bar across its whole width and a white button centered near its bottom edge](/captures/describing-position-panel.webp)

<figcaption>
The header takes the width of the panel and the button is centered from it: change the panel and both follow at the next build.
</figcaption>
</figure>

A node has shortcuts for these computations: [[Node#w]] and [[Node#h]] return its width and height, `dw(n)` and `dh(n)` divide them by `n`, `mw(n)` and `mh(n)` multiply them, `aw(n)` and `ah(n)` add `n`, and `ax(n)` and `ay(n)` add `n` to its position.

These values are read once, when the child is created.
To follow a parent whose size changes, give the setter a lambda:

```java file="com/example/describing/position/PanelUI.java" region="follow"
```

[Signals](/use/learn/interactivity/signals/) explains when JOID computes such a lambda again.

[[Node#x(double)]], [[Node#y(double)]], [[Node#width(double)]] and [[Node#height(double)]] set each value; [[Node#move]] changes the position the way a drag does, so `beforeMove` listeners can refuse it ([Drag and drop](/use/learn/interactivity/drag-and-drop/)).

[[Node#aspectRatio(double)]] keeps the width and the height in a ratio: the height follows the width, or the width follows the height when the width is 0.

```java file="com/example/describing/position/PanelUI.java" region="ratio"
```

## Keep a point of a node in place

The position of a node names its top-left corner by default.
[[Node#anchorX(Align)]] and [[Node#anchorY(Align)]] make it name the center or the other side instead, and [[Node#anchor(Align)]] sets both:

```java file="com/example/describing/position/AnchorUI.java" region="anchors"
```

<figure>

![A light grey card crossed by a dark vertical line. Three bars share the same x: the first starts on the line, the second is centered on it, the third ends on it.](/captures/describing-position-anchors.webp)

<figcaption>
The three bars have the same x; their anchor decides which of their points sits on it.
</figcaption>
</figure>

When the size of an anchored node changes, the anchored point stays where it is: a node anchored at its center grows on both sides.
[[Align]] `CENTER` on both axes centers a node on its position, the usual way to center a text.
`getX()` and `getY()` return the top-left corner where the node is drawn, after its anchor and its layout; [[Node#getDefaultX]] and [[Node#getDefaultY]] return the values you set.

## Take a node out of its parent's positions

[[Node#position(PositionProperty)]] with [[PositionProperty]] `ABSOLUTE` places a child on the canvas instead of inside its parent:

```java file="com/example/describing/position/PanelUI.java" region="absolute"
```

The white square sits at (20, 20) on the canvas, not at (20, 20) in the panel.
It stays a child: it is drawn, hidden and removed with its parent, but layout nodes skip it and it does not count in the size of their content ([Layout](/use/learn/describing-the-ui/layout/)).

## Choose what is drawn on top

Siblings are drawn in attach order, the last on top, and the mouse reaches the top one first.
[[Node#zindex(int)]] changes that order: a higher value is drawn later, so on top.

```java file="com/example/describing/position/OrderUI.java" region="order"
```

<figure>

![A light grey card with three overlapping squares: the dark grey one on the left is drawn above the grey one in the middle, and a small dark grey square peeks out from behind the top-left corner of the white square on the right.](/captures/describing-position-order.webp)

<figcaption>
`zindex(1)` raises the first square above the second; `zindex(-1)` puts the small square behind its own parent.
</figcaption>
</figure>

The `zindex` orders a node among its siblings only.
A child with a negative `zindex` is drawn before its parent, so the parent covers it.
Siblings with the same `zindex` keep their attach order: attaching a node again brings it to their front ([Nodes and the tree](/use/learn/describing-the-ui/nodes/#bring-a-node-to-the-front)).

[[Node#zlevel(double)]] moves a node along the depth axis; it matters only when the depth test is on, as with 3D models ([3D models](/use/learn/going-further/3d-models/)).
The order of whole UIs is another `zindex`, set on the UI ([Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack)).

## Hide or disable a node

```java file="com/example/describing/position/HideUI.java" region="hide"
```

<figure>

![Two grey squares and a white one in a row; the pointer rests on the second grey square, which keeps its color, then clicks the white square twice: the first grey square disappears, then comes back](/captures/describing-position-hide.webp)

<figcaption>
The hidden square is neither drawn nor clickable; the disabled one is drawn but shows no hover.
</figcaption>
</figure>

- [[Node#visible(boolean)]] `false` hides the node and its children: they are not drawn and receive no input.
- [[Node#enabled(boolean)]] `false` keeps the node and its children drawn, but they receive no input and show no hover.
- `interactive(false)` lets the mouse go through a node to what is below it ([Events](/use/learn/interactivity/events/)).

`isVisible()` and `isEnabled()` take the parents into account: a child of a hidden node is hidden.

## Copy a node

[[Node#copy]] returns another node with the same values, children, effects and listeners, ready to attach:

```java file="com/example/describing/position/CopyUI.java" region="copy"
```

<figure>

![A light grey card with three tiles in a row: two white tiles holding a grey disc, and a dark grey tile holding a grey disc.](/captures/describing-position-copy.webp)

<figcaption>
Each copy starts from the tile, then its own setters move it or change its color.
</figcaption>
</figure>

The copy is a snapshot: it keeps the values that the original has at the moment of the copy, and it does not follow the lambdas and signals of the original.
It has no parent until you attach it.
A node of your own needs a constructor that JOID can call to be copied ([Custom nodes](/use/learn/going-further/custom-nodes/)).

## Recap

- Positions and sizes are **design pixels from the parent**; `body` hands you the sized parent.
- Values read from a parent are read **once**; a **lambda** follows them.
- The **anchor** sets which point of the node its position names, and which point stays when it resizes.
- **`ABSOLUTE`** places a child on the canvas and keeps it out of layouts.
- Siblings draw in **attach order**; **`zindex`** reorders them, and a negative one goes **behind the parent**.
- **`visible(false)`** hides a branch; **`enabled(false)`** keeps it drawn without input.
- **`copy()`** makes a snapshot with the same values, children, effects and listeners.

## Try it

**Center a square.**
Place a 100 × 100 square in the middle of the panel of `PanelUI`, without computing its top-left corner.

<details>
<summary>Solution</summary>

Anchor it at its center and place it at the center of the panel:

```java fragment
RectNode.create(panel.dw(2D), panel.dh(2D), 100, 100).color(Color.WHITE).anchor(Align.CENTER).attach(panel);
```

</details>

**Raise on hover.**
Make each square of `OrderUI` come on top of the others while the mouse is over it.

<details>
<summary>Solution</summary>

A lambda reads the hover state on every frame:

```java fragment
.zindex(() -> node.isHovered() ? 10 : 0)
```

`node` is the square itself, for example through `self(node -> node.zindex(...))`.

</details>
