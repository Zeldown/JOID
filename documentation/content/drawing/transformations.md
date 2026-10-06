# Transformations and Framebuffers

Below the drawing classes, JOID exposes the tools they are built on: `Transformation` moves, rotates and scales what you draw, the matrix methods of the render bridge do the same by hand, `FrameBuffer` draws into a texture you draw later, and `Tessellator` builds your own geometry. Use them in draw hooks (see [Drawing Overview](draw-utils.md)) when the drawing classes are not enough.

## Quick example

```java
@Override
public void draw(final double mouseX, final double mouseY) {
    final Vector center = Vector.create(super.getX() + super.dw(2), super.getY() + super.dh(2));
    Transformation.create()
        .rotate(45D, Rotation.ROLL, center)
        .apply(() -> DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE));
}
```

The square turns 45° clockwise around its center. `Transformation` is in `dev.joid.lib.render.transform`, `Rotation`, `Scale` and `Vector` in `dev.joid.lib.render.modifier`. To transform a node and its children instead, use `TransformNodeEffect` (see [TransformNodeEffect](../styling/transform.md)).

## Transformation

A `Transformation` is a list of operations applied to the matrix of the render bridge.

| Method | Description |
|---|---|
| `Transformation.create()` | Empty transformation. |
| `Transformation.create(TransformOperation operation)` | Transformation with one operation. |
| `add(TransformOperation operation)` | Appends an operation. |
| `translate(Vector vector)` | Appends a translation. |
| `rotate(double angle, Rotation rotation, Vector pivot)` | Appends a rotation of `angle` degrees around the axis of `rotation`, through `pivot`. |
| `scale(Scale scale, Vector pivot)` | Appends a scale around `pivot`. |
| `apply()` | Pushes the matrix, then applies every operation. |
| `reset()` | Pops the matrix: call it after `apply()`. |
| `apply(Drawing drawing)` | `apply()`, runs the drawing, then `reset()`, even when the drawing throws. |
| `clear()` | Removes every operation. |
| `getOperations()` | The list of operations. |

`Drawing` (`dev.joid.lib.render.context`) is a functional interface with a single `draw()` method, so a lambda fits.

- The operations apply in the order you add them, each one in the space the previous ones produced: a translation followed by a scale scales around the translated origin.
- Their values are read at every `apply`: build the transformation once with suppliers and it follows your state.
- A translation is rounded to whole window pixels while the transform is axis-aligned, like any moving node (see [Pixel alignment](draw-utils.md#pixel-alignment)).

### Transform operations

The operations are in `dev.joid.lib.render.transform.operation` and implement `TransformOperation`, whose `transform()` applies them to the current matrix.

| Operation | Constructor | Effect |
|---|---|---|
| `TranslateOperation` | `new TranslateOperation(Vector vector)` | Translates by the vector, then rounds the translation to whole pixels. `getVector()` returns it. |
| `RotateOperation` | `new RotateOperation(double angle, Rotation rotation, Vector pivot)`, `new RotateOperation(Supplier<Double> angle, Rotation rotation, Vector pivot)` | Rotates around the pivot; the supplier is read at every `transform()`. |
| `ScaleOperation` | `new ScaleOperation(Scale scale, Vector pivot)` | Scales around the pivot, which stays in place. |

```java
private double angle;

private final Transformation spin = Transformation.create(new RotateOperation(
    () -> this.angle,
    Rotation.ROLL,
    Vector.create(() -> super.getX() + super.dw(2), () -> super.getY() + super.dh(2))
));

@Override
public void draw(final double mouseX, final double mouseY) {
    this.angle += 90D * super.getUi().getFrameTime() / 1000D;
    this.spin.apply(() -> DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE));
}
```

`getFrameTime()` is the duration of the last frame in milliseconds (see [The UI Class](../ui/ui-class.md)).

### Rotation

| Member | Description |
|---|---|
| `Rotation.ROLL` | Around the Z axis, in the plane of the screen. A positive angle turns clockwise on screen. |
| `Rotation.YAW` | Around the vertical (Y) axis: 180° mirrors horizontally. |
| `Rotation.PITCH` | Around the horizontal (X) axis: 180° mirrors vertically. |
| `Rotation.create(double yaw, double pitch, double roll)` | Custom axis: `yaw` is its Y component, `pitch` its X component, `roll` its Z component. |
| `Rotation.create(Supplier<Double> yaw, Supplier<Double> pitch, Supplier<Double> roll)` | Same, read at every use. |
| `Rotation.create()` | No axis: a rotation around it does nothing. |
| `getRawX()`, `getRawY()`, `getRawZ()` | Components of the axis. |

### Scale

| Member | Description |
|---|---|
| `Scale.create()` | `1` on every axis. |
| `Scale.create(double width, double height, double depth)` | Factor per axis: `1` keeps the size, `2` doubles it, `-1` mirrors. |
| `Scale.create(Supplier<Double> width, Supplier<Double> height, Supplier<Double> depth)` | Same, read at every use. |
| `Scale.WIDTH(...)`, `Scale.HEIGHT(...)`, `Scale.DEPTH(...)` | One axis from a value or a supplier, `1` on the others. |
| `width(...)`, `height(...)`, `depth(...)` | Replace one factor with a value or a supplier. |
| `getRawX()`, `getRawY()`, `getRawZ()` | Current factors. |

### Vector

| Member | Description |
|---|---|
| `Vector.create()` | `(0, 0, 0)`. |
| `Vector.create(double x, double y)`, `Vector.create(double x, double y, double z)` | Fixed coordinates, `z` = 0 by default. |
| `Vector.create(Supplier<Double> x, Supplier<Double> y)`, `Vector.create(Supplier<Double> x, Supplier<Double> y, Supplier<Double> z)` | Coordinates read at every use. |
| `Vector.X(...)`, `Vector.Y(...)`, `Vector.Z(...)` | One axis from a value or a supplier, 0 on the others. |
| `x(...)`, `y(...)`, `z(...)` | Replace one coordinate with a value or a supplier. |
| `add(double x, double y, double z)` | Adds an offset to the current coordinates. The suppliers are read once: the vector stops following them. |
| `add(Supplier<Double> x, Supplier<Double> y, Supplier<Double> z)` | Adds a supplied offset; the vector keeps following both. |
| `getX()`, `getY()`, `getZ()` | Current coordinates. |

## The matrix stack of the render bridge

`Transformation` is a shortcut for the matrix methods of `IRenderBridge` (`BridgeHandler.RENDER.get()`), which you can call directly:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
render.pushMatrix();
try {
    render.translate(super.getX() + super.dw(2), super.getY() + super.dh(2), 0D);
    render.scale(1.5D, 1.5D, 1D);
    render.translate(-(super.getX() + super.dw(2)), -(super.getY() + super.dh(2)), 0D);
    DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE);
} finally {
    render.popMatrix();
}
```

| Method | Description |
|---|---|
| `pushMatrix()`, `popMatrix()` | Save and restore the model-view matrix. Pop in a `finally` block, so an exception does not shift every following frame. |
| `translate(double x, double y, double z)` | Translates. |
| `rotate(double angle, double x, double y, double z)` | Rotates `angle` degrees around the axis `(x, y, z)`; an axis of length 0 is ignored. |
| `scale(double x, double y, double z)` | Scales. |
| `quantize(double motionX, double motionY)` | Rounds a motion to whole pixels (see [Pixel alignment](draw-utils.md#moving-a-drawing-inside-its-node-with-quantize)). |
| `loadIdentity()` | Resets the model-view matrix. |
| `pushProjection()`, `popProjection()` | Save and restore the projection matrix. |
| `ortho(double left, double right, double bottom, double top, double near, double far)` | Replaces the projection with an orthographic one. |

Each call multiplies the current matrix on the right: the last call is the first one applied to the vertices, as in OpenGL. In the UI canvas, X goes right and Y goes down.

### MatrixStack

A render bridge built on `RenderBridge` keeps its matrices in two `MatrixStack`s (`dev.joid.lib.bridge.render.matrix`), `getModelView()` and `getProjection()`. You need them when you write a backend (see [Writing a Backend](../integration/writing-a-backend.md)).

| Method | Description |
|---|---|
| `push()`, `pop()` | Save and restore the matrix; `pop()` on an empty stack throws `NoSuchElementException`. |
| `identity()`, `translate(...)`, `scale(...)`, `rotate(...)`, `ortho(...)` | As on the render bridge. |
| `multiply(float[] other)` | Multiplies on the right by a column-major 4×4 matrix. |
| `getMatrix()` | Current column-major 4×4 matrix. |
| `getNormalMatrix()` | Inverse transpose of its 3×3 part, column-major, for normals. |

## FrameBuffer

A `FrameBuffer` (`dev.joid.lib.render.framebuffer`) is an offscreen texture you draw into once and draw on screen as many times as needed.

```java
final int size = 512;
final FrameBuffer buffer = FrameBuffer.create(size, size, TextureFilter.LINEAR);

final IRenderBridge render = BridgeHandler.RENDER.get();
render.pushState();
render.pushProjection();
render.pushMatrix();
try {
    buffer.fill(() -> {
        render.viewport(0, 0, size, size);
        render.clear(0F, 0F, 0F, 0F);
        render.ortho(0D, size, size, 0D, -1000D, 1000D);
        render.loadIdentity();
        DrawUtils.SHAPE.drawCircle(256D, 256D, Color.WHITE, 200D);
    });
} finally {
    render.popMatrix();
    render.popProjection();
    render.popState();
}

buffer.draw(40D, 40D, 128D, 128D);
```

`fill` only binds the buffer: set the viewport to the buffer size, clear it and give it a projection yourself, and save the state around it as above, since the viewport and the bound target are part of the render state. Create a buffer once, fill it again only when its content changes, and `delete()` it when you are done with it.

| Method | Description |
|---|---|
| `FrameBuffer.create(int width, int height, TextureFilter filter)` | New buffer of this size in pixels, sampled with `NEAREST` or `LINEAR` filtering when drawn. Create it on the render thread. |
| `fill(Runnable runnable)` | Binds the buffer, runs the drawing, then binds the default target back, even when the drawing throws. The buffer counts as filled once the drawing completes. |
| `bind()`, `unbind()` | Bind the buffer, or the default target (not the target bound before). |
| `draw(double x, double y, double width, double height)` | Draws the texture on a quad, with normal blending and the current color; throws a `RuntimeException` before the first `fill`. |
| `getWidth()`, `getHeight()` | Size in pixels. |
| `isFilled()`, `getFilter()`, `getHandle()` | State, filter and the `IFrameBuffer` of the backend. |
| `delete()` | Releases the GPU buffer. |

The shader pipeline renders effects such as blur through framebuffers (see [Shader Pipeline](../shaders/pipeline.md)).

## Building geometry with Tessellator

`Tessellator` (`dev.joid.lib.render.tessellator`) collects vertices and sends them to the render bridge in one draw call. Every drawing class uses it.

```java
final Tessellator tessellator = Tessellator.inst();
tessellator.start(DrawMode.TRIANGLES);
tessellator.setColor(255, 0, 0, 255);
tessellator.addVertex(100D, 100D, 0D);
tessellator.setColor(0, 255, 0, 255);
tessellator.addVertex(200D, 100D, 0D);
tessellator.setColor(0, 0, 255, 255);
tessellator.addVertex(150D, 180D, 0D);
tessellator.draw();
```

| Method | Description |
|---|---|
| `Tessellator.inst()` | Shared instance. |
| `copy()` | New, independent tessellator. |
| `start(DrawMode mode)` | Starts a draw; throws `IllegalStateException` when one is already started. Forgets the color, texture coordinates and normal of the previous draw. |
| `quads()` | `start(DrawMode.QUADS)`. |
| `addVertex(double x, double y, double z)` | Adds a vertex with the current color, texture coordinates and normal. |
| `addVertexWithUV(double x, double y, double z, double u, double v)` | `setTextureUV(u, v)`, then `addVertex`. |
| `setColor(...)` | Color of the next vertices: `(int rgb)`, `(int rgb, int alpha)`, `(int r, int g, int b)`, `(int r, int g, int b, int a)` and `(byte r, byte g, byte b)` from 0 to 255 (clamped), `(float r, float g, float b)` and `(float r, float g, float b, float a)` from 0 to 1. |
| `disableColor()` | Ignores `setColor` until the next `start`. |
| `setTextureUV(double u, double v)` | Texture coordinates of the next vertices. |
| `setNormal(float x, float y, float z)` | Normal of the next vertices, components from -1 to 1. |
| `translate(float x, float y, float z)` | Adds an offset to every following vertex. |
| `draw()` | Sends the vertices; throws `IllegalStateException` when no draw was started. |
| `isDrawing()`, `getDrawMode()`, `getVertexCount()` | State of the draw in progress. |

- Call `setColor`, `setTextureUV` and `setNormal` after `start`, before the vertices they apply to. Vertices without color take the current color of the render bridge, and vertices without texture coordinates use `(0, 0)`.
- `QUADS` and `POLYGON` are turned into triangles (a polygon as a fan from its first vertex), `LINE_STRIP` and `LINE_LOOP` into segments. While line smoothing is on and no shader is bound, lines are drawn as antialiased quads `getLineWidth()` window pixels wide.
- The tessellator does not touch the render state: set the blending, texture and shader yourself, inside `pushState()`/`popState()`.
- The `translate` offsets accumulate and are never reset: use them on a `copy()`, not on the shared instance.
- To draw while the shared instance is started (inside your own `render()` of a model, for example), use a `copy()`.

## See also

- [Drawing Overview](draw-utils.md)
- [TransformNodeEffect](../styling/transform.md)
- [3D Models](models.md)
- [Shader Pipeline](../shaders/pipeline.md)
- [Bridges](../integration/bridges.md)