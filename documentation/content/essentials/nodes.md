# Nodes

Everything you see in a UI is a node: a rectangle, a text, an image, a list, a text field. Nodes form a tree: each node can hold children, and a child is placed relative to its parent. This page shows how to create nodes, assemble them into a tree, chain their settings, hide them and order them.

## Creating and attaching a node

Every node type has static factories named `create` (some add named ones, such as `FlexNode.vertical`). A new node belongs to nothing until you attach it:

```java
RectNode.create(100, 100, 400, 200).color(Color.RED).attach(this);
```

![A red rectangle](../images/rect-basic.png "A 400 × 200 red RectNode.")

- `RectNode.create(x, y, width, height)` (`dev.joid.lib.ui.node.impl.design.shape`) creates the node.
- `color(...)` sets a property and returns the node, so calls chain.
- `attach(this)`, inside a UI's `init()`, adds the node to the UI. It is usually the last call of the chain.

Nodes are retained: once attached, a node stays in memory, keeps its state and is drawn every frame until it is removed. You do not redraw anything yourself.

## Building a tree with body

`body(...)` runs a lambda right away with the node, so you can create its children inline. `attach(parent)` appends a child to a node:

```java
RectNode
.create(560, 240, 800, 600)
.color(Color.DARKGRAY)
.body(panel -> {
    RectNode.create(20, 20, 760, 80).color(Color.GRAY).attach(panel);
    RectNode.create(20, 120, 760, 460).color(Color.LIGHTGRAY).attach(panel);
})
.attach(this);
```

![A dark gray panel with a gray header and a light gray content area](../images/ess-nodes-tree.png "The panel and its two children (0.5× scale).")

```
SettingsUI
└── RectNode  (panel, at 560, 240)
    ├── RectNode  (header, at 20, 20 in the panel)
    └── RectNode  (content, at 20, 120 in the panel)
```

Children are placed relative to their parent: the header is drawn at (580, 260) on the canvas, and moving the panel moves both children.

You can also build a tree first and attach its root last. `append(...)` adds several children at once:

```java
final RectNode toolbar = RectNode.create(0, 0, 1920, 80).color(Color.BLACK);
final RectNode back = RectNode.create(20, 20, 40, 40).color(Color.WHITE);
final RectNode close = RectNode.create(1860, 20, 40, 40).color(Color.RED);
toolbar.append(back, close).attach(this);
```

![A black bar across the top of the canvas with a white square on the left and a red square on the right](../images/ess-nodes-toolbar.png "The toolbar with its two children, across the whole 1920-unit width (0.4× scale).")

> NOTE: A node has a single parent. Attaching a node to another parent moves it there: it leaves its previous parent (or the top level of its UI) first.

## Chaining settings

Setters are generic: they return the type the compiler expects. In practice, this gives one rule: in a chain, call the setters of the specific node first (`color` of `RectNode`, `margin` of `FlexNode`...), then the setters shared by every node (`anchor`, `visible`, `zindex`, `onClick`...).

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.DARKGRAY)
.zindex(10)
.onClick((node, mouseX, mouseY, clickType) -> System.out.println("Clicked"))
.attach(this);
```

After `zindex(...)`, the chain is typed `Node`, so `color(...)` would no longer compile there. The same goes for lambda parameters: in `.body(panel -> ...)`, `panel` is a `Node`. When you need the concrete type, assign the result to a variable or give the type explicitly:

```java
final RectNode button = RectNode.create(100, 100, 300, 80).color(Color.DARKGRAY).zindex(10);

RectNode.create(100, 200, 300, 80).<RectNode>body(rect -> rect.color(Color.RED)).attach(this);
```

## Showing and hiding nodes

`visible(...)` takes a predicate, checked every frame, so it can read any state directly:

```java
final BooleanSignal open = new BooleanSignal(false);

RectNode
.create(660, 340, 600, 400)
.color(Color.DARKGRAY)
.visible(panel -> open.getOrDefault())
.attach(this);
```

`BooleanSignal` is a value that you can change from anywhere (`open.toggle()`); signals are the subject of [State and Reactivity](state.md).

A hidden node is not drawn, nor are its children, and it receives no clicks or hover. `enabled(...)` is the softer version: a disabled node is still drawn but neither it nor its children react to the mouse.

```java
RectNode.create(100, 100, 300, 80).color(Color.GRAY).enabled(node -> !open.getOrDefault()).attach(this);
```

## Drawing order with zindex

Siblings are drawn in the order they were attached: the last one is on top. `zindex(int)` changes that order (default `0`): a higher z-index is drawn later, so on top, and receives the mouse first.

```java
RectNode.create(100, 100, 200, 200).color(Color.RED).zindex(1).attach(this);
RectNode.create(150, 150, 200, 200).color(Color.BLUE).attach(this);
```

![A red square overlapping a blue square, drawn above it](../images/ess-nodes-zindex.png "zindex(1) draws the red square above the blue one attached after it.")

The red square is drawn above the blue one although it was attached first. A z-index only orders a node among its siblings; a child is always drawn above its parent, unless the child's z-index is negative.

## Removing nodes

| Code | Effect |
| --- | --- |
| `node.clearChildren()` | Detaches and removes every child of `node`. |
| `ui.getNodeList().remove(node)` | Removes a top-level node from its UI. |

Rather than removing nodes by hand, the usual way to change a part of the screen is to rebuild it from a signal with `watch`, shown in [State and Reactivity](state.md).

## Finding children

| Method | Returns |
| --- | --- |
| `getChildren()` | The children, sorted by z-index. |
| `getChildren(RectNode.class)` | The children that are `RectNode`s. |
| `getChild(1, RectNode.class)` | The second `RectNode` child, as an `Optional`. |
| `getParent()`, `getUi()` | The parent node (`null` at the top level) and the UI. |

## Going further

- [Node Fundamentals](../nodes/node-fundamentals.md): the complete node API, lifecycle, waiting and skeletons, layers, copies.
- [Component Catalog](../components/overview.md): every node JOID ships.
- [Callbacks](../interactions/callbacks.md): every `on...` method.
- [Custom Nodes](../nodes/custom-nodes.md): writing your own node type.

Next: [Layout](layout.md).