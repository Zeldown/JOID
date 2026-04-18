# Drag & Drop

Make nodes movable with the mouse. Movement is constrained by an **area type**, snap targets can be registered, and the drag can either move the node itself or a clone.

## Enable dragging

`DraggableProperty` exposes a set of static factories — one per area type. There is no `horizontal` or `vertical` factory; restrict an axis by clamping coordinates in `onDrag` yourself.

```java
node.draggable(DraggableProperty.free());                                  // unrestricted
node.draggable(DraggableProperty.parent());                                // stay inside parent
node.draggable(DraggableProperty.node(otherNode));                         // stay inside another node
node.draggable(DraggableProperty.ui());                                    // stay inside the 1920×1080 UI
node.draggable(DraggableProperty.screen());                                // stay inside the viewport
node.draggable(DraggableProperty.custom(x, y, width, height));             // stay inside a custom box
node.draggable(DraggableProperty.disabled());                              // never draggable
```

Under the hood each factory calls `area(DraggableAreaType, Object)` with one of `FREE`, `CUSTOM`, `PARENT`, `NODE`, `UI`, `SCREEN`.

## Drag types

Configure what gets moved:

```java
DraggableProperty.free().type(DraggableType.MOVE);     // default — the node itself moves
DraggableProperty.free().type(DraggableType.COPY);     // a clone is dragged, original stays
```

`COPY` is useful for tool palettes — drag a copy from the palette into the workspace.

## Enabled predicate

Every factory accepts an optional predicate:

```java
DraggableProperty.free().enabled(n -> !n.isLocked());
```

When the predicate returns `false`, the drag is blocked. `DraggableProperty.disabled()` is the shortcut for `enabled(n -> false)`.

## Snap targets

Snap targets are registered through the `snap(...)` setters:

```java
DraggableProperty.free()
    .snap(otherNode, anotherNode, aThirdOne);                              // append targets
DraggableProperty.free()
    .snap(DraggableSnapType.NEAREST, otherNode, anotherNode);              // reset + set type
```

`DraggableSnapType` is one of:

- `NEAREST` (default) — snap to the target whose centre is closest to the dragged node.
- `OVERLAP` — snap to the first target the dragged node currently overlaps.

When a snap fires, the `onSnap` callback receives the target:

```java
node.onSnap((dragged, context, snapTarget) -> {
    System.out.println("Snapped onto " + snapTarget);
});
```

## Drag callbacks

```java
node.onDrag((n, context) -> {
    // fires each frame while the node is being dragged
});
```

For start/stop hooks, use the existing mouse callbacks:

```java
node.onMousePressed((n, mx, my, ct, ctx) -> { if (ct == ClickType.LEFT) startDrag(); });
node.onMouseReleased((n, mx, my, ct, ctx) -> { if (ct == ClickType.LEFT) stopDrag(); });
```

The drag itself is wired automatically by `Node` when `draggable(...)` is set — you don't call `startDragging()` manually unless you want programmatic control.

## Example — custom-bounded card

```java
RectNode.create(0, 0, 120, 60)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(6F))
    .draggable(DraggableProperty.custom(0, 0, 1920, 200))                  // only within a top strip
    .attach(parent);
```

## Example — palette → workspace (COPY)

```java
final DraggableProperty palette = DraggableProperty.ui().type(DraggableType.COPY);

for (final Icon icon : icons) {
    RectNode.create(0, 0, 48, 48)
        .color(Color.decode("#1f2937"))
        .draggable(palette)
        .attach(paletteRow);
}
```

A clone of each palette icon is dragged; the originals stay put.

## Global drag state

Access the currently dragged node from anywhere:

```java
Node dragged = ui.getDraggedNode();    // null if nothing is being dragged
```

Useful for drop zones that highlight when a compatible drag is in progress.

## Best practices

- **Pick the tightest area type.** `screen()` lets nodes leave the UI layout; use `parent()` or `custom()` when you want containment.
- **Use snap for UI puzzles.** Sort-into-bucket mini-games, docking panels, form field reordering.
- **Don't mutate the tree in `onDrag`.** Update signals and let the tree react — adding/removing nodes mid-drag is unsafe.

## See also

- [Node Fundamentals](../nodes/node-fundamentals.md) — draggable property.
- [Callbacks](callbacks.md) — mouse events.