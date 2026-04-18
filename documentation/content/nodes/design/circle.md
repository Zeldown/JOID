# CircleNode

A filled circle. Simpler than `RectNode + CircleNodeEffect` when you only need a solid circular shape.

## Create

```java
CircleNode.create(x, y, diameter)
    .color(Color.RED)
    .attach(parent);
```

The third parameter is the **diameter** (the bounding square's side). `x / y` point to the top-left of that square; the circle is inscribed. The node's `width` and `height` both equal `diameter`.

## API

```java
T color(Color color)
T color(Color normal, Color hovered)
T hoveredColor(Color color)
```

That's the full surface: there is no `border(...)` on `CircleNode`. For an outlined circle, use `RectNode + BorderNodeEffect + CircleNodeEffect`, or draw the outline yourself with `DrawUtils.SHAPE`.

The hover colour is interpolated via `hoverValue(1F)` on each draw.

## When to use `RectNode + CircleNodeEffect` instead

- **Gradients** on the circle — `RectNode.color(gradient).effect(CircleNodeEffect.create())` goes through the shader pipeline and handles gradient shading correctly.
- **Ellipse-like shapes** — the effect uses `min(width, height)` as the diameter, so a non-square `RectNode` yields an ellipse-shaped mask.
- **Borders or other effects** — only the `RectNode` pipeline composes effects.

Plain `CircleNode` is `DrawUtils.SHAPE.drawCircle`-based and does not compose shader passes.

## Example — avatar badge

```java
CircleNode.create(0, 0, 48)
    .color(Color.decode("#3b82f6"))
    .hover(() -> "Online")
    .attach(parent);
```

## See also

- [RectNode](rect.md) — when you need gradients, borders, or compositing.
- [Circle Effect](../../effects/circle.md) — circle mask over any node.