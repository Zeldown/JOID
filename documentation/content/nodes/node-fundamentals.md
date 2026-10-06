# Node Fundamentals

`Node` (`dev.joid.lib.ui.node`) is the base class of everything you place in a UI: a box with a position, a size, children, callbacks and effects. This page covers the API every node inherits: building the tree, placing and sizing nodes, visibility, drawing order, lifecycle and copies.

## A first node tree

```java
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class ShopUI extends UI {

    @Override
    public void init() {
        RectNode
        .create(560, 240, 800, 600)
        .color(Color.DARKGRAY)
        .body(panel -> {
            RectNode.create(20, 20, panel.aw(-40), 80).color(Color.GRAY).attach(panel);
            RectNode.create(20, 120, panel.dw(2) - 30, panel.ah(-140)).color(Color.LIGHTGRAY).attach(panel);
        })
        .attach(this);
    }

}
```

![A dark gray panel with a gray header bar and a light gray block on its left half](../images/node-first-tree.png "The panel and its two children, placed with aw, dw and ah (0.5× scale).")

- `create(...)` builds a node. Each node type has its own static factories.
- `body(...)` runs the lambda right away with the node, so you can build its children inline.
- `attach(panel)` appends a child to `panel`; `attach(this)` adds the panel at the top level of the UI.
- Children are placed relative to their parent: the header is drawn at (580, 260) on the canvas.

## Building the tree

### Factories

Every concrete node exposes static factories: `RectNode.create(x, y, width, height)`, `FlexNode.vertical(x, y, width)`, `ContainerNode.create(parent)`, and so on. Constructors are `protected` or `private`. A freshly created node has no UI and no parent until you attach it.

### attach and append

| Method | Description |
| --- | --- |
| `attach(Node parent)` | Appends this node to `parent` (same as `parent.append(this)`). Returns this node. |
| `attach(UI ui)` | Adds this node at the top level of `ui` (`ui.add(this)`) and loads it immediately. Returns this node. |
| `append(Node... nodes)` | Appends the nodes, in order, as children of this node. Each child gets this node as parent and is loaded immediately when this node already belongs to a UI. Returns this node. |
| `remove(Node... nodes)` | Removes the nodes from the children of this node: each one is detached (`onDetach()`, see [Lifecycle](#lifecycle)) and loses its parent. Nodes that are not children are ignored. Returns this node. |

Children appended before their tree is attached are loaded together with it, so you can build a whole tree first and attach its root last:

```java
final ContainerNode toolbar = ContainerNode.create(0, 0, 1920, 80);
final RectNode back = RectNode.create(20, 20, 40, 40).color(Color.WHITE);
final RectNode close = RectNode.create(1860, 20, 40, 40).color(Color.RED);
toolbar.append(back, close).attach(this);
```

Each appended child fires the parent's `onAppend` callbacks; cancelling their PRE phase skips that child (see [Callbacks](../interactions/callbacks.md)).

> NOTE: A node has a single parent. Appending a node that already has another parent moves it: it is first removed from that parent with `remove(...)`, which detaches it, then appended and loaded again. A top-level node of a UI leaves the UI's node list the same way. Appending a node again to the same parent loads it again and moves it to the end of its z-index group.

### body

| Method | Description |
| --- | --- |
| `body(Consumer<T> consumer)` | Runs `consumer` immediately with this node and stores it. Returns this node. |
| `body(Runnable runnable)` | Same without the node parameter. |
| `getBodyConsumer()` | The stored consumer (`null` when `body` was never called). |

`WatchProperty.BODY` runs the stored consumer again when a watched signal changes, which rebuilds the children from fresh data (see [Watching Signals](../state/watch.md) and [Signals](../state/signals.md), package `dev.joid.lib.utils.signal`):

```java
final Signal<Integer> count = new Signal<>(3);

FlexNode
.vertical(100, 100, 300)
.margin(10)
.watch(count, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
.body(flex -> {
    for (int i = 0; i < count.getOrDefault(); i++) {
        RectNode.create(0, 0, 300, 50).color(Color.GRAY).attach(flex);
    }
})
.attach(this);
```

### Chaining and generic return types

Most setters are declared as `<T extends Node> T method(...)`: the compiler infers `T` from where the result goes.

- Assigned to a variable or passed as an argument, `T` is the expected type: `final RectNode card = RectNode.create(0, 0, 200, 100).anchor(Align.CENTER);`.
- In the middle of a chain, a `Node` setter returns `Node`, so the setters of the concrete type are no longer visible after it. Call the type-specific setters first (`color` of `RectNode`, `margin` of `FlexNode`), then the `Node` ones, or give the type explicitly: `RectNode.create(0, 0, 200, 100).<RectNode>anchor(Align.CENTER).color(Color.RED)`.
- Lambda parameters follow the same rule: in `RectNode.create(...).body(rect -> ...)`, `rect` is a `Node`. Type the parameter to get the concrete type: `.body((final RectNode rect) -> rect.color(Color.RED))`.

### Removing nodes

| Code | Effect |
| --- | --- |
| `node.clearChildren()` | Detaches every child (runs `onDetach()` on each subtree), empties the children list and clears the parent of each child, as `remove(...)` does. Returns the node. |
| `node.remove(child...)` | Detaches the children (runs `onDetach()` on each subtree), removes them from the list and clears their parent. Returns the node. |
| `node.getChildren().remove(child)` | Removes one child from the list only. Its detach hooks do not run: prefer `remove(...)`. |
| `ui.getNodeList().remove(node)` | Removes a top-level node from its UI (see [The UI Class](../ui/ui-class.md)). |

Layout nodes close the gap left by a removed child on the next frame.

## Position and size

Positions and sizes are in UI units of the 1920×1080 virtual canvas. With the default `PositionProperty.RELATIVE`, `x` and `y` are relative to the parent's position; top-level nodes are relative to the UI origin.

| Method | Description |
| --- | --- |
| `x(double x)`, `y(double y)` | Sets the position on one axis. |
| `width(double width)`, `height(double height)` | Sets the size on one axis. |
| `position(double x, double y)` | Sets both coordinates. |
| `size(double width, double height)` | Sets both dimensions. |
| `bounds(double x, double y, double width, double height)` | Sets position and size. |
| `getX()`, `getY()`, `getWidth()`, `getHeight()` | Current position and size. `w()` and `h()` are short aliases of `getWidth()` and `getHeight()`. |

All setters return the node.

### Default bounds

The values given to the factory are kept as the default bounds: `getDefaultX()`, `getDefaultY()`, `getDefaultWidth()`, `getDefaultHeight()`. Setters never change them. Layout nodes and scroll containers place each child from its default position: a child of a vertical `FlexNode` created at `y = 5` sits 5 units below its slot, and the children of a scrolling node are moved to their default position plus the scroll offset on every frame.

### Absolute coordinates with getAbsoluteX

| Method | Description |
| --- | --- |
| `getAbsoluteX()`, `getAbsoluteY()` | Position on the UI canvas: the node's position plus the absolute position of its parent. For an `ABSOLUTE` node, its own `x`/`y`. Compare these with the mouse coordinates given to callbacks. |
| `getAbsoluteDefaultX()`, `getAbsoluteDefaultY()` | The parent's current absolute position plus this node's default position (the default position itself for an `ABSOLUTE` node). |

### Relative helpers dw, dh, mw, mh, aw, ah, ax, ay

The helpers read the node's current values. Use them on the parent inside `body` to place children.

| Helper | Returns | On a 200×100 node at (10, 20) |
| --- | --- | --- |
| `w()` / `h()` | width / height | `w()` = 200 |
| `dw(v)` / `dh(v)` | width ÷ `v` / height ÷ `v` | `dw(4)` = 50 |
| `mw(v)` / `mh(v)` | width × `v` / height × `v` | `mw(0.25)` = 50 |
| `aw(v)` / `ah(v)` | width + `v` / height + `v` | `aw(-10)` = 190 |
| `ax(v)` / `ay(v)` | x + `v` / y + `v` | `ax(5)` = 15 |

```java
RectNode
.create(100, 100, 400, 300)
.color(Color.DARKGRAY)
.body(card -> {
    RectNode.create(card.dw(2) - 50, card.dh(2) - 25, 100, 50).color(Color.RED).attach(card);
    RectNode.create(card.aw(-110), card.ah(-60), 100, 50).color(Color.GREEN).attach(card);
})
.attach(this);
```

![A dark gray card with a red rectangle in its center and a green one in its bottom-right corner](../images/node-helpers.png "dw(2) and dh(2) center the red child; aw and ah place the green one 10 units from the corner.")

The first child is centered in the card; the second sits 10 units from its bottom-right corner. `ax` and `ay` add to the node's own position, which is expressed in its parent's space: use them to place siblings.

### PositionProperty

`position(PositionProperty position)` sets how `x`/`y` are interpreted. `PositionProperty` is in `dev.joid.lib.ui.node.property.position`.

| Value | Description |
| --- | --- |
| `RELATIVE` | Default. `x`/`y` are relative to the parent. |
| `ABSOLUTE` | `x`/`y` are relative to the UI origin, whatever the parents. The node still belongs to its parent: it follows the parent's visibility, clipping and drawing order, and its own children are placed relative to it. |

```java
ContainerNode
.create(200, 300, 400, 400)
.body(container -> {
    RectNode.create(30, 40, 20, 20).color(Color.RED).position(PositionProperty.ABSOLUTE).attach(container);
})
.attach(this);
```

The red square is drawn at (30, 40) on the canvas. `getPosition()` returns the current value.

### Anchors with anchor, anchorX and anchorY

An anchor decides which point of the node stays in place when its size changes. On every rendered frame where the width or height differs from the previous frame, JOID moves `x`/`y` so that the anchored point keeps its position. `Align` is in `dev.joid.lib.utils.align`.

| Method | Description |
| --- | --- |
| `anchor(Align anchor)` | Sets both anchors. |
| `anchor(Align anchorX, Align anchorY)` | Sets each anchor. |
| `anchorX(Align anchorX)`, `anchorY(Align anchorY)` | Sets one anchor. |
| `getAnchorX()`, `getAnchorY()` | Current anchors. Default: `Align.START`. |

| `Align` | Point that stays in place |
| --- | --- |
| `START` | Left or top edge (default). |
| `CENTER` | Center. |
| `END` | Right or bottom edge. |

An anchor does not move a node that already has its final size. It matters for nodes whose size is computed after creation, such as a `TextNode` created without a size or a `FlexNode` that grows with its children: their anchored point ends up at the `x`/`y` you gave.

```java
FlexNode
.horizontal(960, 490, 100)
.margin(10)
.anchorX(Align.CENTER)
.body(flex -> {
    for (int i = 0; i < 3; i++) {
        RectNode.create(0, 0, 100, 100).color(Color.RED).attach(flex);
    }
})
.attach(this);
```

The row stays centered on x = 960 whatever the number of children.

### Aspect ratio

`aspectRatio(double aspectRatio)` keeps `width / height` equal to the ratio. On every rendered frame, when the width is not 0 the height becomes `width / ratio`; otherwise, when the height is not 0, the width becomes `height × ratio`. A 0×0 node stays empty. The default, `-1`, disables it; `getAspectRatio()` returns the value.

```java
RectNode.create(0, 0, 320, 0).color(Color.BLACK).aspectRatio(16D / 9D).attach(this);
```

> NOTE: When a node's position changes between two frames (scroll, drag, tween, layout or your own code), JOID moves the drawing of the node and its subtree by whole screen pixels so the content does not shimmer; once the node stops moving, it is drawn at its exact position. See [Drawing Overview](../drawing/draw-utils.md) for the pixel grid.

## Visibility and enabled state

| Method | Description |
| --- | --- |
| `visible(Predicate<T> visibility)` | Replaces the visibility predicate. Default: always visible. |
| `visible(Signal<?>... signals)` | Visible while every signal holds a value that is not `false`: `getOrDefault()` is neither `null` nor `Boolean.FALSE` (a default value counts), so a `BooleanSignal` shows and hides the node. Replaces the predicate. |
| `enabled(Predicate<T> enabled)` | Replaces the enabled predicate. Default: always enabled. |
| `isVisible()` | `true` when the parent is visible, the node is not entirely outside its [overflow area](layout/overflow-and-scroll.md), and its own predicate passes. |
| `isVisibleProperty()` | The node's own predicate only. Layout nodes use it to give no room to hidden children. |
| `isEnabled()` | `true` when the parent is enabled and the node's own predicate passes. |
| `getVisible()`, `getEnabled()` | The predicates themselves. |

The predicates are evaluated each time the state is checked (several times per frame), so they can read any state directly:

```java
final BooleanSignal open = new BooleanSignal(false);

RectNode
.create(660, 340, 600, 400)
.color(Color.DARKGRAY)
.visible(panel -> open.getOrDefault())
.attach(this);
```

| | Hidden (`isVisible()` is `false`) | Disabled (`isEnabled()` is `false`) |
| --- | --- | --- |
| Drawn | No, nor its subtree | Yes |
| Hover state, hover callbacks, `onClick`, wheel scrolling, drag start | No | No |
| Tooltips | No | Yes |
| Update ticks (`update`, `onUpdate`) | Yes | Yes |
| Per-frame work: anchors, animators, scroll easing, drag movement, `onMount` | Paused | Yes |
| Children | Hidden too | Disabled too: `enabled` is inherited like `visible` |

## Drawing order and depth

### zindex

`zindex(int zindex)` sets the node's position in its parent's children list (default `0`); `getZindex()` reads it, and `getIndex()` returns the same value as the sorting key.

- Children stay sorted by ascending z-index. Children with the same z-index keep their attachment order.
- Calling `zindex(...)` sorts the node again and moves it to the end of its z-index group: re-applying the current value brings a node in front of its equals.
- A node draws its children with a negative z-index first, then itself (`draw`), then its other children in ascending order, then its [layers](#layers-with-layer).
- Input events and tooltips go the other way: children with a z-index of 0 or more from the highest, then the node, then the children with a negative z-index. What is drawn on top receives the event first.

For top-level nodes, the z-index also places the node relative to the UI's drawing hooks (see [The UI Class](../ui/ui-class.md)):

| Top-level z-index | Drawn |
| --- | --- |
| Below 0 | Before `preDraw` |
| 0 to 99 | Between `preDraw` and `postDraw` |
| 100 and above | After `postDraw` |

### Layers with layer

A `NodeLayer` (`dev.joid.lib.ui.node.layer`) is a functional interface, `draw(double mouseX, double mouseY)`, drawn after the node's children. Use it for overlays drawn above the children, such as a badge or a selection frame.

| Method | Description |
| --- | --- |
| `layer(NodeLayer layer)` | Adds a layer after the existing ones. |
| `layer(int index, NodeLayer layer)` | Inserts a layer at `index` in the list. |
| `clearLayers()` | Removes every layer. |
| `getLayerList()` | The layers, in drawing order. |

Layers draw in the same space as the node's own `draw`, the parent's origin, so offset them by the node's position:

```java
final RectNode card = RectNode.create(100, 100, 300, 200).color(Color.DARKGRAY);
card.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRect(card.getX() + card.aw(-20), card.getY() + 10, 10, 10, Color.RED)).attach(this);
```

![A dark gray card with a small red square near its top-right corner](../images/node-layer.png "The layer draws a 10 × 10 badge above the card and its children.")

Layers are drawn inside the node's clip when its overflow is `HIDDEN` or `SCROLL`.

### zlevel

`zlevel(double zlevel)` translates the node and its subtree by `zlevel` along the depth axis of the render matrix (default `0`); `getZlevel()` reads it. It changes neither the drawing order nor the event order (use `zindex` for that); it only matters for rendering that depends on depth.

## Children

| Method | Description |
| --- | --- |
| `getChildren()` | The live children list, an `IndexedConcurrentList<Node>` sorted by z-index. |
| `getChildren(Class<T> clazz)` | A new `IndexedLinkedList<T>` with the children that are instances of `clazz` (subclasses included), in drawing order. |
| `getChild(int index, Class<T> clazz)` | The `index`-th child that is an instance of `clazz`, or `null`. |
| `getParent()` | The parent node, or `null` for a top-level node. |
| `getUi()` | The UI the node belongs to, typed by the expected type (`final ShopUI ui = node.getUi();`). While a UI's `init()` runs, a node without UI returns that UI without keeping it: `hasUi()` stays `false` until the node is loaded. Otherwise `null` until the node is loaded. |
| `hasUi()` | `true` once the node has a UI. |

The children list (`dev.joid.lib.utils.list`, see [Utilities](../reference/utilities.md)) offers:

| Method | Description |
| --- | --- |
| `ordered()` | The children in drawing order, as a `List`. |
| `reversed()` | The children in event order. |
| `get(int)`, `getFirst()`, `getLast()` | Access by position (`getFirst()`/`getLast()` return `null` when empty). |
| `size()`, `isEmpty()`, `contains(Node)` | Queries. |
| `recursive()` | A new list with every descendant, each node followed by its own subtree. |
| `remove(Node)` | Removes a child without detaching it. |
| `add(Node)` | Inserts at the z-index position without setting the parent or loading the child: use `append` instead. |
| `copy()` | A snapshot of the list. |

The list is copy-on-write, so callbacks can append or remove children while the tree is being drawn.

Reading the rectangles of a `panel` node:

```java
final RectNode second = panel.getChild(1, RectNode.class);
for (final RectNode tile : panel.getChildren(RectNode.class)) {
    tile.color(Color.GRAY);
}
```

## Lifecycle

| Stage | Trigger | What runs |
| --- | --- | --- |
| Creation | The factory | The constructor. `body` consumers run as soon as you call `body`. |
| Load | `attach` to a UI, `append` to a node that has a UI, the UI opening, a reload | `load(UI)`, wrapped by the `onInit` callbacks: the children are loaded first, then the scrollbar and skeleton, then the effects' `init`, then the node's `init(UI)` hook. |
| Frame | Every frame, while visible | `render(mouseX, mouseY)`: anchors and aspect ratio, hover, animators, scroll, drag, mount check, then drawing (wrapped by `onRender`, with `draw` wrapped by `onDraw`). |
| Update | Each update tick of the UI bridge (once per frame, before drawing, in the bundled demo windows) | `onUpdate()`: the children first, then the node's `update()` hook, wrapped by the `onUpdate` callbacks. Runs for hidden nodes too. |
| Mount | The first rendered frame in which `isMounted()` is `true` | The `onMount` callbacks. Without [wait conditions](#waiting-and-skeletons), this is the node's first rendered frame. |
| Reload | `reload()`, `WatchProperty.RELOAD` | The children reload first, then the node is loaded again (`init` and `onInit` run again), all wrapped by `onReload`. `body` consumers are not run again. |
| Detach | `clearChildren()` or `remove(...)` on the parent, an `append` that moves the node to another parent, `WatchProperty.CLEAR_CHILDREN`, the UI closing or reloading | `onDetach()`: the children first, then the node's `detach()` hook, wrapped by the `onDetach` callbacks. When the node leaves its parent, it also forgets the overflow area of its former container (see `getOverflowArea()`). |

- Methods named `onX(callback)` register a callback; the overloads without callback (`onUpdate()`, `onDetach()`, `onMousePressed(mouseX, mouseY, clickType, context)`...) are the entry points that run the stage. You call `reload()` and `onDetach()` yourself when needed; the others are called by the framework.
- `init` runs on every load, including reloads: keep it repeatable. Override the hooks in your own nodes (see [Custom Nodes](custom-nodes.md)).
- `UI.reload()` and the dev reload shortcut rebuild the whole tree: the old nodes are detached and the UI's `init()` runs again.
- `getUpdateCount()` counts the loads of the node, `getLastUpdate()` is the clock time (ms) of the last load, and `getRenderTime()` is the time (ns) of the last `render`, subtree included.

### Waiting and skeletons

A node can wait for its data before it is considered mounted. While it is not mounted, a placeholder is drawn instead of its content.

| Method | Description |
| --- | --- |
| `wait(ISignal<?> signal)` | Not mounted until the signal holds a value (`isPresent()`: a default value does not count). |
| `wait(long time, TimeUnit unit)` | Not mounted until `time` has elapsed since this call (measured with the clock bridge). |
| `wait(Predicate<T> predicate)` | Not mounted until the predicate passes. Evaluated on every check. |
| `skeleton(Function<T, Node> skeleton)` | Calls `skeleton` once, right away, and draws the returned node instead of this node, its children and its layers while this node is not mounted. The skeleton's parent is this node, so its coordinates are relative to this node. Returning `null` sets no skeleton. |
| `isMounted()` | `true` when every wait condition passes and the parent is mounted. |
| `getSkeleton()` | The skeleton node, or `null`. |
| `getWaitingList()` | The wait conditions. |

- Conditions accumulate: the node mounts once all of them pass. The children of a node that is not mounted are not mounted either.
- With a skeleton, the skeleton also receives the input events while the node is not mounted.
- Without a skeleton, `drawSkeleton` replaces `draw` on the node and on each descendant. The default `drawSkeleton` fills the node's bounds with the animated `Color.LOADING()` color; `ContainerNode` draws nothing and layout nodes only lay out their children.
- `onMount` runs on the first frame drawn once mounted: fill the node with the loaded data there.

```java
final Signal<String> title = new Signal<>();

RectNode
.create(100, 100, 400, 80)
.color(Color.DARKGRAY)
.wait(title)
.skeleton(card -> RectNode.create(0, 0, 400, 80).color(Color.LOADING))
.onMount(card -> System.out.println("Loaded " + title.getOrDefault()))
.attach(this);
```

![A pulsing placeholder that turns into a dark gray card](../images/node-skeleton.gif "The skeleton pulses until title holds a value (set here after two seconds), then the card draws itself.")

The card shows an animated placeholder until `title.set(...)` is called, then draws itself and runs `onMount`.

## Copying nodes with copy

`copy()` returns a new node of the same class, built by reflection through a constructor `(double, double, double, double)`, `(double, double)` or `()`, whatever its visibility; a `ScrollbarNode` is first tried with `(double, double, double, double, BoundingBox)`. A node without one of these constructors throws a `RuntimeException` (`Failed to copy node: <class>`).

| State | Members |
| --- | --- |
| Copied | Current position and size, visibility and enabled predicates, position property, overflow, anchors, draggable property, z-index, z-level, aspect ratio, hover duration and equation, scroll speed, wait conditions, effects, layers, tooltips, callbacks, mounted state, the children (copied recursively), the scrollbar (copied and linked to the copy), and every non-static, non-final, non-transient field declared by subclasses (by reference, so a `RectNode` copy shares the original's color supplier). |
| Shared | The UI, parent and skeleton references, and the animators (the same `TweenAnimator` instances, reported by `onAnimate` on both nodes). The copy is not appended to the parent. |
| Not copied | Body consumer and scroll state. |

Callbacks added to the copy afterwards do not reach the original, and the other way around. `DraggableProperty` copy drags rely on `copy()` (see [Drag and Drop](../interactions/drag-drop.md)).

## Features covered on other pages

| Feature | Node methods | Page |
| --- | --- | --- |
| Callbacks | `onInit`, `onReload`, `onDetach`, `onAppend`, `onMount`, `onUpdate`, `onRender`, `onDraw`, `onClick`, `onMousePressed`, `onMouseReleased`, `onMouseDragged`, `onMouseScroll`, `onKeyPressed`, `onHover`, `onHoverStart`, `onHoverEnd`, `onDrag`, `onDragStart`, `onDragEnd`, `onSnap`, `onWatch`, `onAnimate`, `onScrollUpdate`, `onScrollEnding`, `onScrollEnd` | [Callbacks](../interactions/callbacks.md) |
| Hover and tooltips | `hover(...)`, `hoverLines(...)`, `hoverElements(...)`, `clearHover()`, `clearHoverLines()`, `clearHoverElements()`, `hoverDuration(long)`, `hoverEquation(TweenEquation)`, `hoverValue(float)`, `hovered(boolean)`, `isHovered()`, `isHovered(double, double)`, `isHovered(double, double, boolean)`, `renderHover(double, double)`, `getHoverDuration()`, `getHoverEquation()` | [Hover and Tooltips](../interactions/hover.md) |
| Effects | `effect(NodeEffect)`, `effect(Function)`, `removeEffect(Class)`, `clearEffects()`, `getEffect(Class)`, `hasEffect(Class)`, `getEffectMap()`, `shouldApplyEffect(NodeEffect)` | [Effects](../styling/effects.md) |
| Drag and drop | `draggable(DraggableProperty)`, `startDragging(double, double)`, `stopDragging()`, `dragging(boolean, double, double)`, `isDragging()`, `isDragged()`, `getDraggable()`, `getDraggedNode()` | [Drag and Drop](../interactions/drag-drop.md) |
| Signals | `watch(Signal)`, `watch(Signal, WatchProperty...)`, `watch(Signal, Supplier<Boolean>, WatchProperty...)` | [Watching Signals](../state/watch.md) |
| Stores | `useStore(Class<T>)`, the store of the node's UI | [Stores](../state/stores.md) |
| Animation | `animate(TweenAnimator)`: the node updates the animator on every rendered frame and fires `onAnimate` when its value changes | [TweenAnimator](../animation/tween-animator.md) |
| Overflow and scrolling | `overflow(OverflowProperty)`, `scrollX`, `scrollY`, `scrollOffsetX`, `scrollOffsetY`, `scrollRatioX`, `scrollRatioY`, `updateScroll`, `scrollSpeed`, `scrollbar`, `hasOverflowX`, `hasOverflowY` | [Overflow and Scrolling](layout/overflow-and-scroll.md) |
| Writing nodes | Hooks, input dispatch entry points, `registerCallback`, `executeCallback`, `executePreCallback`, `executePostCallback`, `fireDrag`, `fireDragStart`, `fireDragEnd`, `hasCallback`, `getCallbackList`, `getCallbackMap` | [Custom Nodes](custom-nodes.md) |

## Node API reference

### Tree

| Method | Description |
| --- | --- |
| `append(Node... nodes)` | Appends children, moving them from their previous parent. |
| `remove(Node... nodes)` | Detaches and removes children. |
| `attach(Node parent)`, `attach(UI ui)` | Attaches this node to a parent or to a UI. |
| `body(Consumer<T>)`, `body(Runnable)` | Runs and stores a builder. |
| `clearChildren()` | Detaches and removes every child, which loses its parent. |
| `getChildren()`, `getChildren(Class<T>)`, `getChild(int, Class<T>)` | Children access. |
| `getParent()`, `getUi()`, `hasUi()` | Tree context. |
| `parent(Node parent)` | Sets the parent reference only; the node is not added to the parent's children. Used by `append`, skeletons and scrollbars. |
| `ui(UI ui)` | Sets the UI reference without loading the node. |

### Geometry

| Method | Description |
| --- | --- |
| `x`, `y`, `width`, `height`, `position(double, double)`, `size`, `bounds` | Setters. |
| `getX()`, `getY()`, `getWidth()`, `getHeight()`, `w()`, `h()` | Current values. |
| `getDefaultX()`, `getDefaultY()`, `getDefaultWidth()`, `getDefaultHeight()` | Factory values. |
| `getAbsoluteX()`, `getAbsoluteY()`, `getAbsoluteDefaultX()`, `getAbsoluteDefaultY()` | Canvas coordinates. |
| `dw`, `dh`, `mw`, `mh`, `aw`, `ah`, `ax`, `ay` | Relative helpers. |
| `position(PositionProperty)`, `getPosition()` | Relative or absolute placement. Default `RELATIVE`. |
| `anchor(Align)`, `anchor(Align, Align)`, `anchorX`, `anchorY`, `getAnchorX()`, `getAnchorY()` | Anchors. Default `START`. |
| `aspectRatio(double)`, `getAspectRatio()` | Width / height ratio. Default `-1` (off). |

### State and order

| Method | Description |
| --- | --- |
| `visible(Predicate<T>)`, `visible(Signal<?>...)`, `enabled(Predicate<T>)` | Predicates. |
| `isVisible()`, `isVisibleProperty()`, `isEnabled()`, `getVisible()`, `getEnabled()` | State. |
| `zindex(int)`, `getZindex()`, `getIndex()` | Order among siblings. Default `0`. |
| `zlevel(double)`, `getZlevel()` | Depth translation. Default `0`. |
| `layer(NodeLayer)`, `layer(int, NodeLayer)`, `clearLayers()`, `getLayerList()` | Layers. |

### Lifecycle

| Method | Description |
| --- | --- |
| `load(UI ui)` | Loads the node and its subtree in `ui`. Called by `attach`/`append`. |
| `reload()` | Loads the subtree again. |
| `onDetach()` | Detaches the subtree (runs the detach hooks). |
| `onUpdate()` | Runs one update tick on the subtree. Called by the UI. |
| `render(double mouseX, double mouseY)` | Draws the node for one frame. Called by the UI or by the parent. |
| `wait(...)`, `skeleton(...)`, `isMounted()`, `getSkeleton()`, `getWaitingList()` | Loading state. |
| `copy()` | Copies the node. |
| `getUpdateCount()`, `getLastUpdate()`, `getRenderTime()` | Load count, last load time (ms), last render duration (ns). |

### Debugging

| Method | Description |
| --- | --- |
| `getHierarchy()` | Class names from the root to this node, e.g. `ContainerNode - RectNode - TextNode`. |
| `getMappedIndex()` | Path of indexes from the UI's node list, e.g. `0.1` for the second child of the first top-level node. `N/A` without UI. |
| `toJson()` | A `JsonObject` with `index`, `mappedIndex`, `name`, `class`, `defaultX`, `defaultY` and `bounding` (`x` and `y` as `relative / absolute`, `width`, `height`). In dev mode it adds `scrollX`/`scrollY` (`target / max [OVERFLOW]`), `visible`, `enabled`, `hovered`, `isChild`, `children` and `hierarchy`. |
| `toString()` | The JSON of `toJson()`, pretty-printed in dev mode. |

### Low-level state getters

These getters expose the node's internal bookkeeping. They are read-only views for debugging tools and custom nodes.

| Method | Description |
| --- | --- |
| `getLastClickType()`, `getLastClickTime()` | Last mouse press dispatched to the node (wherever the pointer was) and its clock time (ms). |
| `getLastKey()`, `getLastCharacter()`, `getLastKeyTime()` | Last key event dispatched to the node and its clock time (ms). |
| `getLastWidth()`, `getLastHeight()` | Size seen on the previous frame (anchor bookkeeping). |
| `isMoving()`, `getRestX()`, `getRestY()`, `getDrawnX()`, `getDrawnY()` | Pixel-alignment bookkeeping of a moving node. |
| `getOverflowArea()`, `overflowArea(Node)` | The ancestor whose overflow clips this node, set while drawing. `remove(...)`, `clearChildren()` and an `append` that moves the node set it back to `null`, on the node and on the descendants that inherited the same area, so the former container no longer clips or hides them. |
| `getAnimatorMap()`, `getHoverAnimator()`, `getHoverElementList()`, `getHoverSupplierList()` | Registered animators and hover state. |
| `getDragX()`, `getDragY()`, `getStartDragX()`, `getStartDragY()`, `getTargetDragX()`, `getTargetDragY()` | Drag bookkeeping. |

## See also

- [Callbacks](../interactions/callbacks.md)
- [ContainerNode](layout/container.md)
- [FlexNode](layout/flex.md)
- [Overflow and Scrolling](layout/overflow-and-scroll.md)
- [Custom Nodes](custom-nodes.md)
- [Core Concepts](../getting-started/core-concepts.md)