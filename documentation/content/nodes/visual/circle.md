# CircleNode

`CircleNode` (`dev.joid.lib.ui.node.impl.design.shape`) draws a filled circle inscribed in its square bounds, with an optional hover color. Use it for dots, avatar placeholders, status indicators and cursors.

## Creating a CircleNode

```java
CircleNode.create(100, 100, 50).color(Color.GREEN).attach(this);
```

![A green circle](../../images/circle-basic.png "A CircleNode of diameter 50, shown at 2× scale.")

`create(x, y, diameter)` places the top-left corner of the bounding square at `x, y` and sets both the width and the height to `diameter`. The circle is centered on the middle of that square.

A status dot that lights up under the mouse:

```java
CircleNode.create(20, 20, 16).color(Color.DARKGRAY, Color.GREEN).attach(this);
```

![The cursor hovers a small gray dot that fades to green](../../images/circle-hover.gif "The dot blends to its hovered color while the mouse is over it (2× scale).")

## Colors with color and hoveredColor

| Method | Description |
| --- | --- |
| `color(Color color)` | Sets the fill color. |
| `color(Color color, Color hoveredColor)` | Sets the fill and hovered colors. |
| `hoveredColor(Color color)` | Sets or replaces the hovered color. |

- The default color is `Color.WHITE`, without hovered color.
- With a hovered color, the drawn color blends from the fill color to the hovered color following the node's hover animation (see [Hover and Tooltips](../../interactions/hover.md)).
- Both colors can be gradients built with `Color.toGradient(...)`; the gradient spans the circle's bounding square (see [Colors and Gradients](../../styling/colors.md)).
- Colors are fixed values: no supplier overloads. Every parameter is non-null, so a hovered color cannot be removed once set; set it to the fill color instead.

## Size and shape

The radius is half of the node's width, and the center is `(x + width / 2, y + width / 2)`. Only the width is read: if you resize the node to a non-square size (with `size(...)`, `width(...)` or a layout), the circle keeps a diameter equal to the width.

> TIP: For a circle with a border, a blur or children clipped to the circle, use a [`RectNode`](rect.md) with a [`CircleNodeEffect`](../../styling/circle.md) instead.

## Reference

### Factory

| Method | Description |
| --- | --- |
| `CircleNode.create(double x, double y, double diameter)` | Creates a white circle of the given diameter. |

### Properties

| Method | Default | Description |
| --- | --- | --- |
| `color(Color)` | `Color.WHITE` | Fill color. |
| `color(Color, Color)` | | Fill and hovered colors. |
| `hoveredColor(Color)` | none | Color reached when hovered. |

Every setter returns the node itself, typed by the generic return of the fluent API.

### Getters

| Method | Description |
| --- | --- |
| `getColor()` | Fill color. |
| `getHoveredColor()` | `Optional<Color>`, empty without hovered color. |

### Loading skeleton

While the node waits for a condition set with `wait(...)`, it draws a pulsing grey circle (`Color.LOADING()`) instead of the default rectangular placeholder (see [Node Fundamentals](../node-fundamentals.md)).

## See also

- [RectNode](rect.md)
- [CircleNodeEffect](../../styling/circle.md)
- [Colors and Gradients](../../styling/colors.md)
- [Shapes](../../drawing/shapes.md) for drawing circles without a node