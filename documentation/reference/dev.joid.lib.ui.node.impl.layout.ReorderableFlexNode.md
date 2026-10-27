---
summary: A flex column or row whose children the user reorders by dragging them.
---
A left press on a child starts the drag; the other children slide away, and a scrolling parent scrolls when the child nears its edge.
When the button is released, the child slides into its new place and the children take the new order.
Pinned children keep their place.

Learn: [Reorderable lists](/use/learn/interactivity/reorderable-lists/).

## vertical(double,double,double)

Creates a reorderable column at `x`, `y`, `width` wide.

## horizontal(double,double,double)

Creates a reorderable row at `x`, `y`, `height` high.

## ReorderableFlexNode(double,double,double,double)

Called by a subclass to create a column.

## ReorderableFlexNode(double,double,double,double,Axis)

Called by a subclass with the direction.

## autoDrag

If `true`, a press on a child starts dragging it; if `false`, only `startDrag` does, such as from a handle.

## startDrag(Node)

Starts dragging this child from the mouse position, unless a drag runs or the child is pinned.
Throws an `IllegalArgumentException` when the node is not a child of the list.

## endDrag()

Drops the dragged child; when a `beforeDragDrop` listener refuses, the child goes back to its first place.

## pin(Node[])

Keeps these children at their place: they cannot be dragged and the others move around them.

## unpin(Node[])

Lets these children move again.

## isPinned(Node)

If `true`, the child is pinned.

## isDragging(Node)

If `true`, this child is being dragged.

## getReorderedNode()

The child being dragged, or `null`.

## getChildIndex(Node)

The place of the child in the list, in the order shown during a drag.
Throws an `IllegalArgumentException` when the node is not a child of the list.

## getPinnedNodes()

The pinned children; a read-only view.

## getLogicalOrder()

The order of the children during a drag; empty when no drag runs.

## getChildCurrent()

The offset shown for each child that slides during a drag.

## beforeReorder(EventListener)

Adds a listener that runs before the dragged child takes another place; cancelling the event keeps it at its place.

## onReorderStart(EventListener)

Adds a listener that runs when a drag starts.

## onReorder(EventListener)

Adds a listener that runs each time the dragged child takes another place.

## onReorderEnd(EventListener)

Adds a listener that runs when the dragged child has settled, with its first and its last place.

## layout()

Moves the dragged child with the mouse and slides the others, then places the children.

## mousePressed(NodeMouseEvent)

Starts dragging the hovered child on a left press when `autoDrag` is on, and consumes the press.

## mouseReleased(NodeMouseEvent)

Drops the dragged child when the left button is released.

## detach()

Ends a drag in progress, keeping the order reached.

## isDraggingChild(Node)

If `true`, the child is dragged by the list and drawn above its siblings.

## getLayoutOrder()

The children in the order shown during a drag.

## layoutChild(Node,double)

Places a child at the offset it slides through during a drag.
