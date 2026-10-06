# Overflow and Scrolling

A node's overflow decides what happens to the children that go beyond its bounds: they can spill out, be clipped, or be scrolled with the mouse wheel, from code or with a scrollbar. This page covers `OverflowProperty`, the scroll API every node inherits, `ScrollbarNode` and the scroll callbacks.

## A scrolling list

```java
RectNode
.create(660, 240, 600, 400)
.color(Color.BLACK)
.overflow(OverflowProperty.SCROLL)
.body(area -> {
    FlexNode
    .vertical(0, 0, 600)
    .margin(10)
    .body(flex -> {
        for (int i = 0; i < 30; i++) {
            RectNode.create(0, 0, 600, 60).color(Color.GRAY).attach(flex);
        }
    })
    .attach(area);
})
.attach(this);
```

![The mouse wheel scrolls a list of gray rows inside a black area](../../images/overflow-scroll.gif "The wheel eases the list down; rows outside the area are clipped.")

The list is 2090 units high in a 400-unit area: hovering the area and turning the mouse wheel scrolls it, and everything outside the area is clipped.

## OverflowProperty

`overflow(OverflowProperty overflow)` sets the overflow of a node; `getOverflow()` reads it. `OverflowProperty` is in `dev.joid.lib.ui.node.property.overflow`.

| Value | Behavior |
| --- | --- |
| `NONE` | Default. Children are drawn wherever they are, including outside the node. |
| `HIDDEN` | The node's drawing, its children and its layers are clipped to the node's bounds. |
| `SCROLL` | Clipped like `HIDDEN`, and the content can be scrolled. |

```java
RectNode
.create(100, 100, 200, 200)
.color(Color.DARKGRAY)
.overflow(OverflowProperty.HIDDEN)
.body(box -> {
    RectNode.create(120, 120, 140, 140).color(Color.decode("#A78BFA")).attach(box);
})
.attach(this);
```

![A violet square spilling out of a gray box with NONE, and cut at the box edge with HIDDEN](../../images/overflow-modes.png "With NONE the child spills out of its parent; with HIDDEN it is clipped to the parent's bounds.")

With `HIDDEN` or `SCROLL`, the node becomes the overflow area of its descendants, up to the next descendant that has its own overflow:

- A descendant entirely outside the area is not drawn and reports `isVisible()` as `false`.
- A descendant is only hovered (and so clickable) when the pointer is also over the area: a child sticking out of a clipped node does not react in its hidden part.
- Nested clipping areas combine: a descendant is clipped by every clipping ancestor.

## How scrolling works

- A scroll container scrolls along one axis at a time: horizontally when its content is wider than it, otherwise vertically when its content is taller.
- The content size is measured on every rendered frame from the direct children: their default position plus their current size. The smallest child offset is added once more at the end, so the content ends with the same gap it starts with. A child placed at `y = 10` therefore leaves 10 units free above the first item and below the last one when fully scrolled.
- Scroll offsets are negative: `0` is the start and `-getMaxScrollY()` the end.
- While the content overflows, the direct children are moved on every frame to their default position plus the scroll offset, rounded to whole screen pixels. Changing a direct child's position along the scroll axis has no lasting effect: put a single layout child (a `FlexNode`, `GridNode` or `ContainerNode`) inside the scroll container and arrange the items in it, as in the example above.
- The current offset eases toward a target offset on every frame, independently of the frame rate. All the scroll methods move the target; `updateScroll()` jumps to it.

> NOTE: `FlexNode`, `GridNode` and `ReorderableFlexNode` position their children on every frame, so setting `SCROLL` on them does not scroll them. Keep them at `NONE`, so that they grow with their children, and wrap them in a fixed-size node with `SCROLL`.

## Wheel scrolling

When the pointer is over a `SCROLL` node whose content overflows (the node must be visible and enabled), each wheel event moves the target by 30 units × `scrollSpeed`, twice as much while Left Control is held. Wheel up (a positive value) scrolls toward the start, wheel down toward the end. The node consumes the wheel event.

Children receive the wheel event before their parent, so in nested scroll containers the innermost hovered one scrolls first. A [`MultilineTextFieldNode`](../input/multiline-text-field.md#mouse-and-scrolling) that cannot scroll further in the direction of the wheel leaves the event to its parent.

| Method | Description |
| --- | --- |
| `scrollSpeed(double scrollSpeed)` | Multiplier of the wheel step. Default `1`. |
| `getScrollSpeed()` | Current multiplier. |

## Scrolling from code with setScrollX and setScrollY

| Method | Description |
| --- | --- |
| `setScrollX(double offset)`, `setScrollY(double offset)` | Sets the target offset, clamped between the end (`-getMaxScrollX()`/`-getMaxScrollY()`) and `0`. |
| `setScrollX(float percent)`, `setScrollY(float percent)` | Sets the target to a fraction of the maximum: `0F` is the start, `1F` the end. |
| `scrollX(double value, double speed)`, `scrollY(double value, double speed)` | Moves the target by `value × speed`: positive toward the start, negative toward the end. |
| `updateScroll()` | Sets the current offset to the target on both axes, without easing. |
| `updateScrollX()`, `updateScrollY()` | Same on one axis. |

All of them return the node.

```java
list.setScrollY(1F).updateScroll();
list.scrollY(-200D, 1D);
list.setScrollY(0F);
```

The first line jumps to the end, the second eases 200 units further down, the third eases back to the top.

> WARNING: `setScrollX(float)` takes a fraction and `setScrollX(double)` an offset. An integer literal selects the `float` overload: `setScrollY(-100)` means −100 × the maximum and ends at `0`. Always write the suffix: `setScrollY(-100D)` for an offset, `setScrollY(0.5F)` for a fraction.

The maximum is measured while the node renders, so it is `0` until the node has been drawn once, and a scroll set before that is clamped to `0`. To open a list at a given position, scroll from `onMount`, which runs after the measurement of the first frame:

```java
RectNode
.create(660, 240, 600, 400)
.overflow(OverflowProperty.SCROLL)
.onMount(area -> area.setScrollY(1F).updateScroll())
.body(area -> {
    FlexNode
    .vertical(0, 0, 600)
    .margin(10)
    .body(flex -> {
        for (int i = 0; i < 30; i++) {
            RectNode.create(0, 0, 600, 60).color(Color.GRAY).attach(flex);
        }
    })
    .attach(area);
})
.attach(this);
```

### Scroll state

| Method | Description |
| --- | --- |
| `getScrollX()`, `getScrollY()` | Current offset, between the end and `0`. |
| `getTargetScrollX()`, `getTargetScrollY()` | Target offset. |
| `getMaxScrollX()`, `getMaxScrollY()` | Scroll distance available, measured on the last rendered frame. `0` when the content fits. |
| `hasOverflowX()`, `hasOverflowY()` | `true` when the maximum on that axis is above `0`. |
| `isScrollEndX()`, `isScrollEndY()` | `true` from the moment the target reaches the end (`onScrollEnding`) until the offset arrives there (`onScrollEnd`). |
| `getScrollbar()` | The scrollbar set with `scrollbar(...)`, or `null`. |

## Scroll callbacks

| Method | Lambda | Fires |
| --- | --- | --- |
| `onScrollUpdate(NodeScrollUpdateCallback<T>)` | `(node, value)` | On every call that sets a target: wheel, `scrollX`/`scrollY`, `setScrollX`/`setScrollY`, scrollbar drag, auto-scroll of a [ReorderableFlexNode](reorderable-flex.md). `value` is the requested offset on that axis, before clamping. Cancelling the PRE phase leaves the target unchanged. |
| `onScrollEnding(NodeScrollEndingCallback<T>)` | `(node, scrollX, scrollY)` | As soon as a call moves the target onto the end. Receives the target offsets. It fires once per arrival: again only after the target has left the end. |
| `onScrollEnd(NodeScrollEndCallback<T>)` | `(node, scrollX, scrollY)` | When the eased offset reaches the end announced by `onScrollEnding`. It does not fire when the target leaves the end first, or when the content grows before the offset gets there. |

The callback interfaces are in `dev.joid.lib.ui.node.callback.impl.scroll`. PRE/POST phases and cancellation are described in [Callbacks](../../interactions/callbacks.md).

```java
RectNode
.create(660, 240, 600, 400)
.overflow(OverflowProperty.SCROLL)
.onScrollUpdate((area, value) -> System.out.println("target " + value))
.onScrollEnd((area, scrollX, scrollY) -> System.out.println("arrived at " + scrollY))
.attach(this);
```

### Infinite lists with onScrollEnding

`onScrollEnding` fires before the content has reached the end, which leaves time to append more items. When the content grows, the end moves away, `onScrollEnd` is not fired and the user keeps scrolling:

```java
RectNode
.create(660, 240, 600, 400)
.color(Color.BLACK)
.overflow(OverflowProperty.SCROLL)
.onScrollEnding((area, scrollX, scrollY) -> {
    final FlexNode list = area.getChild(0, FlexNode.class).get();
    for (int i = 0; i < 10; i++) {
        RectNode.create(0, 0, 600, 60).color(Color.GRAY).attach(list);
    }
})
.body(area -> {
    FlexNode
    .vertical(0, 0, 600)
    .margin(10)
    .body(flex -> {
        for (int i = 0; i < 10; i++) {
            RectNode.create(0, 0, 600, 60).color(Color.GRAY).attach(flex);
        }
    })
    .attach(area);
})
.attach(this);
```

## ScrollbarNode

`ScrollbarNode` (`dev.joid.lib.ui.node.impl.structure.scrollbar`) is an abstract node that shows and drives the scroll of a node. The node itself is the thumb; a `BoundingBox` (`dev.joid.lib.utils.box`) describes the track it slides along. Extend it and draw both in `drawScrollbar`:

```java
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.scrollbar.ScrollbarNode;
import dev.joid.lib.utils.box.BoundingBox;

public class SimpleScrollbarNode extends ScrollbarNode {

    protected SimpleScrollbarNode(final double x, final double y, final double width, final double height, final BoundingBox track) {
        super(x, y, width, height, track);
    }

    public static SimpleScrollbarNode create(final double x, final double y, final double width, final double height, final BoundingBox track) {
        return new SimpleScrollbarNode(x, y, width, height, track);
    }

    @Override
    public void drawScrollbar(final double mouseX, final double mouseY) {
        final BoundingBox track = this.getScroll();
        DrawUtils.SHAPE.drawRect(track.getMinX(), track.getMinY(), track.getWidth(), track.getHeight(), Color.DARKGRAY);
        DrawUtils.SHAPE.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), Color.WHITE);
    }

}
```

Give it to the scroll container with `scrollbar(...)`:

```java
RectNode
.create(660, 240, 600, 400)
.color(Color.BLACK)
.overflow(OverflowProperty.SCROLL)
.scrollbar(SimpleScrollbarNode.create(610, 0, 10, 60, BoundingBox.create(610, 0, 10, 400)))
.body(area -> {
    FlexNode
    .vertical(0, 0, 600)
    .margin(10)
    .body(flex -> {
        for (int i = 0; i < 30; i++) {
            RectNode.create(0, 0, 600, 60).color(Color.GRAY).attach(flex);
        }
    })
    .attach(area);
})
.attach(this);
```

![The cursor drags a white scrollbar thumb down its track and the list follows](../../images/overflow-scrollbar.gif "Dragging the thumb scrolls the list; the scrollbar sits beside the clipped area.")

### How the scrollbar behaves

- `scrollbar(ScrollbarNode scrollbar)` links the scrollbar to the node (its scroll node and its parent) and loads it when the node already has a UI. Do not `attach` a scrollbar: it is not a child of the node. A new call replaces the previous scrollbar.
- Its coordinates, and the track's, are relative to the scrolled node. The scrollbar is drawn after the content, outside the clip, so it can sit beside the node (at x = 610 next to a 600-unit area above).
- It is drawn only while the node's overflow is `SCROLL` and its content overflows.
- It follows the axis the node scrolls on: horizontal when the content overflows horizontally, otherwise vertical.
- Create the thumb at the start of the track. The thumb travels `getScrollWidth()` (track width − thumb width) or `getScrollHeight()` (track height − thumb height): its position is its default position plus that travel × the scrolled fraction.
- Pressing the thumb consumes the press and starts a drag: the thumb's center follows the mouse inside the track and the scroll target follows the thumb; the content eases faster than with the wheel while the thumb is dragged. Releasing any mouse button ends the drag.
- The scrollbar receives the mouse and key events of the scrolled node before its children. Its `draw` is final; its `drawSkeleton` draws the scrollbar as well.

### ScrollbarNode reference

| Method | Description |
| --- | --- |
| `ScrollbarNode(double x, double y, double width, double height, BoundingBox scroll)` | Protected constructor: thumb bounds and track. |
| `drawScrollbar(double mouseX, double mouseY)` | Abstract. Draws the track and the thumb. |
| `scrollNode(Node scrollNode)` | Sets the scrolled node. Called by `Node.scrollbar(...)`. Returns the scrollbar. |
| `getScrollNode()` | The scrolled node, or `null`. Nothing is drawn without one. |
| `getScroll()` | The track. |
| `getScrollWidth()`, `getScrollHeight()` | Travel of the thumb: track size − thumb size. |
| `isDragging()` | `true` while the thumb is dragged. |

## Node scroll reference

| Method | Description |
| --- | --- |
| `overflow(OverflowProperty)`, `getOverflow()` | Overflow mode. Default `NONE`. |
| `scrollbar(ScrollbarNode)`, `getScrollbar()` | Scrollbar. |
| `scrollSpeed(double)`, `getScrollSpeed()` | Wheel multiplier. Default `1`. |
| `setScrollX(double)`, `setScrollY(double)`, `setScrollX(float)`, `setScrollY(float)` | Absolute targets: offset or fraction. |
| `scrollX(double, double)`, `scrollY(double, double)` | Relative targets. |
| `updateScroll()`, `updateScrollX()`, `updateScrollY()` | Jump to the target. |
| `getScrollX()`, `getScrollY()`, `getTargetScrollX()`, `getTargetScrollY()`, `getMaxScrollX()`, `getMaxScrollY()` | Offsets. |
| `hasOverflowX()`, `hasOverflowY()`, `isScrollEndX()`, `isScrollEndY()` | Scroll state. |
| `onScrollUpdate`, `onScrollEnding`, `onScrollEnd` | Callbacks. |

## See also

- [Node Fundamentals](../node-fundamentals.md)
- [ContainerNode](container.md)
- [FlexNode](flex.md)
- [ReorderableFlexNode](reorderable-flex.md)
- [Callbacks](../../interactions/callbacks.md)