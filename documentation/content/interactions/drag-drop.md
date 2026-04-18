# Drag & Drop

Make nodes movable with the mouse. Supports constrained movement, snap targets, and drag callbacks.

## Enable dragging

```java
node.draggable(DraggableProperty.free());          // any direction, within parent bounds
node.draggable(DraggableProperty.horizontal());    // X only
node.draggable(DraggableProperty.vertical());      // Y only
node.draggable(DraggableProperty.screen());        // anywhere on screen (ignores parent)
node.draggable(DraggableProperty.zone(x, y, w, h)); // within a specific zone
```

## Drag types

The `DraggableProperty` can be configured for two behaviors:

```java
DraggableProperty.free().type(DraggableType.MOVE);     // default — the node moves
DraggableProperty.free().type(DraggableType.COPY);     // a clone is dragged, original stays
```

`COPY` is useful for tool palettes — drag a copy from the palette into the workspace.

## Snap targets

Register targets where the dragged node should snap:

```java
DraggableProperty.free()
    .snap(otherNode)
    .snap(anotherNode)
    .snap(aThirdOne);
```

When released near a snap target, the node animates to the target's absolute position and `onSnap` fires:

```java
node.onSnap((dragged, context, snapTarget) -> {
    System.out.println("Snapped onto " + snapTarget);
});
```

## Drag callbacks

```java
node.onDrag((n, context) -> {
    // fires while the node is being dragged
});
```

For start/stop:

```java
node.onMousePressed((n, mx, my, ct, ctx) -> {
    if (ct == ClickType.LEFT) startDrag();
});
node.onMouseReleased((n, mx, my, ct, ctx) -> {
    if (ct == ClickType.LEFT) stopDrag();
});
```

Note: `startDragging()` / `stopDragging()` are already wired to mouse events when `draggable()` is set. You rarely call them manually.

## Example — sortable list

```java
final ListSignal<Item> items = new ListSignal<>();

FlexNode.vertical(0, 0, 400).margin(8)
    .watch(items, WatchProperty.BODY)
    .body(list -> {
        for (final Item item : items) {
            RectNode.create(0, 0, 400, 48)
                .color(Color.decode("#1f2937"))
                .effect(RoundedNodeEffect.create(6F))
                .draggable(DraggableProperty.vertical())
                .onDrag((n, ctx) -> {
                    // Re-sort items based on Y position
                    reorderByY();
                })
                .attach(list);
        }
    })
    .attach(parent);
```

## Example — slider built from drag

A custom slider using `draggable` instead of the built-in `SliderNode`:

```java
RectNode.create(0, 0, 300, 4)
    .color(Color.decode("#374151"))
    .body(track -> {
        RectNode.create(100, -6, 16, 16)
            .color(Color.WHITE)
            .effect(CircleNodeEffect.create())
            .draggable(DraggableProperty.zone(0, -6, 284, 16))
            .onDrag((n, ctx) -> {
                float t = (float) (n.getX() / 284D);
                valueSignal.set(t);
            })
            .attach(track);
    })
    .attach(parent);
```

## Global drag state

Access the currently dragged node from anywhere:

```java
Node dragged = ui.getDraggedNode();    // null if nothing is being dragged
```

Useful for drop zones that highlight when a compatible drag is in progress.

## Best practices

- **Constrain movement sensibly.** `DraggableProperty.screen()` lets things wander into unexpected places.
- **Use snap targets for UI puzzles** (e.g., sort-into-bucket mini-games).
- **Don't mutate the tree in `onDrag`.** Update signals instead, let the tree react.

## See also

- [Node Fundamentals](../nodes/node-fundamentals.md) — draggable property.
- [Callbacks](callbacks.md) — mouse events.
