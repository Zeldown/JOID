# Shapes

Everything `DrawUtils.SHAPE` can draw. Low-level, used when you need custom rendering outside the node tree.

## Rectangles

```java
DrawUtils.SHAPE.drawRect(x, y, width, height, color);
```

Simple filled rectangle. Transparent support via `color.a`.

## Rounded rectangles

```java
DrawUtils.SHAPE.drawRoundedRect(x, y, w, h, color, radius);
DrawUtils.SHAPE.drawRoundedRect(x, y, w, h, color, radius, left, top, right, bottom);
```

Per-corner rounding controlled by booleans (see [RoundedNodeEffect](../effects/rounded.md) for the semantic).

## Circles

```java
DrawUtils.SHAPE.drawCircle(centerX, centerY, color, diameter);
```

Uses `CircleShader` internally — smooth, anti-aliased via MSDF.

## Borders

```java
DrawUtils.SHAPE.drawBorder(x1, y1, x2, y2, color, stroke);
DrawUtils.SHAPE.drawFilledBorder(x1, y1, x2, y2, color, stroke);
```

- `drawBorder` — 4 thin lines around a rectangle.
- `drawFilledBorder` — 4 rectangles forming a solid outlined frame.

## Lines

```java
DrawUtils.SHAPE.drawLine(x1, y1, x2, y2, color, stroke);
```

Line segment with adjustable width.

## Polygons

```java
final double[] vertices = { 100, 100, 200, 100, 150, 200 };   // x1, y1, x2, y2, x3, y3
DrawUtils.SHAPE.drawPolygon(color, vertices);
```

Arbitrary filled polygon. Pair x/y coords — triangles, hexagons, custom shapes.

## Raw rect (for shader composition)

```java
DrawUtils.SHAPE.drawRawRect(x, y, width, height);
```

Draws a white rectangle without binding a color — useful when you've bound a shader that will color the output (e.g., gradient fills):

```java
Color.WHITE.bind();
DrawUtils.SHAPE.drawRawRect(x, y, w, h);
Color.reset();
```

Used internally by `RectNode` when gradients are involved.

## Bind empty texture

```java
DrawUtils.SHAPE.bindEmptyTexture();
```

Binds a 1×1 white texture so subsequent quad draws use vertex colors without sampling from a leftover texture. Mostly used internally by shape primitives.

## Use cases

### FPS counter

```java
@Override
public void postDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawRect(1820, 20, 80, 24, new Color(0F, 0F, 0F, 0.6F));
    DrawUtils.TEXT.drawText(1830, 24, Text.create("FPS: " + fps, info));
}
```

### Debug grid

```java
for (int x = 0; x < 1920; x += 100) {
    DrawUtils.SHAPE.drawLine(x, 0, x, 1080, new Color(1F, 1F, 1F, 0.1F), 1D);
}
for (int y = 0; y < 1080; y += 100) {
    DrawUtils.SHAPE.drawLine(0, y, 1920, y, new Color(1F, 1F, 1F, 0.1F), 1D);
}
```

### Radar view

```java
final double[] vertices = new double[points.length * 2];
for (int i = 0; i < points.length; i++) {
    vertices[i * 2]     = centerX + Math.cos(angle(i)) * radius(i);
    vertices[i * 2 + 1] = centerY + Math.sin(angle(i)) * radius(i);
}
DrawUtils.SHAPE.drawPolygon(Color.BLUE, vertices);
```

## Best practices

- **Use nodes when possible.** `DrawUtils.SHAPE.drawRect` inside a `draw()` override is fine, but `RectNode` is easier to interact with (hover, click, effects).
- **Batch where it helps.** GL state changes are cheap in small amounts; don't prematurely optimize.
- **Layer correctly.** `drawBorder` after `drawRect` to ensure borders sit on top.

## See also

- [DrawUtils](draw-utils.md).
- [Color](color.md).
