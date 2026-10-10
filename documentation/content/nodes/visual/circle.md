# CircleNode

`CircleNode` draws a filled circle inscribed in its square bounds, with an optional hover color. Use it for dots, avatar placeholders, status indicators and cursors.

```java
CircleNode.create(100, 100, 50).color(Color.LIGHTGRAY).attach(this);
```

![A light gray circle](../../images/circle-basic.png "A CircleNode of diameter 50, shown at 2× scale.")

`create(x, y, diameter)` places the top-left corner of the bounding square at `x, y` and sets the width and the height to `diameter`. The default color is `Color.WHITE`.

## Hover with hoveredColor

With a hovered color, the fill blends to it while the mouse is over the node, like a [RectNode](rect.md). A status dot that lights up:

```java
CircleNode.create(20, 20, 16).color(Color.DARKGRAY).hoveredColor(Color.WHITE).attach(this);
```

![The cursor hovers a small dark gray dot that fades to white](../../images/circle-hover.gif "The dot blends to its hovered color while the mouse is over it (2× scale).")

Both colors take a plain value, an expression that reads [signals](../../concepts/state.md) (`color(this.online.get() ? Color.WHITE : Color.GRAY)`), a signal or a `Supplier`, and can be gradients built with `Color.toGradient(...)`.

## Size and shape

The circle is centered on the node and its radius is half of the smaller side: a node resized to a non-square size draws the largest circle that fits. For a circle with a border, a blur or children clipped to it, use a `RectNode` with a `CircleNodeEffect` (see [Effects](../../styling/effects.md)).

![A red-to-yellow gradient disk with a white ring, drawn by a RectNode with CircleNodeEffect and a border](../../images/circle-effect-ring.png "A RectNode with CircleNodeEffect takes borders and clips its children.")

## Reference

| Method | Default | Description |
|---|---|---|
| `create(x, y, diameter)` | | Creates a white circle. |
| `color(Color)`, `color(Supplier<Color>)` | `Color.WHITE` | Fill color. |
| `hoveredColor(Color)`, `hoveredColor(Supplier<Color>)` | none | Color under the mouse; `(Color) null` removes it. |
| `getColor()`, `getHoveredColor()` | | Current colors (`null` without hover color). |

## Good to know

- `hoveredColor((Color) null)` removes the hover color; a bare `null` does not compile.
- The circle reacts to the mouse in the corners of its square: hit testing uses the bounds.

## See also

- Next: [TextNode](text.md)
- [RectNode](rect.md)
- [Effects](../../styling/effects.md)
- [Styling](../../concepts/styling.md)
- [Drawing](../../drawing/drawing.md) for circles without a node