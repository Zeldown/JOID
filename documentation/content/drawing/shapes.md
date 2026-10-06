# Shapes

`DrawShape` (`dev.joid.lib.draw.shape`) draws rectangles, rounded rectangles, circles, borders, polygons, lines and curves immediately, through `DrawUtils.SHAPE`. Use it inside a draw hook (see [Drawing Overview](draw-utils.md)); for a shape that needs hover, effects or layout, use `RectNode` or `CircleNode` instead.

## Quick example

```java
@Override
public void draw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.decode("#111827"), 12F);
    DrawUtils.SHAPE.drawRect(super.getX(), super.getY() + 48D, super.getWidth(), 1D, Color.decode("#374151"));
    DrawUtils.SHAPE.drawCircle(super.getX() + 24D, super.getY() + 24D, Color.decode("#22C55E"), 6D);
}
```

Positions and sizes are UI units. Points are `javax.vecmath.Vector2d`. Every method accepts a gradient `Color` (see [Colors and Gradients](../styling/colors.md)): the gradient spans the bounding box of each primitive drawn, that is the whole rectangle, disk, polygon, line or dashed line, but each side of a border and each segment of a curve on its own.

## DrawShape reference

| Method | Description |
|---|---|
| `drawRect(double x, double y, double width, double height, Color color)` | Filled rectangle, snapped to the window pixels. |
| `drawRoundedRect(double x, double y, double width, double height, Color color, float radius)` | Rectangle with four rounded corners. |
| `drawRoundedRect(..., float radius, boolean roundedLeft, boolean roundedTop, boolean roundedRight, boolean roundedBottom)` | Rectangle rounded on the chosen sides only. |
| `drawCircle(double x, double y, Color color, double radius)` | Filled disk centered on `(x, y)`. |
| `drawBorder(double x, double y, double x2, double y2, Color color)` | Outline outside the box, 1 unit thick, corners left empty. |
| `drawBorder(double x, double y, double x2, double y2, Color color, double stroke)` | Same, `stroke` units thick. |
| `drawFilledBorder(double x, double y, double x2, double y2, Color color)` | Outline outside the box, 1 unit thick, corners filled. |
| `drawFilledBorder(double x, double y, double x2, double y2, Color color, double stroke)` | Same, `stroke` units thick. |
| `drawPolygon(Color color, Vector2d... points)` | Filled polygon. |
| `drawLine(Color color, Vector2d... points)` | Antialiased line through every point. |
| `drawLine(Color color, float stroke, Vector2d... points)` | Same, `stroke` window pixels wide. |
| `drawDashedLine(Color color, int pattern, float stroke, Vector2d... points)` | Dashed line, dashes and gaps of `pattern` units. |
| `drawCurvedLine(Color color, Vector2d start, Vector2d end, Vector2d control)` | Quadratic Bézier curve. |
| `drawCurvedLine(Color color, float stroke, Vector2d start, Vector2d end, Vector2d control)` | Same, `stroke` window pixels wide. |
| `drawCurvedLine(Color color, Vector2d start, Vector2d startControl, Vector2d end, Vector2d endControl)` | Cubic Bézier curve. |
| `drawCurvedLine(Color color, float stroke, Vector2d start, Vector2d startControl, Vector2d end, Vector2d endControl)` | Same, `stroke` window pixels wide. |
| `drawShape(DrawMode mode, Color color, Vector2d... points)` | Any primitive of `DrawMode`. |
| `drawRawRect(double x, double y, double width, double height)` | Rectangle with the current color and shader. |
| `DrawShape.getInstance()` | The instance behind `DrawUtils.SHAPE`. |

## Rectangles with drawRect

```java
DrawUtils.SHAPE.drawRect(40D, 40D, 200D, 100D, Color.WHITE);
DrawUtils.SHAPE.drawRect(40D, 140D, 200D, 1D, Color.WHITE.copyAlpha(0.2F));
```

The edges land on the nearest window pixels while the transform is axis-aligned. On an axis where the rectangle covers less than three pixels, it keeps a whole number of pixels centered on its exact position; below one pixel it is drawn one pixel thick with a proportional opacity. A hairline separator keeps the same weight at every position on the screen and never vanishes. See [Pixel alignment](draw-utils.md#pixel-alignment).

## Rounded rectangles with drawRoundedRect

```java
DrawUtils.SHAPE.drawRoundedRect(40D, 40D, 160D, 48D, Color.decode("#1F2937"), 10F);
DrawUtils.SHAPE.drawRoundedRect(40D, 100D, 160D, 48D, Color.decode("#1F2937"), 10F, true, true, true, false);
```

- `radius` is in UI units. The edges snap like `drawRect` and the corners are antialiased inside them.
- A corner is rounded when both of its sides are flagged: `roundedLeft` and `roundedTop` round the top-left corner. The second line above rounds the two top corners only, like a tab.
- The corners are carved by the rounded shader, the same as `RoundedNodeEffect` (see [RoundedNodeEffect](../styling/rounded.md)).

## Circles with drawCircle

```java
DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.WHITE, 8D);
```

`(x, y)` is the center and `radius` is in UI units. The disk is carved by the circle shader inside its exact square, antialiased, without pixel snapping.

## Borders with drawBorder and drawFilledBorder

```java
DrawUtils.SHAPE.drawBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), Color.decode("#374151"), 2D);
```

- The box goes from `(x, y)` to `(x2, y2)`: these are corners, not a size.
- The strokes are drawn outside the box: their inner edges land on the snapped box edges, so a border drawn around a `drawRect` of the same box touches it exactly.
- `stroke` is in UI units (1 by default), rounded to a whole number of pixels, the same on every side; below one pixel, the stroke is one pixel with a proportional opacity.
- `drawBorder` draws four separate sides and leaves the four outer corner squares empty; `drawFilledBorder` extends the top and bottom sides over the corners for a solid frame.

## Polygons with drawPolygon

```java
DrawUtils.SHAPE.drawPolygon(Color.decode("#A78BFA"), new Vector2d(100D, 100D), new Vector2d(200D, 100D), new Vector2d(150D, 180D));
```

The polygon is filled as a fan of triangles from its first point: use convex polygons, or polygons whose every point is visible from the first one. The points keep their exact positions.

## Lines with drawLine and drawDashedLine

```java
DrawUtils.SHAPE.drawLine(Color.WHITE, new Vector2d(0D, 0D), new Vector2d(100D, 50D), new Vector2d(200D, 0D));
DrawUtils.SHAPE.drawLine(Color.WHITE, 2F, new Vector2d(0D, 300D), new Vector2d(300D, 300D));
DrawUtils.SHAPE.drawDashedLine(Color.decode("#4ADE80"), 10, 2F, new Vector2d(0D, 400D), new Vector2d(300D, 400D));
```

- `drawLine` joins every consecutive pair of points with an antialiased line, then turns line smoothing off.
- `stroke` is a width in window pixels: it does not grow with the UI scale. Without `stroke`, the current line width of the render bridge is used. The `stroke` overloads set the line width back to `1F` afterward.
- `drawDashedLine` cuts each segment into dashes of `pattern` UI units separated by gaps of the same length, starting a new pattern at each point; the last dash of a segment is clipped at its end. It sets the line width back to `1F` and smoothing off afterward.
- Lines are antialiased by the line shader. While another shader is bound (a gradient color, a shader effect), they are drawn as plain lines.

## Curves with drawCurvedLine

```java
DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, new Vector2d(0D, 0D), new Vector2d(200D, 0D), new Vector2d(100D, 100D));
DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 2F, new Vector2d(0D, 0D), new Vector2d(100D, 0D), new Vector2d(100D, 100D), new Vector2d(200D, 100D));
```

- The quadratic overloads take `start, end, control`: the control point comes last.
- The cubic overloads take `start, startControl, end, endControl`.
- The curve is drawn as line segments, about one per UI unit of the length of its control polygon, and ends exactly on `end`.
- The points of a curve are computed with `Bezier.quadratic` and `Bezier.cubic` (`dev.joid.lib.utils.bezier`), which you can call for your own geometry (see [Utilities](../reference/utilities.md)).

## Any primitive with drawShape

```java
DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, Color.RED, new Vector2d(0D, 0D), new Vector2d(100D, 0D), new Vector2d(100D, 100D), new Vector2d(0D, 100D));
```

`DrawMode` (`dev.joid.lib.bridge.render.vertex`):

| Mode | Points |
|---|---|
| `TRIANGLES` | Every 3 points make a triangle. |
| `QUADS` | Every 4 points make a quad, drawn as two triangles. |
| `POLYGON` | One polygon, filled as a fan from the first point. |
| `LINES` | Every 2 points make a segment. |
| `LINE_STRIP` | Segments between consecutive points. |
| `LINE_LOOP` | `LINE_STRIP` closed back to the first point. |

`drawShape` does not change the line width nor the smoothing: set them through the render bridge for line modes (see [Render state basics](draw-utils.md#render-state-basics)). `drawPolygon` is `drawShape(DrawMode.POLYGON, ...)` and every filled shape goes through it.

## Rectangles with the current state using drawRawRect

`drawRawRect` draws a rectangle snapped like `drawRect`, without binding a color: it takes the current color of the render bridge and the current shader. Use it when a shader you bound paints the output.

```java
final Color tint = Color.decode("#3B82F6");
tint.bind();
try {
    DrawUtils.SHAPE.drawRawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight());
} finally {
    Color.reset();
}
```

## Render state after a shape

Each call pushes and pops the matrix, enables normal blending for its draw and leaves blending disabled and no texture bound. The shaders of `drawRoundedRect` and `drawCircle` are unbound afterward and the shader bound before the call is restored. When the rounded or circle shader is not available on the backend, these two methods draw nothing.

## See also

- [Drawing Overview](draw-utils.md)
- [Colors and Gradients](../styling/colors.md)
- [RectNode](../nodes/visual/rect.md)
- [CircleNode](../nodes/visual/circle.md)
- [Transformations and Framebuffers](transformations.md)