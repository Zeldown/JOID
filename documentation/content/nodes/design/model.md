# ModelNode

Renders any `IDrawableModel` inside the UI. The node auto-scales the model so its width fits the node's width, then applies an optional user scale, yaw, and pitch around the node's center.

## Create

```java
ModelNode.create(x, y, width, height)
```

## API

```java
T model(IDrawableModel model)
T size(double size)                     // additional scale multiplier (default 1.0)
T rotationYaw(double degrees)           // rotation around Y axis
T rotationPitch(double degrees)         // rotation around X axis
T pipeLineLevel(double level)           // render depth hint (default -1 = auto from model diagonal)
```

There is no `rotation(x, y, z)`, no `rotationSpeed`, no `translate`, no callback. The node draws nothing when `model` is `null` or `size == 0`. Implementations of `IDrawableModel` live outside JOID — you provide the loader.

## `ModelViewerNode`

Interactive viewer sub-class. Same API as `ModelNode`, plus:

```java
ModelViewerNode.create(x, y, width, height)

T rotationYawRange(double min, double max)
T rotationPitchRange(double min, double max)
T sizeRange(double min, double max)            // defaults [0.1, 2.0]
T zoom(double zoom)                            // clamps to size range
```

Mouse press + drag rotates the model (yaw from X movement, pitch from Y movement). Mouse scroll zooms in/out via `value / 3000` per tick. `size`, `rotationYaw`, `rotationPitch` are animated toward their target values via `UI.lerpByFramerate`.

## Example — interactive preview

```java
ModelViewerNode.create(0, 0, 400, 400)
    .model(myModel)
    .size(1.2D)
    .rotationYawRange(-45D, 45D)
    .rotationPitchRange(-30D, 30D)
    .sizeRange(0.5D, 3D)
    .attach(parent);
```

## See also

- `DrawUtils.MODEL` — the drawing layer this node uses.