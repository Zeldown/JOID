# Shader Pipeline

`ShaderPipeline` (`dev.joid.lib.shader.pipeline`) renders a drawing through a chain of shader passes: the drawing is rendered offscreen, each `ShaderPass` processes the result in turn, and the last one composites it on screen. The shader effects of nodes (`RoundedNodeEffect`, `CircleNodeEffect`, `BlurNodeEffect`, `BorderNodeEffect`) run through it automatically; call it yourself to apply passes to your own drawing, and implement `ShaderPass` to add your own post-processing.

## Rendering through passes

Most of the time you do not call the pipeline: adding a shader effect to a node is enough (see [Effects](../styling/effects.md)). To apply passes to your own drawing, wrap it in `ShaderPipeline.render(...)`, for example in the `draw(...)` of a [custom node](../nodes/custom-nodes.md):

```java
@Override
public void draw(final double mouseX, final double mouseY) {
    ShaderPipeline.render(this, () -> DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.ORANGE), new BlurShaderPass(6F, true, 0), new BlurShaderPass(6F, false, 0), new BorderShaderPass(2F, Color.WHITE));
}
```

The rectangle is blurred horizontally, then vertically, then outlined, whatever the order of the arguments: passes run by [priority](#priorities).

| Method | Description |
| --- | --- |
| `static render(Node node, Runnable baseDraw, ShaderPass... passes)` | Renders `baseDraw` through `passes`, over the node's rectangle (`getX()`, `getY()`, `getWidth()`, `getHeight()`). |
| `static render(Node node, List<ShaderPass> passes, Runnable baseDraw)` | Same with a list. The list is not modified. |
| `static render(double x, double y, double width, double height, Runnable baseDraw, ShaderPass... passes)` | Renders `baseDraw` through `passes` over the given rectangle. |
| `static render(double x, double y, double width, double height, List<ShaderPass> passes, Runnable baseDraw)` | Same with a list. |
| `static cleanup()` | Deletes the pooled framebuffers. See [Framebuffers](#framebuffers). |

The rectangle uses the coordinates of the current drawing space, the same as the drawing in `baseDraw`: for a node, its own position inside its parent.

## How a render works

1. Without any pass, `baseDraw` runs directly.
2. The passes are sorted by ascending `priority()`.
3. If there is exactly one pass, with an `expansion()` of `0`, that `supportsDirectBind()`, and the render is not nested inside another pipeline render, the pass is bound and `baseDraw` draws directly on the current target (see [Direct passes](#direct-passes)).
4. Otherwise the framebuffer path runs:
   1. The region is the rectangle enlarged on every side by the largest `expansion()` of the passes. When the current transformation is axis-aligned (no rotation or perspective), the region is extended outward to whole window pixels.
   2. Two framebuffers the size of the region in window pixels are taken from the pool.
   3. The first framebuffer is cleared to transparent, and `baseDraw` draws into it with normal alpha blending, in the same coordinates as on screen.
   4. Each pass but the last is bound with `bindForTexture(...)` and draws the previous framebuffer into the other one, then they swap.
   5. The last pass is bound with `bindForTexture(...)` and draws the result on the current target, as a quad covering the region, with premultiplied alpha blending.
5. On the framebuffer path, a rectangle with a zero or negative width or height skips the passes: `baseDraw` runs directly.

The render state (framebuffer, viewport, projection, matrices, blending, texture) is saved before the framebuffer path and restored after, even when a draw throws.

> NOTE: Anything `baseDraw` draws outside the region is cut off. Expansion is the only way for a pass to draw outside the rectangle.

## Priorities

`priority()` orders the passes: lower values run first, closer to the original drawing. Passes with the same priority keep the order of the list (for node effects: the order of the effects on the node).

| Pass | Priority | Expansion | Effect that creates it |
| --- | --- | --- | --- |
| `RoundedShaderPass` | `100` | `0` | `RoundedNodeEffect` |
| `CircleShaderPass` | `100` | `0` | `CircleNodeEffect` |
| `BlurShaderPass` | `150 + 2 × iteration`, `+ 1` when vertical | `radius` | `BlurNodeEffect` (one horizontal and one vertical pass, iteration `0`) |
| `BorderShaderPass` | `200` | `borderWidth + 2` | `BorderNodeEffect` |

A shape is therefore cut first, then blurred, then outlined. Choose the priority of your own passes relative to these values.

## Expansion

`expansion()` returns, in UI units, how far a pass needs to draw outside the rectangle: a blur spreads by its radius, an outer border by its width. The pipeline enlarges the region by the largest expansion of all its passes, so every pass of the render receives the same region (`ShaderPassContext.getExpansion()` is that largest value). Keep it as small as the effect allows: the framebuffers grow with it.

## Direct passes

A pass that returns `true` from `supportsDirectBind()` can avoid the framebuffers: when it is alone, has no expansion and the render is not nested, the pipeline calls `bindDirect(context)`, runs `baseDraw` on the current target, calls `unbind()` and restores the shader that was bound before. `bindDirect` receives a context whose region is the rectangle itself.

The shader bound by `bindDirect` processes the draw calls of `baseDraw` themselves, with their own vertex colors and textures, instead of a framebuffer texture. Draw calls that bind their own shader (rounded rectangles, circles, gradient colors) replace it for their duration.

`supportsDirectBind()` returns `false` by default, and none of the built-in passes enable it: the built-in effects always render through framebuffers.

## Framebuffers

- The framebuffers are pooled by nesting depth and pixel size: the next render of the same size at the same depth reuses them, and a nested render (a node with a shader effect inside a node whose `CHILDREN` effect is being rendered) gets its own pair.
- They use `TextureFilter.LINEAR` and are composited with `TextureWrap.CLAMP_TO_BORDER`.
- They are never released automatically. `ShaderPipeline.cleanup()` deletes them all; the next renders create new ones. Call it on the render thread, outside any render, when many sizes have accumulated (a node with a shader effect whose size is animated creates a pair per pixel size) or when shutting down.

## ShaderPassContext

`ShaderPassContext` (`dev.joid.lib.shader.pipeline.dto`) describes the area of a render. The pipeline creates one per render and passes it to `bindDirect` / `bindForTexture`.

| Method | Description |
| --- | --- |
| `getX()`, `getY()`, `getWidth()`, `getHeight()` | The rectangle given to the pipeline (UI units). |
| `getExpansion()` | Largest expansion of the passes (`0` in direct mode). |
| `getGrid()` | The `PixelGrid` of the render bridge at render time: `getScaleX()` / `getScaleY()` convert UI units to window pixels. See [Drawing Overview](../drawing/draw-utils.md). |
| `getRegionX()`, `getRegionY()`, `getRegionWidth()`, `getRegionHeight()` | The rendered region (UI units): the rectangle plus the expansion, extended to whole window pixels when the transformation is axis-aligned. |
| `getTextureWidth()`, `getTextureHeight()` | Size of the framebuffers, in window pixels (at least `1`). |
| `getTexelWidth()`, `getTexelHeight()` | `1 / getTextureWidth()` and `1 / getTextureHeight()`: one pixel of the framebuffer in texture coordinates. |
| `static create(double x, double y, double width, double height, double expansion, PixelGrid grid)` | Computes a context. |

## Writing a ShaderPass

| Method | Description |
| --- | --- |
| `int priority()` | Order of the pass, lower first. |
| `float expansion()` | Room needed outside the rectangle, in UI units. Default `0F`. |
| `boolean supportsDirectBind()` | Whether `bindDirect` can be used. Default `false`. |
| `void bindDirect(ShaderPassContext context)` | Binds the shader to process the original draw calls directly. |
| `void bindForTexture(ShaderPassContext context)` | Binds the shader to process the previous result, drawn as a textured quad. |
| `void unbind()` | Releases what the bind methods set. Always called after the draw, even when it throws. |

In `bindForTexture`, the pipeline then draws one quad covering the region (`getRegionX()`...), with the previous result bound as the current texture, a white vertex color and premultiplied alpha blending. In the shader:

- The texture is the previous result, with premultiplied alpha. Read it through a sampler you do not assign (see [Textures and samplers](custom-shaders.md#textures-and-samplers)).
- The texture coordinates go from `0` to `1` across the region; `getTexelWidth()` / `getTexelHeight()` give the size of one pixel, for neighbor samples.
- Write a premultiplied color to `fragColor`.
- Distances in UI units become window pixels with `getGrid().getScaleX()` / `getScaleY()`.

The example below adds a grayscale pass. The shader class follows [Custom Shaders](custom-shaders.md):

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

```java
public class GrayscaleShader extends ShaderImpl {

    private static final GrayscaleShader INSTANCE = new GrayscaleShader();

    private GrayscaleShader() {
        this.load(GrayscaleShader.class.getResourceAsStream("/assets/myui/shaders/grayscale.vsh"), GrayscaleShader.class.getResourceAsStream("/assets/myui/shaders/grayscale.fsh"));
    }

    public static GrayscaleShader inst() {
        return GrayscaleShader.INSTANCE;
    }

    public void bind(final float amount) {
        super.bind();
        this.shader.getFloatUniform("u_Amount").setValue(amount);
    }

}
```

```java
public class GrayscaleShaderPass implements ShaderPass {

    private final float amount;

    public GrayscaleShaderPass(final float amount) {
        this.amount = amount;
    }

    @Override
    public int priority() {
        return 175;
    }

    @Override
    public void bindForTexture(final ShaderPassContext context) {
        if (GrayscaleShader.inst().isAvailable()) {
            GrayscaleShader.inst().bind(this.amount);
        }
    }

    @Override
    public void bindDirect(final ShaderPassContext context) {}

    @Override
    public void unbind() {
        GrayscaleShader.inst().unbind();
    }

}
```

- Priority `175` runs after the blur and before the border, so a border keeps its color.
- `bindDirect` is never called, since `supportsDirectBind()` keeps its default `false`.
- When the shader is not available (it failed to load), the pass binds nothing and the pipeline composites the result unchanged.

To use the pass on nodes, wrap it in a shader effect:

```java
public class GrayscaleNodeEffect extends NodeEffect<Node> {

    private final float amount;

    private GrayscaleNodeEffect(final float amount) {
        this.amount = amount;
    }

    public static GrayscaleNodeEffect create(final float amount) {
        return new GrayscaleNodeEffect(amount);
    }

    @Override
    public boolean isShaderEffect() {
        return true;
    }

    @Override
    public ShaderPass toShaderPass(final Node node) {
        return new GrayscaleShaderPass(this.amount);
    }

}
```

```java
RectNode.create(100, 100, 300, 200).color(Color.RED.toGradient(Color.BLUE)).effect(GrayscaleNodeEffect.create(1F)).attach(this);
```

See [Custom Effects](../styling/custom-effects.md) for the effect side.

## Node effects and the pipeline

During its render, a node collects the passes of its shader effects (`isShaderEffect()` returns `true`) every frame, through `toShaderPasses(node)` (by default the single `toShaderPass(node)`, or none when it returns `null`). Because the passes are created each frame, they can read values that change (a radius from a supplier, the current size of the node).

| Effect scope | Rendered through the passes |
| --- | --- |
| `NodeEffectScope.SELF` | The node's own drawing (`draw`, or `drawSkeleton` before mount), over the node's rectangle. Children and layers are drawn afterwards, unaffected. |
| `NodeEffectScope.CHILDREN` | The whole render of the node: its drawing, its children and its layers, over the node's rectangle. |

The node runs one pipeline render for its `SELF` passes and one for its `CHILDREN` passes; the `SELF` render is then nested inside the `CHILDREN` one.

## Built-in passes

The built-in passes are in `dev.joid.lib.shader.pipeline.pass`. Each binds the matching built-in shader (see [Built-in shaders](custom-shaders.md#built-in-shaders)) and does nothing when that shader is not available.

| Constructor | Description |
| --- | --- |
| `RoundedShaderPass(RoundedNodeEffect effect, Node node)` | Rounds the corners of the node: reads the radius and the rounded sides from the effect, and the node's rectangle snapped to pixels, when bound. |
| `RoundedShaderPass(float radius, float x1, float y1, float x2, float y2)` | Rounds the box whose inner rectangle (the box minus the radius on each rounded side) is `x1, y1, x2, y2`. |
| `CircleShaderPass(Node node)` | Cuts the largest circle that fits in the node: radius `min(width, height) / 2`, centered in the node. |
| `CircleShaderPass(float radius, float centerX, float centerY)` | Cuts a circle of `radius` centered on `centerX, centerY`. |
| `BlurShaderPass(float radius, boolean horizontal, int iteration)` | One-direction Gaussian blur of `radius` UI units (converted to window pixels). Pair a horizontal and a vertical pass for a 2D blur; `iteration` orders several pairs (`0`, `1`...) for a stronger blur. |
| `BorderShaderPass(float borderWidth, Color borderColor)` | Border of `borderWidth` UI units around the opaque shape of the drawing, `fill` `true`, `BorderMode.OUT`. |
| `BorderShaderPass(float borderWidth, Color borderColor, boolean fill)` | Same with `fill`. |
| `BorderShaderPass(float borderWidth, Color borderColor, boolean fill, BorderMode mode)` | `BorderMode.OUT` draws the border outside the shape's edge, `BorderMode.IN` inside it. With `fill` `false`, the pixels outside the rectangle on both axes (the corner areas) are left untouched. The color can be a gradient. |

`BorderMode` is `BorderShader.BorderMode` (`dev.joid.lib.shader.impl`).

## See also

- [Custom Shaders](custom-shaders.md)
- [Effects](../styling/effects.md)
- [Custom Effects](../styling/custom-effects.md)
- [BlurNodeEffect](../styling/blur.md)
- [BorderNodeEffect](../styling/border.md)
- [Transformations and Framebuffers](../drawing/transformations.md)