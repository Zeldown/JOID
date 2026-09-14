# Models

`DrawModel` renders any object implementing `IDrawableModel` through the render bridge. It handles lighting setup, back-face disabling, and a 180° Y rotation so that standard OBJ models face the camera.

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

- `render()` — called inside `drawModel`, issues the model draw calls through the render bridge.
- `getWidth / getHeight / getDepth` — bounding-box dimensions, useful for layout or centering.

JOID doesn't ship a model loader — `ModelNode` consumes whatever `IDrawableModel` you pass it. A common pairing is `net.obj` for OBJ parsing, but anything producing `IDrawableModel` works.

## Render state inside `drawModel`

Every call sets up through the render bridge:

- `cull(false)` — both sides of every triangle visible.
- `lighting(true)` — two directional lights, color material on ambient and diffuse, flat shading.
- Ambient light: `(0.6, 0.6, 0.6, 1.0)`.

Lighting is disabled and the matrix popped after `render()` returns.

## See also

- `ModelNode` — node-level wrapper with hover and placement callbacks.
- `DrawUtils` — entry point for the four drawing facades.