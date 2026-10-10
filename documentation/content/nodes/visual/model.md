# ModelNode

`ModelNode` draws a 3D model inside its box with a scale and a rotation. `ModelViewerNode` extends it into an interactive viewer: drag to rotate, wheel to zoom. Use them for item previews, character viewers and 3D icons.

```java
private final ObjModel model = ObjModel.load("box", new File("models/box.obj"), Resource.of(new File("models/box.png")));

@Override
public void init() {
	ModelNode.create(100, 100, 300, 300).model(this.model).rotationYaw(30D).rotationPitch(15D).attach(this);
}
```

![The demo teapot, smoothly lit, turned to show its side and its base](../../images/model-node.png "The demo model turned by 30 degrees of yaw and 15 degrees of pitch.")

`ObjModel.load(name, handle, texture)` reads a Wavefront OBJ file with its texture; load a model once and reuse it. The model is centered on the node and scaled so that its width fills the node's width, multiplied by `size(...)`.

## Rotation with rotationYaw and rotationPitch

`rotationYaw(degrees)` turns the model around the vertical axis and `rotationPitch(degrees)` tilts it around the horizontal axis. With yaw `0D` you see its +Z face. A turntable reads a `TweenAnimator` on every frame:

```java
final TweenAnimator spin = TweenAnimator.create(0F).sequence(4000F, 1F);
spin.getTimeline().repeat(Tween.INFINITY, 0F);
spin.start();

ModelNode.create(100, 100, 300, 300).model(this.model).rotationYaw(() -> spin.getValue() * 360D).animate(spin).attach(this);
```

![The demo model spinning on itself](../../images/model-spin.gif "rotationYaw read from the animator every frame.")

## Interactive viewer with ModelViewerNode

Dragging with the left button rotates the model; the wheel zooms. Both ease toward their targets and stay inside the ranges you set.

```java
ModelViewerNode
.create(100, 100, 400, 400)
.sizeRange(0.5D, 1.5D)
.rotationPitchRange(-45D, 45D)
.model(this.model)
.attach(this);
```

![The cursor drags the teapot to turn it, then the wheel zooms it out](../../images/model-viewer.gif "Dragging turns the model; the wheel changes its size.")

`zoom(double)` sets an eased, clamped target size from code; `size(...)`, `rotationYaw(...)` and `rotationPitch(...)` set the view at once.

## Reference

| Method | Default | Description |
|---|---|---|
| `ModelNode.create(x, y, width, height)` | | Creates an empty model node. |
| `model(IDrawableModel)`, `model(Supplier)` | `null` | Model to draw. |
| `size(double)`, `size(Supplier<Double>)` | `1D` | Scale factor on top of the width fitting. |
| `rotationYaw(double)`, `rotationYaw(Supplier<Double>)` | `0D` | Rotation around the vertical axis, in degrees. |
| `rotationPitch(double)`, `rotationPitch(Supplier<Double>)` | `0D` | Rotation around the horizontal axis, in degrees. |
| `ModelViewerNode.create(x, y, width, height)` | | Creates an empty viewer. |
| `zoom(double)` | | Eased target size, clamped to the size range. |
| `sizeRange(min, max)` | `0.1D`, `2D` | Limits of the zoom and of the wheel. |
| `rotationYawRange(min, max)`, `rotationPitchRange(min, max)` | unbounded | Rotation limits while dragging. |
| `isDragged()` | | `true` while the mouse rotates the model. |

## Good to know

- Call the `ModelViewerNode` setters (`sizeRange`, `zoom`, the ranges) before `model(...)` in a chain: a `ModelNode` setter returns a `ModelNode`.
- Only the width is fitted: a model taller than wide extends above and below its node.
- The viewer consumes the left press and the wheel over it: a scrolling parent does not scroll under it.

## See also

- Next: [ProgressNode](progress.md)
- [Transformations, Framebuffers and Models](../../drawing/transformations.md)
- [Animation](../../concepts/animation.md)
- [Input](../../concepts/input.md)