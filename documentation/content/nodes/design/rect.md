# RectNode

The workhorse of JOID. A filled rectangle with optional border, gradient, rounded corners, and hover colors. Most UI elements are built on top of `RectNode`.

## Create

```java
RectNode.create(x, y, width, height)
    .color(Color.RED)
    .attach(parent);
```

## Color

```java
node.color(Color color);
node.color(Color normal, Color hovered);
node.color(Supplier<Color> colorFn);
node.color(Supplier<Color> colorFn, Supplier<Color> hoveredColorFn);
node.hoveredColor(Color color);
```

When both `color` and `hovered` are provided, the renderer interpolates via `node.hoverValue(1F)` automatically.

Colors can be plain or **gradients** — the renderer detects it and uses the shader pipeline:

```java
node.color(Color.RED.toGradient(Color.BLUE));
node.color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))); // vertical
```

## Border

```java
node.border(Color color, double stroke);
node.border(Color color, double stroke, boolean fill);
node.border(Color color, Color hoveredColor, double stroke, boolean fill);
```

- `stroke` — border width in logical units.
- `fill` — `true` (default): the border fills the stroke area. `false`: the border is an outline leaving the stroke as empty space.

Borders also support gradients:

```java
node.border(Color.WHITE.toGradient(Color.GRAY), 2D);
```

## Effects

Combine with effects for rounded corners, circles, blur:

```java
RectNode.create(0, 0, 200, 100)
    .color(Color.BLUE.toGradient(Color.MAGENTA))
    .border(Color.WHITE, 2D)
    .effect(RoundedNodeEffect.create(16F))
    .attach(parent);
```

When `RoundedNodeEffect` or `CircleNodeEffect` is present, `RectNode` routes its render through the [Shader Pipeline](../../shaders/pipeline.md). All combinations work:

```java
RectNode.create(0, 0, 200, 100)
    .color(Color.RED.toGradient(Color.YELLOW))
    .effect(CircleNodeEffect.create())
    .effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.IN))
    .attach(parent);
```

## Example — card with hover

```java
RectNode.create(0, 0, 400, 120)
    .color(
        Color.decode("#1f2937"),
        Color.decode("#374151")
    )
    .effect(RoundedNodeEffect.create(12F))
    .effect(BorderNodeEffect.create(Color.decode("#4b5563"), 1F))
    .body(card -> {
        TextNode.create(16, 16)
            .text(Text.create("Hello world", info))
            .attach(card);
    })
    .attach(parent);
```

## Best practices

- **Always set a color.** A `RectNode` without `color()` is transparent (Color.TRANSPARENT default).
- **Prefer effects over manual drawing.** Don't nest a smaller `RectNode` inside a rounded one to fake a border — use `border()` and `effect()`.
- **Use gradients through `Color.toGradient()`.** Don't bind shaders manually; the renderer does it.

## See also

- [Rounded Effect](../../effects/rounded.md)
- [Circle Effect](../../effects/circle.md)
- [Border Effect](../../effects/border.md)
- [Color](../../drawing/color.md) — `Color.toGradient(...)` for gradient fills.
