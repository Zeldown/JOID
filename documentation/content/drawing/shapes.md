# Shapes

`DrawShape` is the facade for every shape primitive JOID can render without going through the node tree. Reach it through `DrawUtils.SHAPE` or directly via `DrawShape.getInstance()` — both return the same singleton. Every method is `void` and writes into the current GL context immediately.

## Rectangles

### `drawRect`

```java
void drawRect(double x, double y, double width, double height, Color color)
```

Filled axis-aligned rectangle. Transparency through `color.a`. Internally routed through `drawPolygon` with four corner points.

```java
DrawUtils.SHAPE.drawRect(0, 0, 200, 100, Color.WHITE);
DrawUtils.SHAPE.drawRect(0, 0, 200, 100, new Color(1F, 1F, 1F, 0.2F));
```

### `drawRoundedRect`

```java
void drawRoundedRect(double x, double y, double width, double height, Color color, float radius)
void drawRoundedRect(double x, double y, double width, double height, Color color, float radius, boolean roundedLeft, boolean roundedTop, boolean roundedRight, boolean roundedBottom)
```

Rectangle with rounded corners, rendered through `RoundedShader` — the same shader backing `RoundedNodeEffect`. The four-corner overload disables rounding on specific sides for tabs or notched layouts.

```java
DrawUtils.SHAPE.drawRoundedRect(40, 40, 120, 60, Color.decode("#1f2937"), 8F);

DrawUtils.SHAPE.drawRoundedRect(40, 40, 120, 60, Color.decode("#1f2937"),
    8F, true, true, false, false);
```

### `drawRawRect`

```java
void drawRawRect(double x, double y, double width, double height)
```

Quad draw without binding a color. Useful when a shader is already active and should paint the output — used internally by `RectNode` when gradients are involved.

```java
Color.WHITE.bind();
DrawUtils.SHAPE.drawRawRect(x, y, w, h);
Color.reset();
```

## Circles

### `drawCircle`

```java
void drawCircle(double centerX, double centerY, Color color, double radius)
```

Anti-aliased disk carved by `CircleShader` inside a quad from `(centerX - radius, centerY - radius)` to `(centerX + radius, centerY + radius)`.

```java
DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.WHITE, 8D);
```

## Borders

### `drawBorder`

```java
void drawBorder(double x, double y, double x2, double y2, Color color)
void drawBorder(double x, double y, double x2, double y2, Color color, double stroke)
```

Four thin rectangles drawn **outside** the box defined by `(x, y) → (x2, y2)`. Use it for crisp 1-pixel outlines.

```java
DrawUtils.SHAPE.drawBorder(10, 10, 210, 110, Color.decode("#374151"), 1D);
```

### `drawFilledBorder`

```java
void drawFilledBorder(double x, double y, double x2, double y2, Color color)
void drawFilledBorder(double x, double y, double x2, double y2, Color color, double stroke)
```

Same geometry as `drawBorder`, but the corners are filled in — the outline is a solid frame without gaps.

```java
DrawUtils.SHAPE.drawFilledBorder(10, 10, 210, 110, Color.decode("#374151"), 2D);
```

## Lines

### `drawLine`

```java
void drawLine(Color color, Vector2d... points)
void drawLine(Color color, float stroke, Vector2d... points)
```

Line strip connecting every consecutive pair of points. `GL_LINE_SMOOTH` is enabled for the duration of the call. The stroke overload sets `glLineWidth` around the draw.

```java
DrawUtils.SHAPE.drawLine(Color.WHITE,
    new Vector2d(0, 0),
    new Vector2d(100, 50),
    new Vector2d(200, 0)
);

DrawUtils.SHAPE.drawLine(Color.WHITE, 2F,
    new Vector2d(0, 0), new Vector2d(300, 0)
);
```

### `drawDashedLine`

```java
void drawDashedLine(Color color, int pattern, float stroke, Vector2d... points)
```

Line strip using `GL_LINE_STIPPLE` with a fixed dash/gap pattern (`0xAAAA`). The `pattern` parameter is the stipple factor — higher values produce longer dashes.

```java
DrawUtils.SHAPE.drawDashedLine(Color.decode("#4ade80"), 2, 1F,
    new Vector2d(0, 50), new Vector2d(300, 50)
);
```

### `drawCurvedLine`

```java
void drawCurvedLine(Color color, Vector2d start, Vector2d end, Vector2d control)
void drawCurvedLine(Color color, float stroke, Vector2d start, Vector2d end, Vector2d control)
void drawCurvedLine(Color color, Vector2d start, Vector2d startControl, Vector2d end, Vector2d endControl)
void drawCurvedLine(Color color, float stroke, Vector2d start, Vector2d startControl, Vector2d end, Vector2d endControl)
```

Bézier curve approximated by line segments — quadratic (one control point) or cubic (two). Segment count scales with the linear distance between endpoints, so longer curves get more segments automatically.

```java
DrawUtils.SHAPE.drawCurvedLine(Color.WHITE,
    new Vector2d(0, 0), new Vector2d(200, 0), new Vector2d(100, 100)
);

DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 2F,
    new Vector2d(0, 0), new Vector2d(100, 0),
    new Vector2d(100, 100), new Vector2d(200, 100)
);
```

## Polygons

### `drawPolygon`

```java
void drawPolygon(Color color, Vector2d... points)
```

Filled polygon with an arbitrary number of vertices, given as `Vector2d` varargs.

```java
DrawUtils.SHAPE.drawPolygon(Color.decode("#a78bfa"),
    new Vector2d(100, 100),
    new Vector2d(200, 100),
    new Vector2d(150, 180)
);
```

### `drawShape`

```java
void drawShape(int mode, Color color, Vector2d... points)
```

Lower-level: any GL primitive mode (`GL_TRIANGLES`, `GL_QUADS`, `GL_LINE_LOOP`, …) with arbitrary vertices. Every other shape method in this class eventually calls `drawShape`.

```java
DrawUtils.SHAPE.drawShape(GL11.GL_LINE_LOOP, Color.RED,
    new Vector2d(0, 0), new Vector2d(100, 0),
    new Vector2d(100, 100), new Vector2d(0, 100)
);
```

## Utilities

### `bindEmptyTexture`

```java
static void bindEmptyTexture()
```

Binds a lazily created 1×1 white texture so subsequent quad draws use vertex colors without sampling a stale texture. Called automatically by `drawShape` and `drawRawRect` — you rarely invoke it directly.

## See also

- `DrawUtils` — entry point for the four drawing facades.
- `Color` — color construction, gradients, transitions, binding.
- `RoundedNodeEffect`, `CircleNodeEffect` — node-level equivalents of the rounded/circle shaders.