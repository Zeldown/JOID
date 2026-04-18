# GridNode

Fixed-column grid with automatic row wrapping. Same-size children, evenly spaced, laid out left-to-right then top-to-bottom.

## Create

```java
GridNode.create(x, y, width, height)
    .horizontalMargin(12D)
    .verticalMargin(12D)
    .body(grid -> {
        // children
    })
    .attach(parent);
```

Child width determines column count: `floor(width / (childWidth + horizontalMargin))`. New rows start when the current one overflows.

## API

```java
grid.horizontalMargin(double);    // gap between columns
grid.verticalMargin(double);      // gap between rows
```

## Example — icon grid

```java
GridNode.create(40, 40, 800, 600)
    .horizontalMargin(16D)
    .verticalMargin(16D)
    .body(grid -> {
        for (final Icon icon : icons) {
            RectNode.create(0, 0, 80, 80)
                .color(Color.decode("#1f2937"))
                .effect(RoundedNodeEffect.create(8F))
                .hover(() -> icon.name)
                .body(card -> {
                    try {
                        ResourceNode.create(16, 16, 48, 48)
                            .resource(Resource.of(icon.stream))
                            .attach(card);
                    } catch (IOException e) { e.printStackTrace(); }
                })
                .attach(grid);
        }
    })
    .attach(parent);
```

## Auto-growth

If `GridNode`'s own `overflow` is `NONE` and the children exceed the fixed height, the grid grows automatically to contain them.

## Best practices

- **Use `GridNode` for evenly-sized elements.** For mixed sizes, `FlexNode` with `wrap` is more flexible.
- **Combine with `OverflowProperty.SCROLL`** for long lists that need scrolling:

```java
RectNode.create(40, 40, 800, 400)
    .overflow(OverflowProperty.SCROLL)
    .body(wrapper -> {
        GridNode.create(0, 0, 800, 400).body(grid -> { ... }).attach(wrapper);
    })
    .attach(parent);
```

## See also

- [FlexNode](flex.md) — linear layouts.
- [ScrollbarNode](scrollbar.md) — scrolling overflow.
