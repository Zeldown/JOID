# Nodes and the Node Tree

Everything you see in a UI is a node: a rectangle, a text, an image, a list, a text field. Nodes form a tree: each node can hold children, placed relative to it. This page shows how to create nodes, assemble them, chain their settings, hide them and order them.

## Creating and attaching a node

Every node type has static factories, usually named `create` (some add named ones, such as `FlexNode.vertical`). A new node belongs to nothing until you attach it:

```java
RectNode.create(100, 100, 400, 200).color(Color.LIGHTGRAY).attach(this);
```

![A light gray rectangle on the dark stage](../images/ess-nodes-rect.png "A 400 × 200 RectNode.")

- `RectNode.create(x, y, width, height)` (`dev.joid.lib.ui.node.impl.design.shape`) creates the node.
- `color(...)` sets a property and returns the node, so the calls chain.
- `attach(this)`, inside the `init()` of a UI, adds the node to the UI. It is usually the last call of the chain.

Nodes are retained: once attached, a node stays in memory, keeps its state and is drawn every frame until it is removed. You never redraw anything yourself.

## Building a tree with body

`body(...)` runs a lambda right away with the node, so you create its children inline. `attach(parent)` appends a child to a node:

```java
RectNode
.create(560, 240, 800, 600)
.color(Color.DARKGRAY)
.body(rect -> {
	RectNode.create(20, 20, 760, 80).color(Color.GRAY).attach(rect);
	RectNode.create(20, 120, 760, 460).color(Color.LIGHTGRAY).attach(rect);
})
.attach(this);
```

![A dark gray panel with a gray header and a light gray content area](../images/ess-nodes-tree.png "The panel and its two children (0.5× scale).")

Children are placed relative to their parent: the header is drawn at (580, 260) on the canvas, and moving the panel moves both children.

![The UI holds the panel, which holds the header and the content; the header at (20, 20) in the panel lands at (580, 260) on the canvas](../images/ess-diagram-tree.png "The node tree and the positions relative to the parent.")

You can also build a tree first and attach its root last. `append(...)` adds several children at once:

```java
final RectNode toolbar = RectNode.create(0, 0, 1920, 80).color(Color.DARKGRAY);
final RectNode back = RectNode.create(20, 20, 40, 40).color(Color.WHITE);
final RectNode close = RectNode.create(1860, 20, 40, 40).color(Color.WHITE);
toolbar.append(back, close).attach(this);
```

![A dark gray bar across the top of the canvas with a white square at each end](../images/ess-nodes-toolbar.png "The toolbar and its two children, across the whole 1920-unit width (0.4× scale).")

A node has a single parent: attaching it somewhere else moves it there.

## Chaining settings

Setters are generic: they return the type the compiler expects. In practice this gives one rule: in a chain, call the setters of the node itself first (`color` and `hoveredColor` of `RectNode`, `margin` of `FlexNode`...), then the setters every node shares (`zindex`, `visible`, `onClick`...).

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.GRAY)
.hoveredColor(Color.LIGHTGRAY)
.zindex(10)
.onClick((node, mouseX, mouseY, clickType) -> System.out.println("Clicked"))
.attach(this);
```

After `zindex(...)` the chain is typed `Node`, so `color(...)` does not compile there. The same goes for lambda parameters: in `.body(rect -> ...)`, `rect` is a `Node`. When you need the concrete type, assign the result to a variable, or give the type to the shared setter:

```java
final RectNode button = RectNode.create(100, 100, 300, 80).zindex(10);

RectNode.create(100, 200, 300, 80).<RectNode>zindex(10).color(Color.GRAY).attach(this);
```

`self(node -> ...)` runs a lambda with the node right away, like `body`, and is the place for a setting built from the node itself; [Styling and Effects](styling.md) uses it for effects that follow the hover.

## Showing and hiding with visible

`visible(false)` hides a node. A boolean signal passed as is makes the node follow it: here a button shows and hides a panel. A `BooleanSignal` holds `true` or `false` like the `IntegerSignal` counter of the Quick Start holds a number, and `toggle()` flips it.

```java
private final BooleanSignal open = BooleanSignal.of(false);
```

```java
RectNode
.create(100, 100, 200, 60)
.color(Color.GRAY)
.onClick((node, mouseX, mouseY, clickType) -> this.open.toggle())
.attach(this);

RectNode.create(100, 200, 400, 300).color(Color.LIGHTGRAY).visible(this.open).attach(this);
```

![Each click on a gray button shows or hides a light gray panel](../images/ess-nodes-visible.gif "visible(this.open): the panel follows the signal the button toggles.")

A hidden node is not drawn, nor are its children, and it receives no clicks or hover. `enabled(false)` is the softer version: the node is still drawn, but neither it nor its children react to the mouse. `interactive(false)` goes one step further and lets the mouse through the node to the nodes behind it, for a drawing that must not cover them. Signals are the subject of [Signals and Reactivity](signals.md).

## Drawing order with zindex

Siblings are drawn in the order they were attached: the last one is on top. `zindex(int)` changes that order (default `0`): a higher z-index is drawn later, so on top, and receives the mouse first.

```java
RectNode.create(100, 100, 200, 200).color(Color.WHITE).zindex(1).attach(this);
RectNode.create(150, 150, 200, 200).color(Color.GRAY).attach(this);
```

![A white square drawn above an overlapping gray square](../images/ess-nodes-zindex.png "zindex(1) draws the white square above the gray one attached after it.")

A z-index only orders a node among its siblings; a child is drawn above its parent, unless the child's z-index is negative.

## Removing and finding nodes

| Code | Effect |
| --- | --- |
| `panel.remove(header)` | Detaches `header` from `panel`. |
| `panel.clearChildren()` | Detaches every child of `panel`. |
| `getChildren()` | The children, sorted by z-index. |
| `getChildren(RectNode.class)` | The children that are `RectNode`s. |
| `getChild(1, RectNode.class)` | The second `RectNode` child, or `null`. |
| `getParent()`, `getUi()` | The parent (`null` at the top level) and the UI. |

A detached node stops everything: its drag or hover ends, its subscriptions to signals stop, and it starts again if you attach it back. To change a part of the screen, you rarely remove nodes by hand: setters follow signals, and `watch` rebuilds a list, shown in [Signals and Reactivity](signals.md) and [Layout](../essentials/layout.md#rebuilding-a-list-with-watch).

## The conventions of the fluent API

JOID builds trees with chained calls. Built-in nodes have no public constructor: a static factory creates them (`create(...)`, or named ones such as `FlexNode.vertical(...)`).

| Convention | Meaning |
| --- | --- |
| `create(...)` | Static factory of nodes and effects, with the values the node needs. |
| Setters named after one property | `color(...)`, `hoveredColor(...)`, `x(...)`, `width(...)`: each sets one property and returns the node. |
| `attach(UI)` / `attach(Node)` | Adds the node to a UI or to a parent node; usually the last call of a chain. |
| `append(Node...)` | Adds children to a node, the reverse of `attach`. |
| `body(Consumer)` | Runs the code right away with the node, to create its children inline; the node keeps it, so `watch` can run it again (see [Layout](../essentials/layout.md#rebuilding-a-list-with-watch)). |
| `self(Consumer)` | Runs the code right away with the node, without keeping it: for an effect built from the node. |
| `onXxx(callback)` | Registers a callback: `onClick`, `onHoverStart`... See [Input and Callbacks](input.md). |

Setters are generic (`public final <T extends Node> T x(double x)`): that is what lets a chain keep the type of the node, with the rule of [Chaining settings](#chaining-settings).

## Pitfalls

- A setter of the node itself after a shared setter does not compile: reorder the chain or add a type witness (`.<RectNode>zindex(10)`).
- A `null` literal is ambiguous between the value and `Supplier` overloads of a setter: write `hoveredColor((Color) null)`.
- Nodes built outside `init()` (in a callback) appear only once attached to a node or the UI.

## See also

- Next: [Input and Callbacks](input.md)
- [Node Fundamentals](../nodes/node-fundamentals.md): the complete node API, lifecycle, waiting and skeletons, layers, copies.
- [Component Catalog](../components/overview.md): every node JOID ships.
- [Callbacks](../interactions/callbacks.md): every `on...` method.
- [Custom Nodes](../nodes/custom-nodes.md): writing your own node type.