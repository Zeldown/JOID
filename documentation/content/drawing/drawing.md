# Drawing

`DrawUtils` draws shapes, text, resources and 3D models immediately through the render bridge. Use it in a layer or in the `draw` method of a custom node when you need free geometry; for anything that needs layout, hover or effects, compose nodes instead.

```java
ContainerNode
.create(100, 100, 400, 120)
.self(node -> node.layer((mouseX, mouseY) -> {
	DrawUtils.SHAPE.drawRoundedRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.decode("#DDDDDD"), 16F);
	DrawUtils.SHAPE.drawCircle(node.getX() + 60D, node.getY() + 60D, Color.decode("#999999"), 30D);
	DrawUtils.SHAPE.drawRect(node.getX() + 120D, node.getY() + 50D, 240D, 20D, Color.WHITE);
}))
.attach(this);
```

![A light gray rounded card with a gray disk on the left and a white bar on the right](../images/drawing-layer.png "Three DrawUtils calls in one layer")

Positions and sizes are units of the 1920×1080 virtual canvas (see [Canvas and Scaling](../concepts/canvas.md)). Inside a draw hook the matrix is translated to the parent of the node, so you draw at `node.getX()`, `node.getY()`. In the examples, `info` is a `TextInfo` built from a loaded font (see [Text and Fonts](../concepts/text.md)).

## Drawing in a custom node with draw

A custom node overrides `draw(double mouseX, double mouseY)`. This badge draws a pill and centers its text on it:

```java
@SuppressWarnings("unchecked")
public class BadgeNode extends Node {

	private Supplier<Text> text;

	protected BadgeNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		this.text = () -> null;
	}

	public static @NonNull BadgeNode create(final double x, final double y, final double width, final double height) {
		return new BadgeNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.decode("#999999"), 20F);
		final Text text = this.text.get();
		if (text != null) {
			DrawUtils.TEXT.drawText(super.getX() + super.dw(2D), super.getY() + super.dh(2D), text);
		}
	}

	public final <T extends BadgeNode> @NonNull T text(final @NonNull Text text) {
		return this.text(Signal.from(text));
	}

	public final <T extends BadgeNode> @NonNull T text(final @NonNull Supplier<Text> text) {
		this.text = text;
		return (T) this;
	}

}
```

```java
BadgeNode.create(100, 100, 160, 40).text(Text.create("NEW", this.info, Align.CENTER, Align.CENTER)).attach(this);
```

![A gray pill with the white word NEW centered on it](../images/drawing-badge.png "The text is centered on the point given to drawText")

The node contract (constructors, factories, callbacks) is on [Custom Nodes](../nodes/custom-nodes.md).

## Where you can draw

Draw only from the hooks JOID calls while it renders a frame.

| Hook | When it runs |
|---|---|
| `Node.draw(mouseX, mouseY)` | After the children with a negative z-index, before the others. |
| `layer(INodeLayer)`, `layer(int index, INodeLayer)` | After every child with a z-index of 0 or more. |
| `onDraw((node, mouseX, mouseY) -> ...)` | After the `draw` of the node. |
| `NodeEffect.pre` / `post` | Around the render of a node (see [Custom Effects](../styling/custom-effects.md)). |

![The frame of a UI from the background to the root nodes by z-index and the tooltips, and the render of a node from the effects pre to the effects post](../images/diagram-draw-order.png "Where each hook runs")

## Shapes with DrawShape

`DrawUtils.SHAPE` draws rectangles, rounded rectangles, borders, shadows, circles, polygons, lines and curves. Points are `javax.vecmath.Vector2d`.

```java
ContainerNode
.create(100, 100, 300, 160)
.self(node -> node.layer((mouseX, mouseY) -> {
	DrawUtils.SHAPE.drawRoundedRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.decode("#DDDDDD"), 12F);
	DrawUtils.SHAPE.drawRect(node.getX(), node.getY() + 60D, node.getWidth(), 1D, Color.decode("#999999"));
	DrawUtils.SHAPE.drawCircle(node.getX() + 30D, node.getY() + 30D, Color.decode("#999999"), 12D);
}))
.attach(this);
```

![A light gray rounded card with a small gray disk in its top-left corner and a thin gray separator below it](../images/shapes-quick.png "A rounded rectangle, a one-unit separator and a circle")

Every method accepts a gradient `Color`, spread over the bounding box of the primitive. `drawBorder` takes two corners and draws outside them; the rectangles take a size. Line and curve strokes are window pixels, border strokes are canvas units.

## Text with DrawText

`DrawUtils.TEXT` draws a string at a point with an alignment, or in a box where it is cut or wrapped (`TextMode`, see [Text and Fonts](../concepts/text.md)):

```java
ContainerNode
.create(0, 0, 0, 0)
.onDraw((node, mouseX, mouseY) -> {
	DrawUtils.TEXT.drawText(960D, 100D, "Paused", this.info, Align.CENTER, Align.START);
	DrawUtils.TEXT.drawText(760D, 160D, 400D, 200D, "Press Escape to go back to the game, or open the settings to change the controls.", this.info, Align.CENTER, Align.START, TextOverflow.NONE, TextMode.SPLIT);
})
.attach(this);
```

![The word Paused centered above a paragraph wrapped on three centered lines](../images/text-draw.png "A line centered on a point, then a paragraph wrapped in a box of 400 units")

The horizontal alignment decides which side of the text sits on the point: `START` starts there, `CENTER` is centered, `END` ends there. `getLines(width, text, info)` splits a string into the lines a box of that width holds.

## Resources with DrawResource

`DrawUtils.RESOURCE` draws a resource (image, SVG, animation or video frame) at its natural size, stretched over a box, or as a region of the source:

```java
private final Resource image = Resource.of("https://placehold.co/400x200/DDDDDD/999999.png");
private final Resource icon = Resource.of("https://placehold.co/100x100/DDDDDD/999999.png");

@Override
public void init() {
	ContainerNode.create(0, 0, 1920, 1080).layer((mouseX, mouseY) -> {
		DrawUtils.RESOURCE.drawResource(100D, 100D, this.icon);
		DrawUtils.RESOURCE.drawResource(260D, 100D, 200D, 100D, this.icon);
		DrawUtils.RESOURCE.drawResource(520D, 100D, 100D, 100D, 150D, 50D, 100D, 100D, this.image);
		Color.WHITE.copyAlpha(0.5F).bind(() -> DrawUtils.RESOURCE.drawResource(680D, 100D, 100D, 100D, this.icon), new Vector4f(680F, 100F, 780F, 200F), true);
	}).attach(this);
}
```

![The same placeholder drawn at its natural size, stretched to a wide box, a region cut from a larger image, and tinted at half opacity](../images/draw-resource-modes.png "Natural size, stretched, region, tinted")

The region `(u, v, regionWidth, regionHeight)` is in the units of `resource.getWidth()` and `getHeight()`, from the top-left corner. A resource with `textureCoords(...)` draws only that region (sprite sheets, see [Images and Media](../concepts/media.md)). The quad takes the current color: bind a `Color` around the call to tint or fade it. A resource not loaded yet draws a transparent placeholder.

## Render state with pushState and popState

The helpers restore the render state they change. When you change it yourself through `getState()`, save it with `pushState()` and restore it in a `finally` block:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
render.pushState();
try {
	render.getState().lineSmooth(true).lineWidth(6F);
	DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, Color.decode("#999999"), new Vector2d(100D, 100D), new Vector2d(300D, 100D), new Vector2d(200D, 220D));
} finally {
	render.popState();
}
```

The state holds the color, blending, depth, lines, stencil, viewport, shader, texture and framebuffer. The matrices are not part of it (see [Transformations, Framebuffers and Models](transformations.md)).

## Pixel alignment

While the transform is axis-aligned, rectangles, borders and resources snap their edges to window pixels, so edges stay sharp. A rectangle thinner than three pixels keeps a whole number of pixels, and never less than one: a 1-unit separator looks the same in every window. Text sits on the pixel grid, and a moving node moves by whole pixels. Circles, polygons and lines keep their exact geometry.

A drawing that moves on its own inside a still node rounds its motion with `quantize`, until the matrix is popped (`slide` is a [TweenAnimator](../concepts/animation.md)):

```java
private final TweenAnimator slide = TweenAnimator.create();

@Override
public void init() {
	ContainerNode
	.create(100, 100, 340, 40)
	.self(node -> node.layer((mouseX, mouseY) -> {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final double offset = this.slide.getValue() * 300D;
		render.getModelView().push();
		try {
			render.quantize(offset, 0D);
			DrawUtils.SHAPE.drawRect(node.getX() + offset, node.getY(), 40D, 40D, Color.WHITE);
		} finally {
			render.getModelView().pop();
		}
	}))
	.attach(this);
}
```

Under a rotation or a skew, shapes and images smooth their edges over one unit instead:

![Three shapes rotated by a few degrees, magnified: a gray card with a closed border, a triangle and an image, all with soft edges](../images/drawing-rotated-edges.png "Rotated shapes and images keep smooth edges")

## Reference

| Method | Description |
|---|---|
| `drawRect(x, y, width, height, color)` | Filled rectangle. |
| `drawRoundedRect(x, y, width, height, color, radius)`, `(..., radius, roundedLeft, roundedTop, roundedRight, roundedBottom)` | Rounded rectangle, optionally only on some sides. |
| `drawRoundedBorder(x, y, width, height, color, radius)`, `(..., radius, stroke)` | Rounded outline inside the box, stroke `1` by default. |
| `drawShadow(x, y, width, height, color, radius, blur)` | Soft shadow of a rounded box. Draw it before the shape. |
| `drawCircle(x, y, color, radius)` | Disk centered on `(x, y)`. |
| `drawBorder(x, y, x2, y2, color)`, `(..., stroke)` | Closed outline outside the corners, stroke `1` by default. |
| `drawPolygon(color, Vector2d... points)` | Filled polygon. |
| `drawLine(color, Vector2d... points)`, `drawLine(color, stroke, points...)` | Antialiased polyline. |
| `drawDashedLine(color, pattern, stroke, points...)` | Dashes and gaps of `pattern` units. |
| `drawCurvedLine(color, [stroke,] start, end, control)` | Quadratic Bézier curve. |
| `drawCurvedLine(color, [stroke,] start, startControl, end, endControl)` | Cubic Bézier curve. |
| `drawShape(DrawMode mode, color, points...)` | `LINES`, `LINE_STRIP`, `LINE_LOOP`, `TRIANGLES`, `QUADS` or `POLYGON`. |
| `drawText(x, y, Text text)`, `drawText(x, y, String text, info, horizontalAlign, verticalAlign)` | Text at a point. |
| `drawText(x, y, width, height, Text text, TextMode mode)`, `drawText(x, y, width, height, String text, info, horizontalAlign, verticalAlign, TextOverflow overflow, TextMode mode)` | Text in a box. |
| `drawResource(x, y, resource)` | Resource at its natural size. |
| `drawResource(x, y, width, height, resource)` | Resource stretched over the box. |
| `drawResource(x, y, width, height, u, v, regionWidth, regionHeight, resource)` | Region of the resource over the box. |
| `DrawUtils.MODEL.drawModel(x, y, size, model)` | 3D model (see [Transformations, Framebuffers and Models](transformations.md)). |
| `DrawUtils.RASTER.drawRaster(x, y, width, height, drawable)` | Content drawn by the host application (a game item, for example) at the pixel size of the box. |

## Good to know

- Keep resources and models in fields. Created in draw code, they are recreated every frame.
- Pop what you push (`popState()`, `getModelView().pop()`) in a `finally` block, or an exception shifts every following frame.
- Text unbinds the current shader: draw text outside your own shader binding.

## See also

- [Transformations, Framebuffers and Models](transformations.md)
- [Shaders](../shaders/shaders.md)
- [Custom Nodes](../nodes/custom-nodes.md)
- [Canvas and Scaling](../concepts/canvas.md)
- [Styling](../concepts/styling.md)