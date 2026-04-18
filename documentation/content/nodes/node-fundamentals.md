# Node Fundamentals

A `Node` is the atomic building block of a UI — position, size, effects, callbacks, children. Everything visible extends `Node`. This page covers the API you use on every node, regardless of its concrete type.

## Creation & attachment

All nodes follow the same factory pattern:

```java
ConcreteNode.create(x, y, width, height)
    .<setters...>
    .body(node -> {
        // children
    })
    .attach(parent);
```

- `create` is a static factory. Some nodes have multiple overloads.
- Setters return the node typed for subclass-safe chaining.
- `body(Consumer<Node>)` lets you configure children inline without breaking the chain.
- `attach(parent)` appends the node to its parent and returns the node.

> TIP: **Chain short.** A fluent chain that spans 40 lines becomes unreadable. Extract complex sub-trees into helper methods returning a configured node.

## Position & size

Nodes use a 1920×1080 logical design space, regardless of actual viewport resolution.

### Direct values

```java
node.x(double);
node.y(double);
node.width(double);
node.height(double);
node.position(double x, double y);
node.size(double w, double h);
```

### Relative helpers

Compute positions relative to the parent:

| Method | Meaning |
|---|---|
| `dw(n)` | Width divided by `n` (e.g., `dw(2)` = half width) |
| `dh(n)` | Height divided by `n` |
| `ax(value)` | Absolute X, considering anchor |
| `ay(value)` | Absolute Y, considering anchor |
| `aw(delta)` | X offset from the right edge (`aw(-100)` = 100 left of right) |
| `ah(delta)` | Y offset from the bottom edge |

Example — center a child in its parent:

```java
parent.body(p -> {
    RectNode.create(p.dw(2) - 50, p.dh(2) - 25, 100, 50)
        .color(Color.RED)
        .attach(p);
});
```

### Aspect ratio

```java
node.aspectRatio(1.77D);  // width = height * 1.77, auto-computed on resize
```

## Z-index & ordering

```java
node.zindex(int);
node.zlevel(double);
```

- `zindex` reorders within the same parent. Higher = drawn later (on top).
- `zlevel` translates the node along Z in GL — useful for tooltips that must always render above siblings.

## Hover

JOID tracks hover state automatically using `isHovered(mouseX, mouseY)`. Two things are exposed:

```java
node.hoverDuration(long);              // ms for the hover fade, default 200
node.hoverEquation(TweenEquation);     // easing for the fade, default LINEAR
node.hovered();                        // boolean: currently hovered
node.hoverValue(float max);            // 0F → max, interpolated during fade
```

Use `hoverValue(1F)` to blend colors or scale on hover:

```java
RectNode.create(0, 0, 100, 50)
    .color(() -> Color.WHITE.to(Color.RED, this.hoverValue(1F)))
    .attach(parent);
```

For static hover states (swap between two colors), the `color(normal, hovered)` setter does it automatically.

## Effects

Effects modify how a node renders. Stack as many as you want:

```java
node.effect(RoundedNodeEffect.create(12F));
node.effect(BorderNodeEffect.create(Color.WHITE, 2F));
node.effect(BlurNodeEffect.create(4F));
```

Effects compose in priority order. Get an effect back:

```java
RoundedNodeEffect<?> rounded = node.getEffect(RoundedNodeEffect.class);
```

See [Effects Overview](../effects/overview.md).

## Overflow

Control how children beyond bounds are handled:

```java
node.overflow(OverflowProperty.HIDDEN);  // clip
node.overflow(OverflowProperty.SCROLL);  // enable scroll (attach a ScrollbarNode)
node.overflow(OverflowProperty.NONE);    // no clip (default)
```

With `SCROLL`, the node's `scrollX` / `scrollY` update based on mouse wheel, and child positions are offset accordingly.

## Draggable

Make a node movable with the mouse. `DraggableProperty` exposes one factory per area type:

```java
node.draggable(DraggableProperty.free());                    // unrestricted
node.draggable(DraggableProperty.parent());                  // stay inside parent
node.draggable(DraggableProperty.node(other));               // stay inside another node
node.draggable(DraggableProperty.ui());                      // stay inside the 1920×1080 UI
node.draggable(DraggableProperty.screen());                  // stay inside the viewport
node.draggable(DraggableProperty.custom(x, y, w, h));        // stay inside a custom box
node.draggable(DraggableProperty.disabled());                // never draggable
```

There is no `horizontal()` / `vertical()` / `zone()` factory — restrict an axis by clamping the value yourself in `onDrag`. See [Drag & Drop](../interactions/drag-drop.md) for snapping, drag copies, and callbacks.

## Callbacks

Attach behaviour via the fluent API. Each setter takes a lambda whose parameters match the callback's `apply(...)` signature:

```java
node.onInit((n) -> { });
node.onRender((n, mouseX, mouseY, partialTicks) -> { });
node.onDraw((n, mouseX, mouseY, partialTicks) -> { });
node.onUpdate((n) -> { });
node.onReload((n) -> { });
node.onAppend((n, child) -> { });
node.onDetach((n) -> { });                                                       // cleanup — release resources here
node.onMount((n) -> { });                                                        // first frame rendered
node.onClick((n, mouseX, mouseY, clickType) -> { });
node.onMousePressed((n, mouseX, mouseY, clickType) -> { });
node.onMouseReleased((n, mouseX, mouseY, clickType) -> { });
node.onMouseDragged((n, mouseX, mouseY, clickType, deltaTime) -> { });
node.onMouseScroll((n, mouseX, mouseY, value) -> { });
node.onKeyPressed((n, character, keyCode) -> { });
node.onScrollUpdate((n, value) -> { });
node.onScrollEnd((n, scrollX, scrollY) -> { });
node.onAnimation((n, animator, value) -> { });
node.onDrag((n) -> { });
node.onSnap((n, snapTarget) -> { });
node.onWatch((n, signal, properties) -> { });                                   // reactive signals
```

See [Callbacks](../interactions/callbacks.md) for full details (PRE/POST phases, `InternalContext`).

## Hover tooltips

Show a tooltip on hover:

```java
node.hover(() -> "Simple text");
node.hover(() -> Arrays.asList("Line 1", "Line 2"));
node.hover(MyTooltipNode.create(...));   // fully custom node
```

See [Hover](../interactions/hover.md).

## Reactive watches

Bind a node to a signal — the node reloads automatically when the signal changes:

```java
node.watch(signal);                                // default: WatchProperty.RELOAD
node.watch(signal, WatchProperty.CLEAR_CHILDREN);
node.watch(signal, () -> condition, properties);   // conditional watch
```

See [Watch](../state/watch.md).

## Tree operations

```java
node.append(childA, childB, childC);
node.clearChildren();
node.getChildren();
node.getChildren(Class<T>);        // typed filter
node.getChild(index, Class<T>);
node.getParent();
node.getUi();                      // root UI
```

`clearChildren()` fires `onDetach()` on each child — honor it for resource cleanup.

## Visibility & enabled

```java
node.visible(boolean);   // not drawn, not interactive
node.enabled(boolean);   // drawn, but no click/drag/scroll
```

Both accept `Supplier<Boolean>` for reactive versions:

```java
node.visible(() -> !someBoolean.getOrDefault());
```

## Best practices

- **Attach last, always.** Chains ending in anything other than `.attach(parent)` leak nodes.
- **Use `body()` to group children.** Clearer than reopening a `.attach(...).getChildren().add(...)` chain.
- **Don't poll — watch.** Reactive bindings are cheap; polling in `update()` isn't.
- **Release in `onDetach()`.** If your node holds a resource (socket, thread, texture), override `detach()` or register `onDetach(callback)` to free it.
- **Don't cross UI boundaries.** Never attach a node from one UI into another — the `ui` reference won't match.

## See also

- [Callbacks](../interactions/callbacks.md) — full catalog.
- [Hover](../interactions/hover.md) — tooltips and interactions.
- [Drag & Drop](../interactions/drag-drop.md).
- [Signals](../state/signals.md) + [Watch](../state/watch.md) — reactive state.
