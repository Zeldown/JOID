# Drawing Overview

Every node draws itself with immediate calls through the render bridge, and you use the same calls in your own draw hooks: `DrawUtils` (`dev.joid.lib.draw`) gathers shapes, text, resources and 3D models. Draw by hand when you write a custom node, an overlay or an effect; for anything that needs layout, hover, dragging or effects, compose nodes instead.

## Drawing in a draw hook

```java
@SuppressWarnings("unchecked")
public class BadgeNode extends Node {

    private Text label;

    protected BadgeNode(final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
    }

    public static BadgeNode create(final double x, final double y, final double width, final double height) {
        return new BadgeNode(x, y, width, height);
    }

    public final <T extends BadgeNode> T label(final String label, final TextInfo info) {
        this.label = Text.create(label, info, Align.CENTER, Align.CENTER);
        return (T) this;
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.decode("#1F2937"), 8F);
        if (this.label != null) {
            DrawUtils.TEXT.drawText(super.getX() + super.dw(2), super.getY() + super.dh(2), this.label);
        }
    }

}
```

```java
BadgeNode.create(0, 0, 120, 40).label("NEW", info).attach(flex);
```

Inside `Node.draw`, the matrix is already translated to the parent of the node: draw at `getX()`/`getY()` with the size of the node, in UI units of the 1920×1080 canvas. See [Custom Nodes](../nodes/custom-nodes.md) for the node contract.

## Where you can draw

Draw only from the hooks JOID calls while it renders a frame: they run on the render thread, with the transform of the UI in place.

| Hook | When it runs |
|---|---|
| `Node.draw(double mouseX, double mouseY)` | Draws the node, after its children with a negative z-index and before the others, inside the node effects. |
| `Node.drawSkeleton(double mouseX, double mouseY)` | Replaces `draw` while the node is not mounted (see [Node Fundamentals](../nodes/node-fundamentals.md)). |
| `UI.drawBackground(double mouseX, double mouseY)` | Before the view transform (offset and zoom of the UI) is applied and before any node. |
| `UI.preDraw(double mouseX, double mouseY)` | After the root nodes with a negative z-index, before the root nodes with a z-index from 0 to 99. |
| `UI.postDraw(double mouseX, double mouseY)` | After the root nodes with a z-index from 0 to 99, before the root nodes with a z-index of 100 or more. |
| `NodeEffect.pre` / `NodeEffect.post` | Around the drawing of a node (see [Custom Effects](../styling/custom-effects.md)). |
| `ITextEffect.background` / `ITextEffect.decorate` | Behind and over a glyph (see [Markup and Text Effects](../text/markup-and-effects.md)). |

## DrawUtils entry points

| Field | Class | Draws |
|---|---|---|
| `DrawUtils.SHAPE` | `DrawShape` | Rectangles, rounded rectangles, circles, borders, polygons, lines and curves. See [Shapes](shapes.md). |
| `DrawUtils.TEXT` | `DrawText` | Strings and `Text`, aligned, cut or wrapped. See [Drawing Text](text.md). |
| `DrawUtils.RESOURCE` | `DrawResource` | Images, animations and videos of a `Resource`, whole or a region. See [Drawing Resources](resources.md). |
| `DrawUtils.MODEL` | `DrawModel` | 3D models. See [3D Models](models.md). |

Each class is a singleton also reachable through its static `getInstance()` (`DrawShape.getInstance()`...). Creating a second instance throws a `RuntimeException`.

Everything below the drawing classes (matrices, `Transformation`, framebuffers, the `Tessellator`) is on [Transformations and Framebuffers](transformations.md).

## Pixel alignment

UI units rarely cover a whole number of window pixels: one unit is 0.7115 pixel in a 1366×768 window. JOID keeps what it draws on the window pixels, so edges stay sharp and nothing shimmers while a UI scrolls or slides. All of it applies only while the transform is axis-aligned (no rotation, skew or perspective tilt); a rotated or skewed drawing keeps its exact geometry.

- **Rectangle edges snap.** `drawRect`, `drawRoundedRect`, `drawBorder`, `drawFilledBorder` and `drawResource` put each edge on the nearest window pixel. Two edges at the same position land on the same pixel, so a child that fills its parent never lets it show through, and a side never collapses below one pixel.
- **Thin rectangles become lines.** On an axis where `drawRect` covers less than three pixels (an underline, a separator, a caret), it keeps a whole number of pixels centered on its exact position. Below one pixel it is drawn one pixel thick with a proportional opacity, so a hairline keeps the same weight everywhere and never vanishes. Borders keep the same whole thickness on every side.
- **Text sits on the pixels.** The baseline of a text lands on a pixel row and its x-height on a whole number of pixels.
- **Motion moves by whole pixels.** A node whose position changes from one frame to the next (scroll, drag, animation, layout, your own code) moves by whole window pixels from where it rested, with its children, and is drawn at its exact position again once it stops. The scroll offset of an overflow node and the translations of a `Transformation` are rounded the same way. The layout and the hit tests keep the exact positions.

Circles, polygons and lines keep their exact geometry.

### Moving a drawing inside its node with quantize

A drawing that follows its node moves with it. A drawing that moves on its own inside a still node rounds its motion itself with `IRenderBridge.quantize(double motionX, double motionY)`, which shifts the matrix so the motion becomes a whole number of window pixels, until the matrix is popped:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
final double offset = this.slide.getValue() * 300D;
render.pushMatrix();
try {
    render.quantize(offset, 0D);
    DrawUtils.SHAPE.drawRect(super.getX() + offset, super.getY(), 40D, 40D, Color.WHITE);
} finally {
    render.popMatrix();
}
```

`BridgeHandler` (`dev.joid.lib.bridge`) holds the registered bridges; `BridgeHandler.RENDER.get()` is the render bridge.

### PixelGrid reference

`render.getPixelGrid()` returns the `PixelGrid` (`dev.joid.lib.bridge.render.matrix`) of the current projection, matrix and viewport. Use it to snap your own geometry like the built-in shapes do. Screen positions are viewport pixels, from the bottom-left corner, Y up.

| Method | Description |
|---|---|
| `isAligned()` | `true` when the transform is axis-aligned; every snapping method returns its input unchanged otherwise. |
| `getScaleX()`, `getScaleY()` | Window pixels per UI unit along each axis. |
| `getUnitX()`, `getUnitY()` | Signed screen pixels per UI unit (`getUnitY()` is negative for the UI canvas, whose Y goes down). |
| `getOriginX()`, `getOriginY()` | Screen position of the UI point (0, 0). |
| `toScreenX(double x)`, `toScreenY(double y)` | UI position to screen pixels. |
| `fromScreenX(double)`, `fromScreenY(double)` | Screen pixels to UI position. |
| `snapX(double x)`, `snapY(double y)` | Position of the nearest pixel edge. |
| `snapRight(double left, double right)`, `snapBottom(double top, double bottom)` | Snapped far edge, at least one pixel away from the snapped near edge. |
| `snapWidth(double left, double width)`, `snapHeight(double top, double height)` | Far edge of a stroke growing from `left` (or `top`), a whole number of pixels thick, at least one. |
| `spanX(double x, double width)`, `spanY(double y, double height)` | `Span` of an edge pair: `getStart()`, `getEnd()` and `getCoverage()` (below 1 for a span thinner than a pixel). |
| `quantizeX(double offset)`, `quantizeY(double offset)` | Offset rounded to whole pixels. |
| `toPixelWidth(double width)`, `toPixelHeight(double height)` | Size in pixels, rounded up, at least 1. |
| `PixelGrid.of(double scaleX, double scaleY)` | Aligned grid with this scale and its origin at (0, 0). |
| `PixelGrid.of(float[] projection, float[] modelView, int viewportWidth, int viewportHeight)` | Grid of these matrices and viewport. |

```java
final PixelGrid grid = BridgeHandler.RENDER.get().getPixelGrid();
final double left = grid.snapX(super.getX());
final double right = grid.snapRight(super.getX(), super.getX() + super.getWidth());
```

## Render state basics

The drawing classes manage their own state: each shape and resource call pushes and pops the matrix, enables normal blending for its draw and leaves blending disabled and no texture bound afterward. Text draws with its own shader and resets the current color to white.

When you change the state yourself, save it with `pushState()` and restore it with `popState()` in a `finally` block:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
render.pushState();
try {
    render.lineSmooth(true);
    render.lineWidth(6F);
    DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, Color.RED, points);
} finally {
    render.popState();
}
```

| Method of `IRenderBridge` | Description |
|---|---|
| `pushState()`, `popState()` | Save and restore the whole state: color, blending, depth, culling, lighting, color mask, alpha test, lines, stencil, viewport, shader, texture and framebuffer. |
| `color(float r, float g, float b, float a)` | Current color; vertices without their own color take it. `Color.bind()` sets it from a `Color`, `Color.reset()` sets it back to white (see [Colors and Gradients](../styling/colors.md)). |
| `blend(BlendState state)` | `BlendState.NORMAL`, `BlendState.PREMULTIPLIED`, `BlendState.DISABLED` or `BlendState.create(...)`. Disabled by default. |
| `lineWidth(float width)`, `getLineWidth()` | Width of lines, in window pixels. `1F` by default. |
| `lineSmooth(boolean smooth)`, `isLineSmooth()` | Antialiased lines. |
| `depth(boolean test, boolean write)` | Depth test and depth writes. |
| `cull(boolean cull)` | Back-face culling. |
| `colorMask(boolean write)` | Whether the color is written. |
| `alphaTest(float threshold)` | Discards the fragments whose alpha is at or below the threshold; stays on until `popState()`. |
| `shader(IShader shader)`, `getShader()` | Current shader, `null` for the default one (see [Custom Shaders](../shaders/custom-shaders.md)). |
| `resetTexture()` | Unbinds the texture. |
| `getViewportWidth()`, `getViewportHeight()` | Viewport size, in window pixels. |
| `getPixelGrid()` | See [PixelGrid reference](#pixelgrid-reference). |

The matrix methods (`pushMatrix`, `translate`, `rotate`, `scale`...) are on [Transformations and Framebuffers](transformations.md); the complete interface, stencil included, is on [Bridges](../integration/bridges.md).

## See also

- [Shapes](shapes.md)
- [Drawing Text](text.md)
- [Drawing Resources](resources.md)
- [3D Models](models.md)
- [Transformations and Framebuffers](transformations.md)
- [Custom Nodes](../nodes/custom-nodes.md)