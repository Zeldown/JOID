---
title: "Nodes and the tree"
description: "Build the node tree: create, configure, nest, move and remove nodes."
genre: learn
sidebar:
  order: 2
---

Everything a UI shows is a node: a rectangle, a text, an image, a list, a slider.
Nodes hold other nodes, and together they form the node tree of the UI.
This page shows how to create nodes, nest them and change the tree.

**You will learn**
- How to create a node, configure it and attach it
- How to nest nodes with `body`
- How positions follow the tree
- How to build, move, raise and remove parts of the tree
- How to chain setters without a compile error, and reach the node inside a chain

## Create a node

Every node type has a `create(...)` factory, setters that return the node, and `attach(...)`:

```java file="com/example/describing/nodes/ToolbarUI.java" region="opener"
```

[[RectNode#create]] creates a rectangle from `x`, `y`, `width` and `height`, `color(...)` sets one of its properties and returns the node, and `attach(this)` adds it to the UI.
You configure a node in one chain, one setter per property.
Once attached, the node is drawn on every frame until you remove it.

Most setters also take a `Supplier`, so that the property follows a value that changes: [Signals](/use/learn/interactivity/signals/) answers the question "When does JOID compute this value again?" for each form.

## Nest nodes with body

[[Node#body(Consumer)]] runs a lambda right away with the node, so you create its children where you create the node.
`attach(card)` attaches a child to its parent:

```java file="com/example/describing/nodes/ProfileUI.java" region="body"
```

<figure>

![A dark card with a blue header bar, and below it a dark grey panel holding a grey disc and two grey lines of different lengths](/captures/describing-nodes-profile.webp)

<figcaption>
The code reads like the result: each body holds what is drawn inside its node.
</figcaption>
</figure>

The lambda parameter is the node itself: use it to attach children and to read its size.
Name it after its role (`card`, `content`), so that each `attach(...)` says where the child goes.

## Read positions in the tree

A child is placed relative to its parent.
Here the first bar of the menu card of [UIs](/use/learn/describing-the-ui/uis/) sits at (50, 130) in the card, and the card at (760, 340) on the canvas:

<figure>

![On the left, a tree: MenuUI holds a RectNode named card at (760, 340), which holds a TextNode and a highlighted RectNode named bar at (50, 130). On the right, a miniature canvas shows the card with the word Text at its top and its bar highlighted under it. The labels read: card at (760, 340) on the canvas; bar at (50, 130) in the card; so bar at (810, 470) on the canvas.](/captures/describing-nodes-tree.webp)

<figcaption>
Positions add up along the tree: move the card and everything in it follows.
</figcaption>
</figure>

- The nodes attached to the UI are the top of the tree; their position is on the canvas.
- Every other node is positioned from the top-left corner of its parent.
- A child is drawn above its parent, and the children are drawn in the order you attach them, the last on top.

[[Node#getAbsoluteX]] and [[Node#getAbsoluteY]] give the position of a node on the canvas.
[Position and size](/use/learn/describing-the-ui/position-and-size/) covers sizing, anchors and the drawing order.

## Build, move and remove parts of the tree

When a method builds a branch, or attaches it later, build the branch first and attach its root last.
[[Node#append]] attaches several children at once:

```java file="com/example/describing/nodes/ToolbarUI.java" region="append"
```

The children of a branch that is not attached yet wait: they join the UI with their root.

A node has one parent.
Attaching it to another node, or to the UI, moves it there with its children.

[[Node#remove]] detaches children from a node, and [[Node#clearChildren]] detaches all of them; [[UI#remove]] detaches nodes from the top of the tree:

```java file="com/example/describing/nodes/ToolbarUI.java" region="remove"
```

A detached node stops drawing, reacting and following its signals.
Attached again, it starts again: [Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/) details what runs at each step.

To rebuild a whole branch when data changes, let JOID run the `body` again for you: [Rebuilding the tree](/use/learn/state/rebuilding/) shows how.

## Bring a node to the front

Attaching a node that is already attached to the same parent moves it to the end of its siblings, so it is drawn on top of them:

```java file="com/example/describing/nodes/ToolbarUI.java" region="front"
```

The blue rectangle is attached first, then the dark grey one covers it; attaching the blue one again puts it on top.
To keep a node above or below its siblings whatever the attach order, give it a `zindex` ([Position and size](/use/learn/describing-the-ui/position-and-size/#choose-what-is-drawn-on-top)).

## Read the tree

[[Node#getParent]] returns the parent, or `null` at the top of the tree, and [[Node#getChildren()]] returns every child, in attach order.
[[Node#getChildren(Class)]] keeps the children of one class, and [[Node#getChild]] returns one of them by index, or `null`:

```java file="com/example/describing/nodes/ToolbarUI.java" region="query"
```

The nodes at the top of the tree are in [[UI#getNodeList]], in drawing order, and [[Node#getUi]] returns the UI of a node.
These lists are read-only views: change the tree with `attach`, `append` and `remove`.

## Chain setters in the right order

Setters return the node, typed by what the compiler expects next.
The setters of a node type, such as `color` of `RectNode`, return that type.
The setters that every node has, such as `visible`, `onClick` or `attach`, return a plain `Node` in the middle of a chain.

So call the setters of the type first, then the shared ones:

```java fragment
RectNode.create(0, 0, 100, 100).visible(true).color(ProfileUI.ACCENT); // ✗ visible returns a Node: no color
RectNode.create(0, 0, 100, 100).color(ProfileUI.ACCENT).visible(true); // ✓ the RectNode setters come first
```

When the order cannot change, give the type to the shared setter: `.<RectNode>visible(true).color(...)`.
At the end of a chain, the variable you assign gives the type: `final RectNode first = RectNode.create(...).color(...).attach(this);` compiles.

In the middle of a chain, the node has no variable yet.
[[Node#self]] runs a lambda right away with the node, so that a setter can read the node itself:

```java file="com/example/describing/nodes/ToolbarUI.java" region="self"
```

Here the height of the bar follows its width.
Unlike `body`, which JOID runs again when it rebuilds the children, `self` runs once.

The same type argument types the node that `body`, `self` and the listeners receive: in the middle of a chain, their lambda receives a plain `Node`.
`.<RectNode>onClick(event -> event.getNode().color(...))` gives the listener a `RectNode`, so that it can call the setters of the type.

## Know where each node is taught

The [Catalog](/use/catalog/) shows every node with a capture.
These pages teach them by role:

- Shapes, such as [[RectNode]] and [[CircleNode]]: [Colors and styling](/use/learn/style-and-motion/styling/).
- Text: [Text](/use/learn/describing-the-ui/text/).
- Images, animations and videos: [Images and media](/use/learn/describing-the-ui/media/).
- Nodes that arrange other nodes: [Layout](/use/learn/describing-the-ui/layout/).
- Controls, such as checkboxes and sliders: [Controls](/use/learn/interactivity/controls/).
- A node that does not exist yet: [Custom nodes](/use/learn/going-further/custom-nodes/).

## Recap

- Every node comes from a **`create(...)` factory** and is configured by **fluent setters**, one per property.
- **`attach(this)`** adds a node to the UI; **`attach(parent)`** adds it to another node.
- **`body(...)`** runs right away and creates the children inside their parent.
- A child is **positioned from its parent** and drawn above it; siblings are drawn in attach order.
- A node has **one parent**: attaching it elsewhere moves it; **`remove(...)`** detaches it.
- Attaching a node again **brings it to the front** of its siblings.
- Call the **type setters before the shared setters** in a chain; **`self(...)`** hands you the node in the middle of a chain.

## Try it

**Add a badge.**
Add a small dark disc in the top-right corner of the header of `ProfileUI`.

<details>
<summary>Solution</summary>

Give the header a body: the disc is positioned from the header, not from the card.

```java fragment
RectNode
.create(40, 40, 520, 80)
.color(ProfileUI.ACCENT)
.body(header -> CircleNode.create(470, 15, 50).color(ProfileUI.ELEMENT).attach(header))
.attach(card);
```

</details>

**Fix the chain.**
This line does not compile.
Make it compile without changing what it does.

```java fragment
CircleNode.create(0, 0, 40).onClick(event -> JOID.close(this)).color(ProfileUI.ACCENT).attach(this);
```

<details>
<summary>Solution</summary>

`onClick` is shared by every node: call `color` first.

```java fragment
CircleNode.create(0, 0, 40).color(ProfileUI.ACCENT).onClick(event -> JOID.close(this)).attach(this);
```

</details>

**Raise on click.**
Make each dark grey button of the toolbar come to the front of its siblings when it is clicked.

<details>
<summary>Solution</summary>

Attaching a node again to its parent moves it to the end of its siblings; the event gives the clicked node.

```java fragment
.onClick(event -> event.getNode().attach(toolbar))
```

</details>
