# ModelNode and ModelViewerNode

`ModelNode` (`dev.joid.lib.ui.node.impl.design.model`) draws a 3D model inside its box with a scale and a rotation. `ModelViewerNode` extends it into an interactive viewer: drag to rotate, wheel to zoom, with eased movement and optional limits. Use them for item previews, character viewers and 3D icons.

## Loading a model

Both nodes draw an `IDrawableModel` (`dev.joid.lib.draw.model.utils`). JOID ships `OBJModel` (`dev.joid.lib.obj`), which reads a Wavefront OBJ file with a texture:

```java
final Resource texture = Resource.of(MyUI.class.getResourceAsStream("/models/chest.png"));
final OBJModel model = OBJModel.load("chest", MyUI.class.getResourceAsStream("/models/chest.obj"), texture);
```

Load a model once and reuse it. `OBJModel`, its data and writing your own `IDrawableModel` are covered in [3D Models](../../drawing/models.md).

## Displaying a model with ModelNode

```java
ModelNode.create(100, 100, 300, 300).model(model).rotationYaw(30D).rotationPitch(15D).attach(this);
```

![A cube with colored faces, its three nearest faces visible](../../images/model-node.png "A cube model turned by 30 degrees of yaw and 15 degrees of pitch.")

### Fitting and scale

- The model is centered on the center of the node.
- It is scaled so that its width fills the node's width, multiplied by `size(...)`. The node's height is not used: a model taller than it is wide can extend above and below the node.
- A model dimension of `0` counts as `1`.
- `size(double)` is a scale factor (default `1D`, `0.5D` = half the width). At `0`, nothing is drawn. Do not confuse it with `size(width, height)` inherited from `Node`, which resizes the node.

### Rotation

`rotationYaw(double)` turns the model around the vertical axis and `rotationPitch(double)` tilts it around the horizontal axis, both in degrees and around the node's center. Both default to `0D`.

### Depth of the following nodes with pipeLineLevel

After drawing the model, the node raises the UI's render pipeline level: the UI pushes the root nodes drawn after it forward by that depth, so that they are drawn in front of the model's geometry instead of intersecting it. By default (`-1D`), the depth is the model's diagonal multiplied by its scale. `pipeLineLevel(double)` sets an explicit depth instead.

## Interactive viewer with ModelViewerNode

```java
ModelViewerNode
    .create(100, 100, 400, 400)
    .sizeRange(0.5D, 1.5D)
    .rotationPitchRange(-45D, 45D)
    .model(model)
    .attach(this);
```

![The cursor drags a cube to turn it, then the wheel zooms it out](../../images/model-viewer.gif "Dragging turns the model; the wheel changes its size. Both ease toward their targets.")

> NOTE: In a chain, a setter returns the type that declares it. Call the `ModelViewerNode` setters (`sizeRange`, `rotationYawRange`, `rotationPitchRange`, `zoom`) before `model(...)` and the other `ModelNode` setters, or assign the node to a `ModelViewerNode` variable first.

### Mouse controls

| Input | Effect |
| --- | --- |
| Press any mouse button over the node | Starts rotating. The press is consumed, so the nodes below do not receive it. |
| Move the mouse while pressed | Each UI unit moves the target yaw by 1/5 degree (right increases it) and the target pitch by 1/5 degree (up increases it). Both are clamped to their ranges. |
| Release the button, anywhere | Stops rotating. |
| Mouse wheel over the node | Changes the target size by `value / 3000` (a notch of `120` adds `0.04`), clamped to the size range. The wheel event is consumed. |

The displayed size and rotation ease toward their targets on every frame with `UI.lerpByFramerate`, so the speed does not depend on the frame rate. Without a model, the viewer draws nothing and ignores the drag.

### Setting the view from code

| Method | Behavior |
| --- | --- |
| `zoom(double zoom)` | Sets the target size, clamped to the size range. The size eases toward it. |
| `size(double size)` | Sets the size and its target at once (no easing, no clamping). |
| `rotationYaw(double rotationYaw)` | Sets the yaw and its target at once (no easing, no clamping). |
| `rotationPitch(double rotationPitch)` | Sets the pitch and its target at once (no easing, no clamping). |
| `sizeRange(double min, double max)` | Limits of `zoom(...)` and of the wheel. Default `0.1D` to `2D`. |
| `rotationYawRange(double min, double max)` | Limits of the yaw while dragging. Default unbounded. |
| `rotationPitchRange(double min, double max)` | Limits of the pitch while dragging. Default unbounded. |

## Reference

### ModelNode

| Method | Default | Description |
| --- | --- | --- |
| `ModelNode.create(double x, double y, double width, double height)` | | Creates an empty model node. |
| `model(IDrawableModel model)` | `null` | Model to draw. Nothing is drawn without one. |
| `size(double size)` | `1D` | Scale factor applied on top of the width fitting. |
| `rotationYaw(double rotationYaw)` | `0D` | Rotation around the vertical axis, in degrees. |
| `rotationPitch(double rotationPitch)` | `0D` | Rotation around the horizontal axis, in degrees. |
| `pipeLineLevel(double pipeLineLevel)` | `-1D` (automatic) | Depth added in front of the model for the following root nodes. |

Getters: `getModel()`, `getSize()`, `getRotationYaw()`, `getRotationPitch()`, `getPipeLineLevel()`.

### ModelViewerNode

`ModelViewerNode` has every `ModelNode` method, plus:

| Method | Default | Description |
| --- | --- | --- |
| `ModelViewerNode.create(double x, double y, double width, double height)` | | Creates an empty viewer. |
| `zoom(double zoom)` | | Eased, clamped target size. |
| `sizeRange(double min, double max)` | `0.1D`, `2D` | Size limits. |
| `rotationYawRange(double min, double max)` | `-Double.MAX_VALUE`, `Double.MAX_VALUE` | Yaw limits while dragging. |
| `rotationPitchRange(double min, double max)` | `-Double.MAX_VALUE`, `Double.MAX_VALUE` | Pitch limits while dragging. |

| Getter | Description |
| --- | --- |
| `getTargetSize()`, `getTargetRotationYaw()`, `getTargetRotationPitch()` | Values the view eases toward. |
| `getMinSize()`, `getMaxSize()` | Size range. |
| `getMinRotationYaw()`, `getMaxRotationYaw()` | Yaw range. |
| `getMinRotationPitch()`, `getMaxRotationPitch()` | Pitch range. |
| `isDragged()` | `true` while the mouse rotates the model. |
| `getDraggedMouseX()`, `getDraggedMouseY()` | Mouse position of the last drag step. |

All setters return the node itself, typed by the generic return of the fluent API.

## See also

- [3D Models](../../drawing/models.md)
- [Transformations and Framebuffers](../../drawing/transformations.md)
- [Mouse and Keyboard](../../interactions/mouse-and-keyboard.md)
- [The UI Class](../../ui/ui-class.md) for `lerpByFramerate`
- [Node Fundamentals](../node-fundamentals.md)