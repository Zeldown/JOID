# CircleNode

A filled circle. Simpler than using `RectNode + CircleNodeEffect` when you just want a circular shape.

## Create

```java
CircleNode.create(x, y, diameter)
    .color(Color.RED)
    .attach(parent);
```

The `x` / `y` represent the top-left of the bounding square; the circle is inscribed.

## API

```java
node.color(Color color);
node.color(Color normal, Color hovered);
node.border(Color color, double stroke);
```

Same color/border semantics as [RectNode](rect.md).

## When to use RectNode + CircleNodeEffect instead

If you need:

- Rectangular aspect ratio (ellipse-like effect) → use `RectNode` with `CircleNodeEffect` — the effect uses `min(w, h)` as the diameter.
- Gradients on the circle → `RectNode.color(gradient).effect(CircleNodeEffect.create())` routes through the shader pipeline and handles it correctly.

Plain `CircleNode` is DrawUtils-based and doesn't compose shader passes.

## Example — avatar badge

```java
CircleNode.create(0, 0, 48)
    .color(Color.decode("#3b82f6"))
    .border(Color.WHITE, 2D)
    .hover(() -> "Online")
    .attach(parent);
```

## See also

- [RectNode](rect.md) — when you need gradients or compositing.
- [Circle Effect](../../effects/circle.md).
