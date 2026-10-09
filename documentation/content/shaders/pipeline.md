# Shader Pipeline

In [Styling and Effects](../concepts/styling.md) you saw that a node with shader effects draws into an offscreen buffer, then runs one pass per effect in a fixed order. `ShaderPipeline` (`dev.joid.lib.shader.pipeline`) is that mechanism: it renders a drawing through a chain of shader passes. The drawing is rendered offscreen, each `IShaderPass` processes the result in turn, and the last one composites it on the current target. The shader effects of nodes (`RoundedNodeEffect`, `CircleNodeEffect`, `BlurNodeEffect`, `BorderNodeEffect`) run through it; call it yourself to post-process your own drawing, and implement `IShaderPass` to add your own processing.

## Rendering through passes

Most of the time you do not call the pipeline: a shader effect on a node is enough (see [Effects](../styling/effects.md)). To apply passes to your own drawing, wrap it in `ShaderPipeline.render(...)`, for example in the `draw(...)` of a [custom node](../nodes/custom-nodes.md):

```java
@Override
public void draw(final double mouseX, final double mouseY) {
	ShaderPipeline.render(this, () -> DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.decode("#999999")), new BlurShaderPass(3F, true, 0), new BlurShaderPass(3F, false, 0), new BorderShaderPass(4F, Color.WHITE));
}
```

![A gray rectangle with blurred edges, outlined by a white border that follows the blurred shape](../images/pipeline-render.png "Blurred horizontally, then vertically, then outlined")

The rectangle is blurred horizontally, then vertically, then outlined, whatever the order of the arguments: the passes run by [priority](#ordering-passes-with-priority). The rectangle given to the pipeline (here the node's) uses the coordinates of the current drawing space, the same as the drawing.

## How a render works

![The drawing goes into framebuffer A, each pass but the last draws A into B and the two swap, the last pass composites onto the current target](../images/diagram-shader-pipeline.png "One render: the drawing into a framebuffer, then one pass after the other")

1. Without any pass, the drawing runs directly.
2. The passes are sorted by ascending `priority()`.
3. The region is the rectangle enlarged on every side by the largest `expansion()` of the passes. When the transform is axis-aligned, the region is extended outward to whole window pixels.
4. Two framebuffers the size of the region in window pixels are taken from the pool.
5. The first framebuffer is cleared to transparent, and the drawing draws into it with normal blending, in the same coordinates as on screen.
6. Each pass but the last is bound with `bind(...)` and draws the previous framebuffer into the other one, then they swap.
7. The last pass is bound with `bind(...)` and draws the result on the current target, as a quad covering the region, with premultiplied blending. Under a rotation or a skew, the quad is enlarged by one texel and sampled with `CLAMP_TO_EDGE`, so its edges stay smooth.

A rectangle with a zero or negative width or height skips the passes: the drawing runs directly. The render state is saved before the render and restored after, even when a draw throws. Anything the drawing draws outside the region is cut off: expansion is the only way for a pass to draw outside the rectangle.

## Ordering passes with priority

`priority()` orders the passes: lower values run first, closer to the original drawing. Passes with the same priority keep the order of the list (for node effects, the order of the effects on the node).

| Pass | Priority | Expansion | Effect that creates it |
|---|---|---|---|
| `RoundedShaderPass` | `100` | `0` | `RoundedNodeEffect` |
| `CircleShaderPass` | `100` | `0` | `CircleNodeEffect` |
| `BlurShaderPass` | `150 + 2 × iteration`, `+ 1` when vertical | `radius` | `BlurNodeEffect` (one horizontal and one vertical pass, iteration `0`) |
| `BorderShaderPass` | `200` | `borderWidth + 2` | `BorderNodeEffect` |

A shape is therefore cut first, then blurred, then outlined. Choose the priority of your own passes relative to these values.

## Making room with expansion

`expansion()` returns, in canvas units, how far a pass needs to draw outside the rectangle: a blur spreads by its radius, an outer border by its width. The pipeline enlarges the region by the largest expansion of all its passes, so every pass of the render receives the same region (`ShaderPassContext.getExpansion()` is that largest value). Keep it as small as the effect allows: the framebuffers grow with it.

## Writing a IShaderPass

A pass binds a shader in `bind`, and releases it in `unbind`. The pipeline then draws one quad covering the region (`getRegionX()`...), with the previous result bound as the current texture, a white vertex color and premultiplied blending. In the shader:

- The texture is the previous result, with premultiplied alpha. Read it through a sampler you do not assign (see [Textures and samplers](custom-shaders.md#textures-and-samplers)).
- The texture coordinates go from `0` to `1` across the region; `getTexelWidth()` / `getTexelHeight()` give the size of one pixel, for neighbor samples.
- Write a premultiplied color to `fragColor`.
- Distances in canvas units become window pixels with `getGrid().getScaleX()` / `getScaleY()`.

This pass turns the result to grayscale. A shader is a pair of GLSL files, a vertex shader and a fragment shader, loaded by a `ShaderProgram` subclass. JOID declares the inputs and the output for you: the vertex attributes `aPosition` and `aTexCoord`, the matrices `uProjectionMatrix` and `uModelViewMatrix`, and `fragColor`, the color the fragment shader writes. The next page, [Custom Shaders](custom-shaders.md), explains this format, the loading and the uniforms in full. The shader files:

```glsl
out vec2 vTexCoord;

void main() {
	vTexCoord = aTexCoord;
	gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}
```

```glsl
in vec2 vTexCoord;

uniform sampler2D tex;
uniform float u_Amount;

void main() {
	vec4 color = texture(tex, vTexCoord);
	float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
	fragColor = vec4(mix(color.rgb, vec3(gray), u_Amount), color.a);
}
```

The shader class, as on [Custom Shaders](custom-shaders.md):

```java
public class GrayscaleShader extends ShaderProgram {

	private static final GrayscaleShader INSTANCE = new GrayscaleShader();

	private GrayscaleShader() {
		super.load(GrayscaleShader.class.getResourceAsStream("/assets/myui/shaders/grayscale.vsh"), GrayscaleShader.class.getResourceAsStream("/assets/myui/shaders/grayscale.fsh"));
	}

	public static @NonNull GrayscaleShader inst() {
		return GrayscaleShader.INSTANCE;
	}

	public void bind(final float amount) {
		super.bind();
		super.getShader().uniform("u_Amount", amount);
	}

}
```

The pass:

```java
public class GrayscaleShaderPass implements IShaderPass {

	private final float amount;

	public GrayscaleShaderPass(final float amount) {
		this.amount = amount;
	}

	@Override
	public void unbind() {
		GrayscaleShader.inst().unbind();
	}

	@Override
	public int priority() {
		return 175;
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (GrayscaleShader.inst().canDraw()) {
			GrayscaleShader.inst().bind(this.amount);
		}
	}

}
```

- Priority `175` runs after the blur and before the border, so a border keeps its color.
- When the shader is not available (it failed to load), the pass binds nothing and the pipeline composites the result unchanged; `canDraw()` prints `[JOID] The shader GrayscaleShader is unavailable, what it draws is skipped` once in dev mode.

To use the pass on nodes, wrap it in a shader effect (see [Custom Effects](../styling/custom-effects.md)):

```java
public class GrayscaleNodeEffect extends NodeEffect<Node> {

	private final float amount;

	protected GrayscaleNodeEffect(final float amount) {
		this.amount = amount;
	}

	public static @NonNull GrayscaleNodeEffect create(final float amount) {
		return new GrayscaleNodeEffect(amount);
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public IShaderPass toShaderPass(final @NonNull Node node) {
		return new GrayscaleShaderPass(this.amount);
	}

}
```

```java
RectNode.create(100, 100, 300, 160).color(Color.YELLOW.toGradient(Color.BLUE)).effect(GrayscaleNodeEffect.create(1F)).attach(this);
```

![The same yellow to blue gradient rectangle twice, the second one in shades of gray](../images/pipeline-grayscale.png "Without the effect, then with GrayscaleNodeEffect.create(1F)")

## Node effects and the pipeline

During its render, a node collects the passes of its shader effects (`isShaderEffect()` returns `true`) every frame, through `toShaderPasses(node)` (by default the single `toShaderPass(node)`, or none when it returns `null`). Because the passes are created each frame, they can read values that change (a radius from a supplier, the current size of the node).

| Effect scope | Rendered through the passes |
|---|---|
| `NodeEffectScope.SELF` | The node's own drawing (`draw`, or `drawSkeleton` before mount), over the node's rectangle. Children and layers are drawn afterwards, unaffected. |
| `NodeEffectScope.CHILDREN` | The whole render of the node: its drawing, its children and its layers, over the node's rectangle. |

The node runs one pipeline render for its `SELF` passes and one for its `CHILDREN` passes; the `SELF` render is then nested inside the `CHILDREN` one.

## Framebuffer pool

- The framebuffers are pooled by nesting depth and pixel size: the next render of the same size at the same depth reuses them, and a nested render (a node with a shader effect inside a node whose `CHILDREN` effect is being rendered) gets its own pair.
- They use `TextureFilter.LINEAR` and are composited with `TextureWrap.CLAMP_TO_BORDER` on an aligned grid, `CLAMP_TO_EDGE` under a rotation.
- A pair that has not served for 5 seconds is released: `UIBridge.draw()` calls `ShaderPipeline.releaseUnused()` on the render thread at every frame. A node whose size is animated does not accumulate framebuffers.
- `ShaderPipeline.cleanup()` deletes them all, for example when your application shuts down the renderer; the next renders create new ones. Call it on the render thread, outside any render.

## Reference

### ShaderPipeline

| Method | Description |
|---|---|
| `static render(Node node, Runnable baseDraw, IShaderPass... passes)` | Renders `baseDraw` through `passes`, over the node's rectangle (`getX()`, `getY()`, `getWidth()`, `getHeight()`). |
| `static render(Node node, List<IShaderPass> passes, Runnable baseDraw)` | Same with a list. The list is not modified. |
| `static render(double x, double y, double width, double height, Runnable baseDraw, IShaderPass... passes)` | Renders `baseDraw` through `passes` over the given rectangle. |
| `static render(double x, double y, double width, double height, List<IShaderPass> passes, Runnable baseDraw)` | Same with a list. |
| `static releaseUnused()` | Deletes the pooled framebuffers unused for 5 seconds. Called at every frame by `UIBridge`. |
| `static cleanup()` | Deletes every pooled framebuffer. |

### IShaderPass

| Method | Description |
|---|---|
| `int priority()` | Order of the pass, lower first. |
| `float expansion()` | Room needed outside the rectangle, in canvas units. Default `0F`. |
| `void bind(ShaderPassContext context)` | Binds the shader that processes the previous result, drawn as a textured quad. |
| `void unbind()` | Releases what `bind` set. Always called after the draw, even when it throws. |

### ShaderPassContext

`ShaderPassContext` (`dev.joid.lib.shader.pipeline.dto`) describes the area of a render. The pipeline creates one per render and passes it to `bind`.

| Method | Description |
|---|---|
| `getX()`, `getY()`, `getWidth()`, `getHeight()` | The rectangle given to the pipeline (canvas units). |
| `getExpansion()` | Largest expansion of the passes. |
| `getGrid()` | The `PixelGrid` of the render bridge at render time: `getScaleX()` / `getScaleY()` convert canvas units to window pixels. See [Drawing Overview](../drawing/draw-utils.md#pixelgrid). |
| `getRegionX()`, `getRegionY()`, `getRegionWidth()`, `getRegionHeight()` | The rendered region (canvas units): the rectangle plus the expansion, extended to whole window pixels when the transform is axis-aligned. |
| `getTextureWidth()`, `getTextureHeight()` | Size of the framebuffers, in window pixels (at least `1`). |
| `getTexelWidth()`, `getTexelHeight()` | `1 / getTextureWidth()` and `1 / getTextureHeight()`: one pixel of the framebuffer in texture coordinates. |
| `static create(double x, double y, double width, double height, double expansion, PixelGrid grid)` | Computes a context. |

### Built-in passes

The built-in passes are in `dev.joid.lib.shader.pipeline.pass`. Each binds the matching built-in shader (see [Built-in shaders](custom-shaders.md#built-in-shaders)) and binds nothing when that shader is not available.

| Constructor | Description |
|---|---|
| `RoundedShaderPass(RoundedNodeEffect effect, Node node)` | Rounds the corners of the node: reads the radius and the rounded sides from the effect, and the node's rectangle snapped to pixels, when bound. |
| `RoundedShaderPass(float radius, float x1, float y1, float x2, float y2)` | Rounds the box whose inner rectangle (the box minus the radius on each rounded side) is `x1, y1, x2, y2`. |
| `CircleShaderPass(Node node)` | Cuts the largest circle that fits in the node: radius `min(width, height) / 2`, centered in the node. |
| `CircleShaderPass(float radius, float centerX, float centerY)` | Cuts a circle of `radius` centered on `centerX, centerY`. |
| `BlurShaderPass(float radius, boolean horizontal, int iteration)` | One-direction Gaussian blur of `radius` canvas units (converted to window pixels). Pair a horizontal and a vertical pass for a 2D blur; `iteration` orders several pairs (`0`, `1`...) for a stronger blur. |
| `BorderShaderPass(float borderWidth, Color borderColor)` | Border of `borderWidth` canvas units around the opaque shape of the drawing, `fill` `true`, `BorderMode.OUT`. |
| `BorderShaderPass(float borderWidth, Color borderColor, boolean fill)` | Same with `fill`. |
| `BorderShaderPass(float borderWidth, Color borderColor, boolean fill, BorderMode mode)` | `BorderMode.OUT` draws the border outside the shape's edge, `BorderMode.IN` inside it. With `fill` `false`, the pixels outside the rectangle on both axes (the corner areas) are left untouched. The color can be a gradient. |

`BorderMode` is `BorderShader.BorderMode` (`dev.joid.lib.shader.impl`).

## Pitfalls

- A pass only works on the previous result: there is no way to bind it around the original draw calls. Bind your own shader in a draw hook for that (see [Custom Shaders](custom-shaders.md#binding-and-drawing)).
- What the drawing draws outside the region is cut off: give the pass an `expansion()` when it draws past the rectangle.
- Write a premultiplied color: the result is composited with `BlendState.PREMULTIPLIED`.

## See also

- Next: [Custom Shaders](custom-shaders.md)
- [Effects](../styling/effects.md)
- [Custom Effects](../styling/custom-effects.md)
- [BlurNodeEffect](../styling/blur.md)
- [BorderNodeEffect](../styling/border.md)
- [Transformations and Framebuffers](../drawing/transformations.md)