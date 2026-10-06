# Drag and Drop

Any node becomes draggable with `draggable(DraggableProperty)`. JOID then moves the node (or a copy of it) with the mouse, keeps it inside an area, snaps it onto target nodes on release and fires drag callbacks along the way.

## Making a node draggable

```java
public class BoardUI extends UI {

    @Override
    public void init() {
        RectNode
        .create(200, 200, 800, 600)
        .color(Color.WHITE)
        .body(board -> {
            RectNode.create(50, 50, 100, 100).color(Color.BLUE).draggable(DraggableProperty.parent()).attach(board);
        })
        .attach(this);
    }

}
```

![The cursor drags a blue square across a white board and releases it past the right edge: the square slides back inside](../images/drag-board.gif "The square follows the mouse during the drag and returns inside its parent on release (0.5× scale).")

The blue square follows the mouse while you hold the left button on it and stays inside the white board when you release it. `DraggableProperty` is in `dev.joid.lib.ui.node.property.draggable`.

## How a drag works

1. **Start**: a left press over the node (hovered: visible, enabled, UI on top) starts the drag, unless the property is disabled for the node or the press was consumed (see below). `onDragStart` fires. The node remembers where the mouse grabbed it and where it started.
2. **Move**: each mouse drag event sets the drag target to the mouse position minus the grab offset. `onDrag` fires.
3. **Follow**: on every frame, the node eases its absolute position toward the target, covering a sixth of the remaining distance per 1/60 s (frame-rate independent) and landing on it once closer than `0.5` unit.
4. **End**: a mouse button release ends the drag, and so does the window grabbing the mouse (`IWindowBridge.isMouseGrabbed()`). `onDragEnd` fires: with snap targets, the node heads to a target or back to its start; without, it stays where it was dropped.

> NOTE: The drag starts only when the press is still unconsumed after the node's own handlers. A child that consumes the press (a button with `onClick`, a text field...) or an `onClick` on the node itself prevents the drag. Starting a drag does not consume the press: overlapping draggable nodes under the mouse all start dragging.

## Areas with DraggableProperty factories

| Factory | Area | Bounds |
|---|---|---|
| `DraggableProperty.free()` | `FREE` | None. |
| `DraggableProperty.parent()` | `PARENT` | The parent of the dragged node. |
| `DraggableProperty.node(Node node)` | `NODE` | Another node. |
| `DraggableProperty.custom(double x, double y, double width, double height)` | `CUSTOM` | A rectangle in absolute UI units. |
| `DraggableProperty.ui()` | `UI` | The virtual canvas, `(0, 0, 1920, 1080)`. |
| `DraggableProperty.screen()` | `SCREEN` | The visible area of the window in UI units, which goes beyond the canvas when the window ratio differs from 16:9. |
| `DraggableProperty.disabled()` | `FREE` | None; dragging is disabled. |

Every factory starts with the type `MOVE`, the snap type `NEAREST`, no snap target and dragging enabled (except `disabled()`).

The area applies when the node is not being dragged: during the drag, the node follows the mouse even outside its area; on release, and whenever it lies outside while idle, it slides back inside.

## DraggableProperty reference

| Method | Description |
|---|---|
| `enabled(Predicate<Node> enabled)` | Allows dragging only when the predicate returns `true` for the node. Evaluated at each press and each frame. |
| `type(DraggableType type)` | `MOVE` (default) or `COPY`. |
| `area(DraggableAreaType areaType)` | Changes the area type. |
| `area(DraggableAreaType areaType, Object area)` | Changes the area type and its object: a `Node` for `NODE`, a `double[] {x, y, width, height}` for `CUSTOM`; any other object for these two types throws an `IllegalArgumentException`. |
| `snap(Node... snapNodes)` | Adds snap targets. |
| `snap(DraggableSnapType snapType, Node... snapNodes)` | Sets the snap type and replaces the snap targets; without nodes, removes them all. |
| `copy()` | A new property with the same settings and its own list of snap targets. |
| `isEnabled(Node node)` | Result of the enabled predicate. |
| `hasSnapping()` | `true` when at least one snap target is set. |
| `getSnapping(Node node)` | The snap target chosen for the node at its current position, or `null`. |
| `getBounds(Node node)` | The area as `{x, y, width, height}` in absolute UI units; throws an `IllegalArgumentException` for `FREE`. |
| `lerp(double frameTime, double value, double target)` | The easing step used to follow the target. |
| `getType()`, `getEnabled()`, `getAreaType()`, `getAreaObject()`, `getSnapType()`, `getSnapNodes()` | Current settings. |

The setters return the property, so you can chain them. A property holds no drag state: several nodes can share one.

## Moving or copying with DraggableType

| Type | Behavior |
|---|---|
| `MOVE` | The node itself moves. |
| `COPY` | A copy of the node (made with `copy()`, positioned absolutely, drawn after the node and its children) follows the mouse; the original stays in place. The copy is removed when the drag ends. |

With `COPY`, `getDraggedNode()` returns the copy during the drag and `null` afterwards. Snapping only reports the target through `onSnap`: create the result of the drop yourself.

```java
public class PaletteUI extends UI {

    @Override
    public void init() {
        final DraggableProperty drag = DraggableProperty.screen().type(DraggableType.COPY).snap(DraggableSnapType.OVERLAP);

        final RectNode slot = RectNode.create(800, 400, 120, 120).color(Color.GRAY).attach(this);
        drag.snap(slot);

        RectNode
        .create(100, 400, 120, 120)
        .color(Color.RED)
        .draggable(drag)
        .onSnap((node, snapNode) -> RectNode.create(10, 10, 100, 100).color(Color.RED).attach(snapNode))
        .attach(this);
    }

}
```

![A copy of a red square is dragged onto a gray slot, which then shows a red square; a second copy dropped elsewhere disappears](../images/drag-copy.gif "The copy overlaps the slot on release, so onSnap fills it; a copy dropped outside any target goes back and is removed (0.75× scale).")

`DraggableType`, `DraggableAreaType` and `DraggableSnapType` are nested in `DraggableProperty`.

## Snapping

Snap targets are nodes the dragged node lands on when you release it.

| Snap type | Target chosen on release | Without a match |
|---|---|---|
| `NEAREST` (default) | The target whose center is the closest to the center of the dragged node, at any distance. | Not possible while a target exists. |
| `OVERLAP` | The first target, in the order you added them, whose bounds overlap the dragged node. | The node goes back to where the drag started. |

When a target is chosen, `onSnap` fires and the drag target becomes the target's absolute top-left corner: the node slides there. For `COPY`, the bounds of the copy are tested. Without any snap target, the node stays where it was dropped (inside its area).

## Drag callbacks

| Method | Lambda arguments | Fires | Between PRE and POST |
|---|---|---|---|
| `onDragStart` | `(node)` | The drag starts. | Starts the drag (records the grab offset and start position, creates the copy). The POST lambda sees `isDragging()` `true`. |
| `onDrag` | `(node)` | Each mouse drag event during the drag. | Moves the drag target under the mouse. |
| `onDragEnd` | `(node)` | The drag ends. | Snaps the node or sends it back, stops the drag, removes the copy. The POST lambda sees `isDragging()` `false`. |
| `onSnap` | `(node, snapNode)` | During the drag end, when a snap target is chosen. | Sets the drag target to the snap node. |

`onSnap` runs inside the default action of `onDragEnd`, so an `onSnap` lambda runs before the `onDragEnd` lambda. In the board of the first example:

```java
RectNode
.create(50, 50, 100, 100)
.color(Color.BLUE)
.draggable(DraggableProperty.parent())
.onDragStart(node -> System.out.println("Start"))
.onDrag(node -> System.out.println("Target " + node.getTargetDragX() + ", " + node.getTargetDragY()))
.onDragEnd(node -> System.out.println("Dropped"))
.attach(board);
```

Cancelling the PRE phase vetoes the step: no drag for `onDragStart`, an unchanged target for `onDrag`, the dropped position kept for `onSnap`. Cancelling the PRE phase of `onDragEnd` skips the whole end: the node stays in the dragging state. See [Callbacks](callbacks.md#pre-and-post-phases).

The children of a [ReorderableFlexNode](../nodes/layout/reorderable-flex.md) also receive `onDragStart`, `onDrag` and `onDragEnd` while they are reordered.

## Drag state of a node

| Method | Description |
|---|---|
| `draggable(DraggableProperty draggable)` | Sets the property, stops any drag and resets the start and target positions to the current absolute position. |
| `getDraggable()` | The property, or `null`. |
| `isDragging()` | `true` between the drag start and the drag end. |
| `isDragged()` | `true` while the node moves toward its drag target. |
| `getDraggedNode()` | The copy during a `COPY` drag, otherwise `null`. |
| `getDragX()`, `getDragY()` | Grab offset: mouse position minus the node's absolute position at the start. |
| `getStartDragX()`, `getStartDragY()` | Absolute position of the node when the drag started. |
| `getTargetDragX()`, `getTargetDragY()` | Absolute position the node is heading to. |

## Driving a drag from code

| Method | Description |
|---|---|
| `startDragging(double mouseX, double mouseY)` | Starts a drag as a press at that mouse position would: fires `onDragStart`, creates the copy for `COPY`. The node must have a `DraggableProperty`. |
| `stopDragging()` | Ends the drag as a release would: fires `onDragEnd`, with `onSnap` inside it when a target is chosen. |
| `dragging(boolean dragging, double mouseX, double mouseY)` | Sets the drag state without callbacks, copy or snapping. `true` grabs the node at that mouse position; `false` stops the drag, and the node finishes its move to the current target. |
| `fireDragStart(Runnable runnable)`, `fireDrag(Runnable runnable)`, `fireDragEnd(Runnable runnable)` | Run `runnable` as the default action of the `onDragStart`, `onDrag` or `onDragEnd` callbacks (`null` runs only the callbacks). Used by nodes that manage the drag of their children. |

## See also

- [Callbacks](callbacks.md)
- [Mouse and Keyboard](mouse-and-keyboard.md)
- [ReorderableFlexNode](../nodes/layout/reorderable-flex.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)