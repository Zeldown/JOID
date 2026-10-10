# ReorderableFlexNode

`ReorderableFlexNode` is a flex column or row whose children the user reorders by dragging: the other children slide aside, the dropped child glides to its slot, and callbacks report the new order. Use it for playlists, task lists, tab bars and any list sorted by hand.

In the examples, `this.info` is a `TextInfo` built from a loaded font (see [Text and Fonts](../../concepts/text.md)).

```java
private final StringSignal order = StringSignal.of("");

@Override
public void init() {
	ReorderableFlexNode
	.vertical(100, 100, 300)
	.margin(8D)
	.onReorderEnd((flex, child, oldIndex, newIndex) -> this.order.set(oldIndex + " -> " + newIndex))
	.body(flex -> {
		for (int i = 0; i < 5; i++) {
			final int index = i;
			RectNode
			.create(0, 0, 300, 50)
			.color(Color.GRAY)
			.body(item -> {
				TextNode.create(16, 11).text(Text.create("Item " + (index + 1), this.info)).attach(item);
			})
			.attach(flex);
		}
	})
	.attach(this);
}
```

![The cursor drags Item 1 down; the other items slide up](../../images/reorder-drag.gif "The dragged item glides to its new slot on release.")

With `autoDrag(true)` (the default), a left press on a child starts dragging it along the main axis. Dropping Item 1 on the fourth slot reports `0 -> 3`. `margin`, `align` and `direction` work as on a FlexNode (see [Layout](../../concepts/layout.md)).

## Locking children with lock

`lock(Node...)` pins children in place: they cannot be dragged and the others cannot pass them. `unlock(Node...)` frees them.

```java
final ReorderableFlexNode list = ReorderableFlexNode.vertical(100, 100, 300).margin(8D).attach(this);

for (int i = 1; i <= 5; i++) {
	final RectNode item = RectNode.create(0, 0, 300, 50).color(i % 2 == 0 ? Color.GRAY : Color.LIGHTGRAY).attach(list);
	if (i == 1 || i == 3) {
		list.lock(item);
	}
}
```

![Item 5 is dragged to the top; it lands under Locked 1 and the locked rows stay in place](../../images/reorder-lock.gif "Dropped on a locked row, the item takes the nearest free slot.")

## Drag handles with startDrag

`autoDrag(false)` turns off the drag on press; start drags from your code with `startDrag(child)`, for example from a handle. `endDrag()` drops the child as a release would.

```java
final ReorderableFlexNode list = ReorderableFlexNode.vertical(100, 100, 300).autoDrag(false).attach(this);

RectNode
.create(0, 0, 300, 50)
.color(Color.LIGHTGRAY)
.body(item -> {
	RectNode.create(0, 0, 40, 50).color(Color.GRAY).onClick((handle, mouseX, mouseY, button) -> list.startDrag(item)).attach(item);
})
.attach(list);
```

## Auto-scroll in a scrolling parent

Put the list in a fixed-size node with `OverflowProperty.SCROLL`. While dragging near its edge, that node scrolls toward the edge, faster as the mouse goes deeper.

```java
RectNode
.create(100, 100, 340, 260)
.color(Color.WHITE)
.overflow(OverflowProperty.SCROLL)
.body(rect -> {
	ReorderableFlexNode
	.vertical(10, 10, 320)
	.margin(8D)
	.body(flex -> {
		for (int i = 0; i < 10; i++) {
			RectNode.create(0, 0, 320, 50).color(Color.GRAY).hoveredColor(Color.DARKGRAY).attach(flex);
		}
	})
	.attach(rect);
})
.attach(this);
```

![Item 2 is held near the bottom edge; the area scrolls by itself until the release](../../images/reorder-scroll.gif "Near the edge of the scrolling area, the area scrolls while the item is dragged.")

## Events with onReorder

| Callback | Lambda | Fires |
|---|---|---|
| `onReorderStart` | `(flex, child) -> ...` | A drag starts; cancel its `pre(...)` phase to refuse it. |
| `onReorder` | `(flex, child) -> ...` | The dragged child moves to a new slot. |
| `onReorderEnd` | `(flex, child, oldIndex, newIndex) -> ...` | The drop settles in its slot. |

## Reference

| Method | Description |
|---|---|
| `vertical(x, y, width)`, `horizontal(x, y, height)` | Creates a column or a row. |
| `margin(double)`, `align(Align)`, `direction(FlexDirection)` | Layout, as on a FlexNode. |
| `autoDrag(boolean)` | Whether a press starts a drag. Default `true`. |
| `lock(Node...)`, `unlock(Node...)`, `isLocked(Node)` | Pins and frees children. |
| `startDrag(Node)`, `endDrag()` | Drags from code. |
| `isDragging(Node)`, `getReorderedNode()`, `getLogicalOrder()` | Drag state and live order. |

## Good to know

- `overflow(SCROLL)` on the list itself does not scroll it: wrap it in a scrolling node.
- An `onClick` on a child consumes the press and prevents its drag: use `autoDrag(false)` and a handle.
- `getChildIndex(child)` and `getLogicalOrder()` give the live order during a drag.

## See also

- Next: [ChartNode](../data/chart.md)
- [Layout](../../concepts/layout.md)
- [Input](../../concepts/input.md)
- [Signals and State](../../concepts/state.md)