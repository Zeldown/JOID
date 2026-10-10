# Transformations, Framebuffers and Models

`Transformation` moves, rotates and scales what you draw, `FrameBuffer` draws into a texture you draw later, `Tessellator` builds your own geometry, and `DrawModel` draws 3D models. Use them in draw hooks (see [Drawing](drawing.md)) when the helpers are not enough.

```java
ContainerNode
.create(100, 100, 200, 200)
.self(node -> node.layer((mouseX, mouseY) -> {
	Transformation
	.create()
	.rotate(30D, Rotation.ROLL, Vector.create(node.getX() + node.dw(2D), node.getY() + node.dh(2D)))
	.apply(() -> DrawUtils.SHAPE.drawRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.decode("#DDDDDD")));
}))
.attach(this);
```

![A light gray square turned 30 degrees clockwise around its center, with smooth edges](../images/transform-draw-rotate.png "The square rotates around its center")

To transform a node and its children instead, use `TransformNodeEffect` (see [Effects](../styling/effects.md)).

## Building a Transformation

A `Transformation` is a list of operations: `translate(Vector)`, `rotate(angle, Rotation, pivot)` and `scale(Scale, pivot)`. They apply in the order you add them, each in the space the previous ones produced. `apply(Drawing)` pushes the matrix, applies the operations, runs the drawing and pops the matrix, even when the drawing throws.

Translations and pivots are canvas units, angles are degrees. The pivot is the point that stays in place:

```java
Transformation
.create()
.rotate(20D, Rotation.ROLL, Vector.create(200D, 160D))
.apply(() -> DrawUtils.SHAPE.drawRect(100D, 100D, 200D, 120D, Color.decode("#999999")));

Transformation
.create()
.rotate(20D, Rotation.ROLL, Vector.create(400D, 100D))
.apply(() -> DrawUtils.SHAPE.drawRect(400D, 100D, 200D, 120D, Color.decode("#999999")));
```

![Two gray rectangles over their unrotated outlines: the first turned around its center, the second around its top-left corner](../images/transform-pivot.png "Pivot on the center, then pivot on the top-left corner")

`Rotation.ROLL` turns in the screen plane (clockwise for a positive angle), `YAW` and `PITCH` around the vertical and horizontal axes. `Scale.create(width, height, depth)`: `1` keeps the size, `-1` mirrors.

## Following a value with suppliers

`Vector`, `Scale`, `Rotation` and `RotateTransformOperation` also take suppliers, read at every `apply`. Build the transformation once in a field and it follows your state:

```java
public class SpinNode extends Node {

	private final Transformation spin;

	private double angle;

	protected SpinNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		this.spin = Transformation.create(new RotateTransformOperation(() -> this.angle, Rotation.ROLL, Vector.create(() -> super.getX() + super.dw(2D), () -> super.getY() + super.dh(2D))));
	}

	public static @NonNull SpinNode create(final double x, final double y, final double width, final double height) {
		return new SpinNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		this.angle += 90D * super.getUi().getFrameTime() / 1000D;
		this.spin.apply(() -> DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.decode("#DDDDDD")));
	}

}
```

`getFrameTime()` is the duration of the last frame in milliseconds, so the square turns 90 degrees per second.

## Matrix stacks of IRenderBridge

`Transformation` is a shortcut for the two `MatrixStack`s of the render bridge, `getModelView()` and `getProjection()`, which you can use directly:

```java
ContainerNode
.create(100, 100, 200, 200)
.self(node -> node.layer((mouseX, mouseY) -> {
	final IRenderBridge render = BridgeHandler.RENDER.get();
	render.getModelView().push();
	try {
		render.getModelView().translate(node.getX() + node.dw(2D), node.getY() + node.dh(2D), 0D);
		render.getModelView().scale(1.5D, 1.5D, 1D);
		render.getModelView().translate(-(node.getX() + node.dw(2D)), -(node.getY() + node.dh(2D)), 0D);
		DrawUtils.SHAPE.drawRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.decode("#DDDDDD"));
	} finally {
		render.getModelView().pop();
	}
}))
.attach(this);
```

The last call applies first to the vertices, as in OpenGL. X goes right, Y goes down. `pushState()` does not save the matrices.

## Drawing into a FrameBuffer

A `FrameBuffer` is an offscreen texture: draw into it once, then draw it on screen as many times as needed.

```java
public class StampNode extends Node {

	private FrameBuffer buffer;

	protected StampNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull StampNode create(final double x, final double y, final double width, final double height) {
		return new StampNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.buffer == null) {
			this.buffer = FrameBuffer.create(256, 256, TextureFilter.LINEAR);
			final IRenderBridge render = BridgeHandler.RENDER.get();
			render.getProjection().push();
			render.getModelView().push();
			try {
				this.buffer.fill(() -> {
					render.getState().viewport(0, 0, 256, 256);
					render.clearColor(0F, 0F, 0F, 0F);
					render.getProjection().ortho(0D, 256D, 256D, 0D, -1000D, 1000D);
					render.getModelView().identity();
					DrawUtils.SHAPE.drawCircle(128D, 128D, Color.decode("#999999"), 120D);
					DrawUtils.SHAPE.drawCircle(128D, 128D, Color.decode("#DDDDDD"), 60D);
				});
			} finally {
				render.getModelView().pop();
				render.getProjection().pop();
			}
		}

		this.buffer.draw(super.getX(), super.getY(), 128D, 128D);
		this.buffer.draw(super.getX() + 160D, super.getY() + 32D, 64D, 64D);
	}

	@Override
	public void detach() {
		if (this.buffer != null) {
			this.buffer.delete();
			this.buffer = null;
		}
	}

}
```

![The same pair of concentric gray disks drawn twice from one framebuffer, at two sizes](../images/framebuffer-draw.png "Filled once, drawn twice")

`fill(Runnable)` binds the buffer, runs the drawing and unbinds it, even when the drawing throws. Inside, set the viewport to the buffer size (window pixels), clear it and give it a projection; save the matrices around `fill`. Fill the buffer again only when its content changes, and `delete()` it when you are done.

`bind()` saves the render state and binds the buffer, `unbind()` restores it, so framebuffers nest. Prefer `fill`, which pairs them for you.

![Two nested bind and unbind pairs: each bind saves the state and binds its buffer, each unbind restores the state saved by its bind](../images/diagram-framebuffer-bind.png "bind and unbind restore the target bound before")

## Building geometry with Tessellator

`Tessellator` collects vertices and sends them to the render bridge in one draw call. Every drawing helper uses it.

```java
ContainerNode
.create(0, 0, 400, 300)
.layer((mouseX, mouseY) -> {
	final Tessellator tessellator = Tessellator.inst();
	tessellator.start(DrawMode.TRIANGLES);
	tessellator.setColor(255, 0, 0, 255);
	tessellator.addVertex(200D, 100D, 0D);
	tessellator.setColor(0, 255, 0, 255);
	tessellator.addVertex(300D, 260D, 0D);
	tessellator.setColor(0, 0, 255, 255);
	tessellator.addVertex(100D, 260D, 0D);
	tessellator.draw();
})
.attach(this);
```

![A triangle whose corners are red, green and blue, the colors blending across its surface](../images/tessellator-triangle.png "One color per vertex, interpolated")

Call `setColor`, `setTextureUV` and `setNormal` after `start`, before the vertices they apply to. The tessellator does not touch the render state: set blending, texture and shader yourself, inside `pushState()` / `popState()`.

## 3D models with DrawModel

`DrawUtils.MODEL` draws a 3D model: anything that implements `IDrawableModel`. JOID ships `ObjModel`, which reads a Wavefront `.obj` file drawn with one texture.

```java
private final ObjModel model = ObjModel.load("model.obj", ObjModel.class.getResourceAsStream("/assets/models/model.obj"), Resource.of(ObjModel.class.getResourceAsStream("/assets/models/texture.png")));

@Override
public void init() {
	ContainerNode.create(0, 0, 1920, 1080).layer((mouseX, mouseY) -> DrawUtils.MODEL.drawModel(300D, 200D, 120D, this.model)).attach(this);
}
```

![The demo teapot with its neutral ornamented texture, seen from the side, lit from the viewer](../images/models-demo.png "The model is centered on (300, 200), one model unit is 120 canvas units")

The origin of the model lands on `(x, y)` and one model unit becomes `size` canvas units. Models follow the OBJ convention of modeling tools: +X right, +Y up, +Z toward the viewer.

![The origin of the model sits on (x, y); +X goes right, +Y goes up and +Z comes toward the viewer](../images/diagram-model-axes.png "Model axes on screen")

Light comes from the viewer; faces with vertex normals look smooth. Each `drawModel` clears the depth buffer before and after, so a node drawn later covers the model where they overlap. To show a model as a node rotated with the mouse, use [ModelNode](../nodes/visual/model.md).

## Reference

| Method | Description |
|---|---|
| `Transformation.create()`, `create(ITransformOperation operation)` | Empty transformation, or one with an operation. |
| `translate(Vector vector)` | Appends a translation, rounded to whole pixels while the transform is axis-aligned. |
| `rotate(double angle, Rotation rotation, Vector pivot)` | Appends a rotation of `angle` degrees around `pivot`. |
| `scale(Scale scale, Vector pivot)` | Appends a scale around `pivot`. |
| `apply(Drawing drawing)` | Pushes the matrix, applies the operations, draws, pops. |
| `Vector.create(x, y)`, `Scale.create(width, height, depth)`, `Rotation.create(yaw, pitch, roll)` | Values or suppliers. |
| `MatrixStack.push()`, `pop()`, `identity()` | Save, restore, reset the matrix. |
| `MatrixStack.translate(x, y, z)`, `scale(x, y, z)`, `rotate(angle, x, y, z)`, `ortho(left, right, bottom, top, near, far)` | Transform the matrix. |
| `FrameBuffer.create(int width, int height, TextureFilter filter)` | Framebuffer of that size in window pixels. |
| `fill(Runnable drawing)` | Binds, draws, unbinds. |
| `bind()`, `unbind()` | Pair by hand; `bind()` on a bound buffer throws. |
| `draw(x, y, width, height)` | Draws the buffer; throws before the first `fill`. |
| `delete()` | Releases the GPU memory. |
| `Tessellator.inst()` | The shared tessellator. |
| `start(DrawMode mode)`, `draw()` | Begin, then send the vertices. |
| `addVertex(x, y, z)`, `addVertexWithUV(x, y, z, u, v)` | Add a vertex. |
| `setColor(r, g, b, a)`, `setTextureUV(u, v)`, `setNormal(x, y, z)` | Apply to the next vertices. |
| `ObjModel.load(String name, Object handle, Resource texture)` | Loads an `.obj` file. |
| `drawModel(x, y, size, model)`, `drawModel(x, y, sizeX, sizeY, sizeZ, model)` | Draws a model, scaled per axis. |

## Good to know

- Pop the matrix in a `finally` block: an exception between `push()` and `pop()` shifts every following frame.
- A framebuffer drawn before its first `fill` throws a `RuntimeException`.

## See also

- [Drawing](drawing.md)
- [Shaders](../shaders/shaders.md)
- [ModelNode](../nodes/visual/model.md)
- [Effects](../styling/effects.md)