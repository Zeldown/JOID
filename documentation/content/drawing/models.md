# Models

`DrawModel` renders any object implementing `IDrawableModel` into the current GL context. It handles lighting setup, back-face disabling, and a 180° Y rotation so that standard OBJ models face the camera.

```java
DrawUtils.MODEL.method(...);
// or
DrawModel.getInstance().method(...);
```

## `drawModel` — uniform scale

```java
void drawModel(double x, double y, double size, IDrawableModel model)
```

Translates to `(x, y, 0)` and applies `(size, size, size)` scale.

```java
DrawUtils.MODEL.drawModel(100, 100, 40D, crate);
```

## `drawModel` — per-axis scale

```java
void drawModel(double x, double y, double sizeX, double sizeY, double sizeZ, IDrawableModel model)
```

Same as above but each axis scales independently. Use it for stretched or compressed models.

```java
DrawUtils.MODEL.drawModel(100, 100, 40D, 60D, 40D, crate);
```

## `IDrawableModel`

Any model-loading library can provide a JOID-compatible implementation:

```java
public interface IDrawableModel {
    void render();
    double getWidth();
    double getHeight();
    double getDepth();
}
```

- `render()` — called inside `drawModel`, issues the actual vertex/index GL calls.
- `getWidth / getHeight / getDepth` — bounding-box dimensions, useful for layout or centering.

JOID doesn't ship a model loader — `ModelNode` consumes whatever `IDrawableModel` you pass it. A common pairing is `net.obj` for OBJ parsing, but anything producing `IDrawableModel` works.

## GL state inside `drawModel`

Every call sets up:

- `GL_CULL_FACE` disabled (both sides of every triangle visible).
- `GL_LIGHTING`, `GL_LIGHT0`, `GL_LIGHT1` enabled.
- `GL_COLOR_MATERIAL` enabled with `GL_FRONT_AND_BACK` / `GL_AMBIENT_AND_DIFFUSE`.
- `GL_SHADE_MODEL` set to `GL_FLAT`.
- Ambient light model: `(0.6, 0.6, 0.6, 1.0)`.

All of the above is restored after `render()` returns.

## See also

- `ModelNode` — node-level wrapper with hover and placement callbacks.
- `DrawUtils` — entry point for the four drawing facades.