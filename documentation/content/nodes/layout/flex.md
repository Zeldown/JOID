# FlexNode

`FlexNode` (`dev.joid.lib.ui.node.impl.structure.flex`) places its children one after the other in a column or a row, with a fixed gap and an optional alignment across the line. It grows along its main axis to fit its children. Use it for lists, toolbars and menus instead of computing positions by hand.

## A row and a column

```java
FlexNode
.horizontal(960, 490, 100)
.margin(10)
.align(Align.CENTER)
.anchorX(Align.CENTER)
.body(flex -> {
    for (int i = 0; i < 3; i++) {
        RectNode.create(0, 0, 100, 100).color(Color.RED).attach(flex);
    }
})
.attach(this);

FlexNode
.vertical(100, 100, 300)
.margin(8)
.body(flex -> {
    RectNode.create(0, 0, 300, 60).color(Color.GRAY).attach(flex);
    RectNode.create(0, 0, 300, 120).color(Color.LIGHTGRAY).attach(flex);
    RectNode.create(0, 0, 300, 60).color(Color.GRAY).attach(flex);
})
.attach(this);
```

![A column of three gray bars next to a row of three red squares](../../images/flex-row-column.png "The column stacks its children with an 8-unit gap, the row lines them up with a 10-unit gap.")

The row is 320 units wide (three children and two gaps of 10) and stays centered on x = 960 thanks to its anchor. The column stacks its children at y = 0, 68 and 196 and ends up 256 units high.

## Creating a FlexNode with vertical or horizontal

| Factory | Direction | Fixed size | Computed size |
| --- | --- | --- | --- |
| `vertical(double x, double y, double width)` | `COLUMN` | `width` | Height, from the children |
| `horizontal(double x, double y, double height)` | `ROW` | `height` | Width, from the children |

`FlexNode` is `final`: these two factories are the only way to create one.

## How children are placed

- Children are placed in the order of `getChildren()`: attachment order, sorted by [z-index](../node-fundamentals.md#zindex).
- Along the main axis, each child is placed at the end of the previous one plus the margin. The child's own default position is added to its slot: a child created at `y = 5` in a column sits 5 units below its slot.
- A child whose own visibility predicate is false (`isVisibleProperty()`) takes no room: the next child takes its place. It becomes part of the line again as soon as it is visible.
- The main size of the `FlexNode` is the sum of the visible children's sizes plus the margins between them, or 0 without visible children. The cross size keeps the value given to the factory.
- The layout runs when the node loads, on every update tick and on every frame, including while the node is waiting for data. Children appended, removed, resized, shown or hidden are picked up on the next frame.

`FlexNode` does not wrap its children onto several lines: use [GridNode](grid.md) for that.

## Spacing with margin

`margin(double margin)` sets the gap between two consecutive visible children along the main axis. Default: `0`. There is no gap before the first child or after the last one.

## Alignment with align

`align(Align align)` positions each child across the main axis, inside the cross size of the `FlexNode`. `Align` is in `dev.joid.lib.utils.align`.

| `Align` | Column (horizontal position) | Row (vertical position) |
| --- | --- | --- |
| `null` | The child keeps its own `x`. Default. | The child keeps its own `y`. |
| `START` | `x = 0` | `y = 0` |
| `CENTER` | Centered in the width | Centered in the height |
| `END` | Right edge on the right edge | Bottom edge on the bottom edge |

With an alignment, the child's own offset on the cross axis is replaced. `align(null)` restores the default.

```java
FlexNode
.vertical(100, 100, 200)
.margin(8)
.align(Align.CENTER)
.body(flex -> {
    RectNode.create(0, 0, 120, 40).color(Color.GRAY).attach(flex);
    RectNode.create(40, 0, 160, 40).color(Color.LIGHTGRAY).attach(flex);
    RectNode.create(0, 0, 80, 40).color(Color.GRAY).attach(flex);
})
.attach(this);
```

![The same column of three bars with the alignments null, START, CENTER and END](../../images/flex-align.png "With null the second bar keeps its own x = 40; START, CENTER and END replace it. The darker area is the 200-unit width of the FlexNode.")

## Direction

`direction(FlexDirection direction)` switches between `FlexDirection.COLUMN` and `FlexDirection.ROW` (`FlexNode.FlexDirection`). When the direction changes, every child goes back to its default position before the next layout. The new main size is then computed from the children, while the new cross size keeps its current value: set it with `width(...)` or `height(...)` when you rely on `align`.

```java
final FlexNode menu = FlexNode.vertical(100, 100, 300);
menu.direction(FlexDirection.ROW).height(60);
```

## Centering a FlexNode with anchors

A `FlexNode` starts with a main size of 0, so its [anchor](../node-fundamentals.md#anchors-with-anchor-anchorx-and-anchory) controls where it grows: with `anchorX(Align.CENTER)`, a row created at x = 960 stays centered on 960 whatever its width; with `anchorY(Align.END)`, a column grows upwards from its `y`.

## Lists built from a signal

Rebuild the children from data with [watch](../../state/watch.md): `WatchProperty.CLEAR_CHILDREN` (`dev.joid.lib.ui.node.property.watch`) detaches the old children, then `WatchProperty.BODY` runs the builder again, and the layout follows. `ListSignal` is in `dev.joid.lib.utils.signal.impl.iterable`.

```java
final ListSignal<String> items = new ListSignal<>(Arrays.asList("Sword", "Shield"));

FlexNode
.vertical(100, 100, 400)
.margin(8)
.watch(items, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
.body(flex -> {
    for (final String item : items.getOrDefault()) {
        RectNode.create(0, 0, 400, 60).color(Color.GRAY).hover(() -> item).attach(flex);
    }
})
.attach(this);

items.add("Bow");
```

## Nesting and scrolling

- A `FlexNode` can contain other `FlexNode`s: a column of rows builds a simple table.
- A `FlexNode` grows with its content, so it never overflows itself. To scroll a long list, put the `FlexNode` in a fixed-size node with `OverflowProperty.SCROLL` (see [Overflow and Scrolling](overflow-and-scroll.md)) rather than setting the overflow on the `FlexNode`.
- For a list the user can reorder by dragging, use [ReorderableFlexNode](reorderable-flex.md).

## Reference

| Method | Description |
| --- | --- |
| `vertical(double x, double y, double width)` | New column with a fixed width. |
| `horizontal(double x, double y, double height)` | New row with a fixed height. |
| `margin(double margin)` | Gap between visible children. Default `0`. Returns the `FlexNode`. |
| `align(Align align)` | Cross-axis alignment, or `null` to leave the children's cross position alone. Default `null`. Returns the `FlexNode`. |
| `direction(FlexDirection direction)` | `COLUMN` or `ROW`. Resets the children to their default position when it changes. Returns the `FlexNode`. |
| `getMargin()`, `getAlign()`, `getDirection()` | Current settings. |

| `FlexDirection` | Description |
| --- | --- |
| `COLUMN` | Children stacked from top to bottom. |
| `ROW` | Children lined up from left to right. |

`margin`, `align` and `direction` return `FlexNode`, so they can be chained in any order before the `Node` setters. Everything else is inherited from [Node](../node-fundamentals.md).

## See also

- [Node Fundamentals](../node-fundamentals.md)
- [GridNode](grid.md)
- [ReorderableFlexNode](reorderable-flex.md)
- [Overflow and Scrolling](overflow-and-scroll.md)
- [Watching Signals](../../state/watch.md)