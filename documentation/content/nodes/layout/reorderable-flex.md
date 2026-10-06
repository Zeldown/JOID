# ReorderableFlexNode

`ReorderableFlexNode` (`dev.joid.lib.ui.node.impl.structure.reorderable`) is a [FlexNode](flex.md)-style column or row whose children the user can reorder by dragging them. The other children slide aside to make room, the dropped child glides to its slot, and callbacks report the new order. Use it for playlists, task lists, tab bars and any list sorted by hand.

## A reorderable list

```java
final Color[] colors = {Color.decode("#A78BFA"), Color.decode("#4ADE80"), Color.decode("#22D3EE"), Color.decode("#FBBF24")};

RectNode
.create(280, 100, 400, 360)
.color(Color.DARKGRAY)
.overflow(OverflowProperty.SCROLL)
.body(area -> {
    ReorderableFlexNode
    .vertical(0, 0, 400)
    .margin(10)
    .onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println(oldIndex + " -> " + newIndex))
    .body(flex -> {
        for (int i = 0; i < 12; i++) {
            RectNode.create(0, 0, 400, 60).color(colors[i % colors.length]).attach(flex);
        }
    })
    .attach(area);
})
.attach(this);
```

![The cursor drags a violet row down the list, then drags another row to the bottom edge and the list scrolls by itself](../../images/reorder-drag.gif "The other rows slide aside, the dropped row glides to its slot, and the area auto-scrolls near its edge.")

Pressing the left button over an item starts dragging it right away. When the pointer gets close to the top or bottom edge of the scrolling area, the area scrolls by itself.

## Creating a ReorderableFlexNode

| Factory | Direction | Fixed size | Computed size |
| --- | --- | --- | --- |
| `vertical(double x, double y, double width)` | `COLUMN` | `width` | Height, from the children |
| `horizontal(double x, double y, double height)` | `ROW` | `height` | Width, from the children |

`ReorderableFlexNode` is `final` and its constructor is private: create it with these factories.

The layout follows the same rules as [FlexNode](flex.md#how-children-are-placed): children in the order of `getChildren()`, each child's own default offset added to its slot, `margin` between visible children, hidden children (own visibility predicate false) taking no room, main size computed from the children.

| Method | Description |
| --- | --- |
| `margin(double margin)` | Gap between visible children. Default `0`. |
| `align(Align align)` | Cross-axis alignment (`START`, `CENTER`, `END`), or `null` to leave the children's cross position alone. Default `null`. |
| `direction(FlexDirection direction)` | `FlexDirection.COLUMN` or `ROW`. Resets the children to their default position when it changes. |
| `auto(boolean auto)` | Whether a press on a child starts a drag. Default `true`. |

These setters and the `onReorder*` methods return `ReorderableFlexNode`, so they can be chained in any order before the `Node` setters.

## Dragging with the mouse

With `auto(true)`, a left press over a child starts dragging that child, unless:

- the press was already consumed, for example by an `onClick` on the child or on one of its descendants;
- the `ReorderableFlexNode` is disabled;
- another child is already being dragged.

The press that starts a drag is consumed, so the parents' `onClick` callbacks do not run. A press whose drag is refused by `onReorderStart` is not consumed: it goes on to the parents and the nodes behind.

While dragging:

- the child follows the pointer along the main axis, keeping the point where it was grabbed, and stays within the list;
- it is drawn above the other children (its z-index is raised to `Integer.MAX_VALUE` until the drop, then restored);
- the child changes slot when its leading edge (top or left) passes the middle of a neighbor's slot, and the other children glide to their new place.

Releasing any mouse button drops the child: it glides to its slot, then the new order is written to `getChildren()` and the end callbacks run.

## Drag handles with startDrag

Disable the automatic drag with `auto(false)` and start drags from your own code, for example from a handle:

```java
final ReorderableFlexNode list = ReorderableFlexNode.vertical(0, 0, 400).margin(10).auto(false);
for (int i = 0; i < 12; i++) {
    final RectNode row = RectNode.create(0, 0, 400, 60).color(Color.GRAY).attach(list);
    RectNode
    .create(10, 15, 30, 30)
    .color(Color.DARKGRAY)
    .onClick((handle, mouseX, mouseY, clickType) -> {
        if (clickType.isLeft()) {
            list.startDrag(row);
        }
    })
    .attach(row);
}
list.attach(this);
```

| Method | Description |
| --- | --- |
| `startDrag(Node child)` | Starts dragging `child` as if it had been pressed at the current mouse position (`(0, 0)` when the node has no UI). Ignored while a drag is in progress. Throws `IllegalArgumentException` when `child` is not a child of this node. |
| `endDrag()` | Drops the dragged child, like a mouse release. Ignored when nothing is dragged or the drop is already running. |

Both return the `ReorderableFlexNode`.

## Auto-scroll

During a drag, once the pointer has moved 5 units from where the drag started, the `ReorderableFlexNode` looks up its ancestors for the first node with `OverflowProperty.SCROLL` whose content overflows. When the pointer is within 60 units of that node's edges along the list's main axis, the node scrolls toward that edge, faster as the pointer goes deeper into the margin (up to 2 units per frame).

- A column scrolls its ancestor vertically, a row horizontally.
- When the first scrolling ancestor scrolls on the other axis, nothing scrolls.

Place the list in a fixed-size scroll container, as in the first example (see [Overflow and Scrolling](overflow-and-scroll.md)).

## Reorder callbacks

| Method | Lambda | Fires |
| --- | --- | --- |
| `onReorderStart(NodeReorderStartCallback callback)` | `(flex, child)` | When a drag starts, from a press or from `startDrag`. Cancelling the PRE phase prevents the drag and leaves the press to the other nodes. |
| `onReorder(NodeReorderCallback callback)` | `(flex, child)` | Each time the dragged child moves to another slot, until the drop. Read the new slot with `flex.getCurrentIndex()` in the POST phase; in the PRE phase it is still the previous one. Cancelling the PRE phase keeps the child on its current slot; the callback fires again on the next frames while the child targets another slot. |
| `onReorderEnd(NodeReorderEndCallback callback)` | `(flex, child, oldIndex, newIndex)` | Once the dropped child has settled and `getChildren()` holds the new order. `oldIndex` equals `newIndex` for a drop in place. Cancelling the PRE phase only skips the POST phase: the order is already applied. When the list is detached during a drag, the child is dropped at once on its current slot. When the dragged child is removed from the list, the drag ends with `newIndex` at `-1`. |

The callback interfaces are in `dev.joid.lib.ui.node.impl.structure.reorderable.callback`. They are typed with `ReorderableFlexNode` for `flex`, and `child` is the dragged `Node`. Their callback ids are public: `ReorderableFlexNode.CALLBACK_REORDER_START`, `CALLBACK_REORDER` and `CALLBACK_REORDER_END`. PRE/POST phases are described in [Callbacks](../../interactions/callbacks.md).

The dragged child also fires its own drag callbacks (see [Drag and Drop](../../interactions/drag-drop.md)): `onDragStart` when the drag starts, `onDrag` on every frame of the drag, and `onDragEnd` once after the drop.

Saving the order once the user drops an item:

```java
ReorderableFlexNode
.vertical(0, 0, 400)
.margin(10)
.onReorderEnd((flex, child, oldIndex, newIndex) -> {
    for (final Node item : flex.getChildren()) {
        System.out.println(item.getHierarchy());
    }
})
.attach(this);
```

A veto that freezes the order while a condition holds:

```java
final BooleanSignal locked = new BooleanSignal(true);

ReorderableFlexNode
.vertical(0, 0, 400)
.onReorder(new NodeReorderCallback() {

    @Override
    public void apply(final ReorderableFlexNode flex, final Node child) {}

    @Override
    public void pre(final ReorderableFlexNode flex, final InternalContext context, final Node child) {
        if (locked.getOrDefault()) {
            context.cancel();
        }
    }

})
.attach(this);
```

## Logical order and indexes

During a drag, the order shown on screen is the logical order; `getChildren()` keeps the committed order (with the dragged child moved to the end of the list so that it draws on top) until the drop completes.

| Method | Description |
| --- | --- |
| `getChildIndex(Node child)` | Index of `child` in `getChildren()`. Throws `IllegalArgumentException` when it is not a child. |
| `getLogicalOrder()` | The live order during a drag, dragged child included. Empty outside a drag. |
| `getCurrentIndex()` | Slot currently targeted by the dragged child. |
| `getInitialIndex()` | Slot the dragged child started from. |
| `isDragging(Node child)` | `true` while `child` is the dragged child, drop animation included. |
| `getReorderedNode()` | The dragged child, or `null`. `getDraggedNode()`, inherited from `Node`, is the copy of a [`COPY` drag](../../interactions/drag-drop.md) and stays `null` here. |
| `isReleasing()` | `true` while the dropped child glides to its slot. |

Children appended during a drag join the end of the logical order and keep that place after the drop. Children removed during a drag leave it. Do not remove the dragged child itself before the drop.

## Reference

| Method | Description |
| --- | --- |
| `vertical(double x, double y, double width)`, `horizontal(double x, double y, double height)` | Factories. |
| `margin(double)`, `align(Align)`, `direction(FlexDirection)`, `auto(boolean)` | Layout and drag settings. |
| `getMargin()`, `getAlign()`, `getDirection()`, `isAutoDrag()` | Current settings. |
| `startDrag(Node)`, `endDrag()` | Drags from code. |
| `onReorderStart`, `onReorder`, `onReorderEnd` | Callbacks. |
| `getChildIndex(Node)`, `getLogicalOrder()`, `getCurrentIndex()`, `getInitialIndex()`, `isDragging(Node)`, `getReorderedNode()`, `isReleasing()` | Drag state. |
| `getDragOffset()`, `getDraggedCurrent()`, `getDraggedZindex()`, `getDragStartMouseX()`, `getDragStartMouseY()`, `isScrollArmed()`, `getChildCurrent()` | Drag bookkeeping: grab offset, animated position of the dragged child, its z-index before the drag, press position, auto-scroll armed flag, animated positions of the other children. |

Everything else is inherited from [Node](../node-fundamentals.md).

## See also

- [FlexNode](flex.md)
- [Overflow and Scrolling](overflow-and-scroll.md)
- [Drag and Drop](../../interactions/drag-drop.md)
- [Callbacks](../../interactions/callbacks.md)