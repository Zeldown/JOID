# FlexNode

Automatic linear layout — horizontal or vertical — with margins, growth, and overflow wrapping.

## Create

```java
FlexNode.horizontal(x, y, height)         // row, auto-width
    .body(flex -> { /* children */ })
    .attach(parent);

FlexNode.vertical(x, y, width)             // column, auto-height
    .body(flex -> { /* children */ })
    .attach(parent);
```

Children are positioned sequentially along the main axis with `margin` gaps between them.

## API

```java
flex.margin(double);                 // gap between children
flex.padding(double);                // padding inside the flex node
flex.align(Align);                   // START / CENTER / END (cross-axis)
flex.wrap(boolean);                  // overflow to next line/column
```

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

## Wrapping

With `wrap(true)`, children that overflow the flex length wrap to the next row/column:

```java
FlexNode.horizontal(0, 0, 800).margin(8).wrap(true).body(grid -> {
    for (int i = 0; i < 20; i++) {
        RectNode.create(0, 0, 80, 80).color(Color.decode("#3b82f6")).attach(grid);
    }
}).attach(parent);
```

Prefer [GridNode](grid.md) for strict column/row layouts with equal sizing.

## Reactive children

`FlexNode` recomputes positions on layout changes — adding or removing children via `append()` / `clearChildren()` triggers a re-layout automatically.

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

- [GridNode](grid.md) — strict grid layouts.
- [ContainerNode](container.md) — non-layout wrapper.
