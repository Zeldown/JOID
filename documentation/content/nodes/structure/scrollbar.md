# ScrollbarNode

Visual scrollbar that follows a target node's scroll position. `ScrollbarNode` is **abstract** — you subclass it to draw the track and thumb, while the base class handles drag tracking and percentage-to-pixel mapping.

## Construction

The constructor is protected and takes the usual bounds plus a `BoundingBox` describing the scrollable content:

```java
protected ScrollbarNode(double x, double y, double width, double height, BoundingBox scroll)
```

## Usage

```java
public class MyScrollbar extends ScrollbarNode {

    public MyScrollbar(double x, double y, double w, double h, BoundingBox scroll) {
        super(x, y, w, h, scroll);
    }

    @Override
    public void drawScrollbar(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            Color.decode("#3b82f6"), 3F);
    }
}
```

Then attach it alongside the scrollable content and call `scrollNode(node)` to link the two:

```java
final Node content = /* your scrollable content */;
new MyScrollbar(392, 0, 8, 80, content.getBoundingBox())
    .scrollNode(content)
    .attach(parent);
```

## API

```java
T scrollNode(Node node)              // the node being scrolled

double getScrollWidth()               // scroll.width - this.width
double getScrollHeight()              // scroll.height - this.height
boolean isDragging()
BoundingBox getScroll()
Node getScrollNode()

abstract void drawScrollbar(double mouseX, double mouseY)
```

## Behavior

On `mousePressed` over the scrollbar, `dragging` is set to `true`. While dragging:

- If the linked `scrollNode` has X overflow, the scrollbar slides horizontally and updates the target's `scrollX` percentage (0..1).
- Else if the target has Y overflow, the scrollbar slides vertically and updates `scrollY`.

`mouseReleased` clears the dragging flag. Implement `drawScrollbar(mouseX, mouseY)` to render the track/thumb — the bounds and position have already been updated by the base class at that point.

## See also

- `Node Fundamentals` — overflow and scroll properties.