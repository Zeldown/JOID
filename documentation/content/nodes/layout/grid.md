# GridNode

`GridNode` (`dev.joid.lib.ui.node.impl.structure.grid`) places its children from left to right and starts a new row when the next cell would pass its width. Use it for tiles of the same size: inventories, galleries, icon pickers.

## A grid of tiles

```java
GridNode
.create(10, 10, 500, 500)
.margin(5)
.body(grid -> {
    for (int i = 0; i < 50; i++) {
        RectNode.create(0, 0, 50, 50).color(Color.RED).attach(grid);
    }
})
.attach(this);
```

![Fifty red tiles in rows of nine](../../images/grid-tiles.png "The grid starts a new row when the next tile would pass its 500-unit width.")

Nine 50-unit tiles fit in each 500-unit row (9 × 50 + 8 × 5 = 490), so the 50 tiles fill five full rows and part of a sixth.

## Creating a GridNode

`GridNode.create(double x, double y, double width, double height)` creates a grid with a fixed width. The height is the minimum height of the grid (see [Sizing](#sizing-and-overflow)). `GridNode` is `final`.

## How cells are placed

- Children are placed in the order of `getChildren()`: attachment order, sorted by [z-index](../node-fundamentals.md#zindex).
- Each child is placed after the previous one on the current row, plus the horizontal margin. When the child's right edge (its own default `x` + its width + the row offset) would pass the grid's width, it starts a new row instead. The first cell of a row never wraps, even when it is wider than the grid.
- The trailing margin is not counted: a row of three 100-unit cells fits exactly in a 300-unit grid.
- A new row starts below the previous one by the height of its first cell plus the vertical margin. Give the cells the same height: rows do not adapt to the tallest cell.
- Each child's own default position is added to its cell: a child created at (5, 5) sits 5 units right and below its cell.
- Every child takes a cell, including children whose visibility predicate is false.
- The layout runs when the node loads, on every update tick and on every frame, including while the node waits for data. Children appended or removed are picked up on the next frame.

## Margins

| Method | Description |
| --- | --- |
| `horizontalMargin(double margin)` | Gap between two cells of a row. Default `0`. |
| `verticalMargin(double margin)` | Gap between two rows. Default `0`. |
| `margin(double margin)` | Sets both margins. |
| `getHorizontalMargin()`, `getVerticalMargin()` | Current margins. |

The setters return the `GridNode`.

## Sizing and overflow

The width never changes. The height depends on the grid's [overflow](overflow-and-scroll.md):

| Overflow | Height |
| --- | --- |
| `NONE` (default) | The height of the rows (the last row's offset plus the last cell's height), and never less than the height given to `create`. It shrinks back when cells are removed. |
| `HIDDEN` or `SCROLL` | The height you set. The rows below it are clipped. |

The cells are positioned by the layout on every frame, so `SCROLL` on the grid itself does not scroll it. To scroll a grid, keep its overflow at `NONE` so that it grows with its rows, and put it in a fixed-size node that scrolls:

```java
RectNode
.create(100, 100, 500, 300)
.color(Color.DARKGRAY)
.overflow(OverflowProperty.SCROLL)
.body(area -> {
    GridNode
    .create(0, 0, 500, 0)
    .margin(5)
    .body(grid -> {
        for (int i = 0; i < 100; i++) {
            RectNode.create(0, 0, 50, 50).color(Color.RED).attach(grid);
        }
    })
    .attach(area);
})
.attach(this);
```

![The cursor scrolls a grid of red tiles inside a gray area](../../images/grid-scroll.gif "The grid grows with its rows; the fixed-size parent scrolls it.")

## Reference

| Method | Description |
| --- | --- |
| `create(double x, double y, double width, double height)` | New grid. `height` is the minimum height when the overflow is `NONE`. |
| `horizontalMargin(double)`, `verticalMargin(double)`, `margin(double)` | Margins. Return the `GridNode`. |
| `getHorizontalMargin()`, `getVerticalMargin()` | Current margins. |

Everything else is inherited from [Node](../node-fundamentals.md).

## See also

- [Node Fundamentals](../node-fundamentals.md)
- [FlexNode](flex.md)
- [Overflow and Scrolling](overflow-and-scroll.md)