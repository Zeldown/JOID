# FlexNode

Automatic linear layout — horizontal or vertical — with a configurable gap between children and optional cross-axis alignment. The node's main-axis size grows to fit its children; the cross-axis size is fixed by the factory you pick.

## Create

```java
FlexNode.horizontal(x, y, height)         // row, height fixed, width grows
    .body(flex -> { /* children */ })
    .attach(parent);

FlexNode.vertical(x, y, width)             // column, width fixed, height grows
    .body(flex -> { /* children */ })
    .attach(parent);
```

Children are positioned sequentially along the main axis with `margin` gaps between them.

## API

```java
FlexNode margin(double value)              // gap between children (main axis)
FlexNode align(Align align)                // START / CENTER / END (cross axis)
FlexNode direction(FlexDirection dir)      // COLUMN / ROW — matches the factory
```

Only visible children (`isVisibleProperty()`) contribute to the layout — hidden children keep their slot but don't take space.

## Example — toolbar

```java
FlexNode.horizontal(40, 40, 48).margin(8).body(toolbar -> {
    RectNode.create(0, 0, 48, 48).color(Color.decode("#1f2937")).attach(toolbar);
    RectNode.create(0, 0, 48, 48).color(Color.decode("#1f2937")).attach(toolbar);
    RectNode.create(0, 0, 48, 48).color(Color.decode("#1f2937")).attach(toolbar);
}).attach(parent);
```

Three 48×48 buttons in a row, 8px between each. No manual X tracking.

## Example — card list

```java
FlexNode.vertical(40, 40, 400).margin(12).body(list -> {
    for (final Item item : items) {
        RectNode.create(0, 0, 400, 80)
            .color(Color.decode("#1f2937"))
            .effect(RoundedNodeEffect.create(8F))
            .body(card -> {
                TextNode.create(16, 16)
                    .text(Text.create(item.name, info))
                    .attach(card);
            })
            .attach(list);
    }
}).attach(parent);
```

## Reactive children

`FlexNode` recomputes positions every frame (`draw`, `update`, `drawSkeleton`) — adding or removing children updates the layout on the next tick.

For reactive data-driven lists:

```java
FlexNode.vertical(0, 0, 400).margin(8)
    .watch(itemSignal, WatchProperty.BODY)
    .body(list -> {
        for (Item item : itemSignal.getOrDefault()) {
            // render each
        }
    })
    .attach(parent);
```

`WatchProperty.BODY` re-runs the `body` consumer on signal change without rebuilding the whole UI.

## Best practices

- **Prefer `FlexNode` over manual X/Y tracking.** Fewer bugs when items are added/removed.
- **Use `margin` not manual offsets.** `FlexNode` handles inter-child gaps cleanly.
- **For dynamic lists, combine with `watch(signal, BODY)`** instead of rebuilding the whole UI.

## See also

- `GridNode` — strict grid layouts with equal sizing.
- `ContainerNode` — non-layout wrapper.
