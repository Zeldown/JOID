# DrawUtils

Static entry points for ad-hoc drawing outside the node tree. Use in `preDraw` / `postDraw`, custom `Node.draw()` overrides, or quick prototypes.

## The four facades

```java
DrawUtils.SHAPE      // rectangles, circles, lines, polygons
DrawUtils.RESOURCE   // images, textures
DrawUtils.TEXT       // text rendering
DrawUtils.MODEL      // 3D OBJ models
```

Each is a pre-constructed instance — no `new DrawShape()` needed.

## `DrawUtils.SHAPE`

```java
DrawUtils.SHAPE.drawRect(x, y, width, height, color);
DrawUtils.SHAPE.drawRoundedRect(x, y, w, h, color, radius);
DrawUtils.SHAPE.drawRoundedRect(x, y, w, h, color, radius, left, top, right, bottom);
DrawUtils.SHAPE.drawCircle(centerX, centerY, color, diameter);
DrawUtils.SHAPE.drawBorder(x1, y1, x2, y2, color, stroke);
DrawUtils.SHAPE.drawFilledBorder(x1, y1, x2, y2, color, stroke);
DrawUtils.SHAPE.drawLine(x1, y1, x2, y2, color, stroke);
DrawUtils.SHAPE.drawPolygon(color, vertices);
DrawUtils.SHAPE.drawRawRect(x, y, w, h);         // no color — for shader composition
DrawUtils.SHAPE.bindEmptyTexture();               // bind a 1x1 white texture
```

See [Shapes](shapes.md) for details and use cases.

## `DrawUtils.RESOURCE`

```java
DrawUtils.RESOURCE.drawResource(x, y, resource);                      // at natural size
DrawUtils.RESOURCE.drawResource(x, y, width, height, resource);       // stretched
DrawUtils.RESOURCE.drawScaledResourceWidth(x, y, width, resource);    // height auto
DrawUtils.RESOURCE.drawScaledResourceHeight(x, y, height, resource);  // width auto
DrawUtils.RESOURCE.drawCenteredResource(x, y, w, h, resource);        // CONTAIN fit
```

## `DrawUtils.TEXT`

```java
DrawUtils.TEXT.drawText(x, y, Text.create("Hello", info));
DrawUtils.TEXT.drawText(x, y, text, maxWidth, TextOverflow.ELLIPSIS);
```

## `DrawUtils.MODEL`

```java
DrawUtils.MODEL.drawModel(x, y, z, scale, model);
DrawUtils.MODEL.drawModel(x, y, z, scaleX, scaleY, scaleZ, rotX, rotY, rotZ, model);
```

## Use cases

### Overlay in `postDraw`

```java
@Override
public void postDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.WHITE, 8);
}
```

### Background in `preDraw`

```java
@Override
public void preDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawRect(0, 0, 1920, 1080, Color.decode("#111827"));
}
```

### Custom node `draw()`

```java
@Override
public void draw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawRect(getX(), getY(), getWidth(), getHeight(), Color.RED);
    DrawUtils.TEXT.drawText(getX() + 8, getY() + 8, text);
}
```

## Best practices

- **Prefer nodes over DrawUtils.** Nodes integrate with hover, drag, effects, and the tree — `DrawUtils` is raw GL with no interactivity.
- **Use `DrawUtils` for overlays only.** FPS counters, cursors, debug outlines.
- **Don't fight the effect system.** If you're reaching for `DrawUtils.SHAPE.drawRoundedRect` to hand-draw a rounded card, just use a `RectNode` + `RoundedNodeEffect`.

## See also

- [Shapes](shapes.md) — shape drawing details.
- [Color](color.md) — color system.
