# Custom Effects

A custom effect is a subclass of `NodeEffect<T extends Node>` (`dev.joid.lib.ui.node.effect`), where `T` is the node it applies to: `NodeEffect<Node>` for an effect that goes on any node, as the built-in effects, or a node class such as `NodeEffect<RectNode>` to read the getters of that node in the hooks. Write one when the [built-in effects](effects.md) do not cover a visual treatment you want to reuse on any node: a hard shadow, a hover lift, a color filter. This page closes the Styling guides: render-state effects only need the drawing calls you already know, while shader effects touch GPU programs, which the Advanced section covers in depth.

There are two kinds of effects, and you pick one by overriding different hooks:

| Kind | Override | Use it to |
| --- | --- | --- |
| Render state | `pre(...)` and `post(...)` | Draw something before or after the node, or change the render state (matrix, stencil, color) around it. |
| Shader | `isShaderEffect()` and `toShaderPass(...)` or `toShaderPasses(...)` | Process the rendered pixels of the node with a shader. |

## A render-state effect

This effect draws a hard shadow under the node while it is hovered (for a soft shadow, use the built-in [ShadowNodeEffect](shadow.md)). It draws with `DrawUtils.SHAPE`, met in [Input Controls](../essentials/controls.md), and follows the [factory pattern](../components/ui-kit.md) of the components: a `protected` constructor and a static `create(...)`:

```java
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

public class HardShadowNodeEffect extends NodeEffect<Node> {

	private final Color color;
	private final double offset;

	protected HardShadowNodeEffect(final Color color, final double offset) {
		this.color = color;
		this.offset = offset;
	}

	public static HardShadowNodeEffect create(final Color color, final double offset) {
		return new HardShadowNodeEffect(color, offset);
	}

	@Override
	public boolean shouldApply(final Node node) {
		return node.hoverValue(1F) > 0F;
	}

	@Override
	public void pre(final Node node, final double mouseX, final double mouseY) {
		final Color shadow = this.color.copyAlpha(this.color.a * node.hoverValue(1F));
		DrawUtils.SHAPE.drawRect(node.getX() + this.offset, node.getY() + this.offset, node.getWidth(), node.getHeight(), shadow);
	}

}
```

```java
RectNode.create(100, 100, 300, 200).color(Color.WHITE).effect(HardShadowNodeEffect.create(Color.BLACK.copyAlpha(0.3F), 6D)).attach(this);
```

![On a light background, the cursor hovers a white card and a gray shadow appears under it](../images/custom-effect-shadow.gif "The shadow fades in with the hover progress (shown on a light background so the 30 % black stands out).")

When `pre` runs, the current matrix is the one the node draws with: draw at `node.getX()`, `node.getY()`, in UI units, as in the node's own `draw`.

### Restoring the state in post

Whatever `pre` changes, `post` restores. `BridgeHandler.RENDER.get()` returns the render bridge of the backend (see [The Frame Loop](../concepts/frame-loop.md)); it holds the current matrix, the transformation applied to everything drawn, on a stack: `pushMatrix()` saves it, `translate(...)` moves what is drawn next, `popMatrix()` restores the saved matrix. This effect lifts the node while it is hovered, by pushing a matrix in `pre` and popping it in `post`:

```java
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

public class LiftNodeEffect extends NodeEffect<Node> {

	private final float height;

	protected LiftNodeEffect(final float height) {
		this.height = height;
	}

	public static LiftNodeEffect create(final float height) {
		return new LiftNodeEffect(height);
	}

	@Override
	public void pre(final Node node, final double mouseX, final double mouseY) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushMatrix();
		render.translate(0D, -node.hoverValue(this.height), 0D);
	}

	@Override
	public void post(final Node node, final double mouseX, final double mouseY) {
		BridgeHandler.RENDER.get().popMatrix();
	}

}
```

![On a light background, the cursor hovers a white card that moves up](../images/custom-effect-lift.gif "RectNode.create(100, 100, 300, 200).color(Color.WHITE).effect(LiftNodeEffect.create(8F)): the card lifts by 8 units while hovered.")

The node calls `post` in a `finally` block, so the matrix is popped even if the render of the node throws.

## A shader effect

A shader effect returns shader passes. As in [Effects](effects.md#effects-and-the-shader-pipeline), the node renders into a framebuffer (an offscreen image), then each pass runs a shader (a small GPU program written in GLSL, run on every pixel) over that image. This example turns a node to grayscale. It needs three pieces: the shader, a pass and the effect. This section shows the minimum; the [Shader Pipeline](../shaders/pipeline.md) and [Custom Shaders](../shaders/custom-shaders.md) pages of the Advanced section explain the pipeline, the GLSL dialect and the uniforms in depth.

The shader loads its GLSL from the classpath through `ShaderImpl` (`dev.joid.lib.shader.impl`), like the built-in shaders. A uniform is a value you hand to the GPU program before it runs, here `u_Amount`, the strength of the filter:

```java
import dev.joid.lib.shader.impl.ShaderImpl;

public final class GrayscaleShader extends ShaderImpl {

	private static final GrayscaleShader INSTANCE = new GrayscaleShader();

	private GrayscaleShader() {
		super.load(GrayscaleShader.class.getResourceAsStream("/assets/myapp/shaders/grayscale.vsh"), GrayscaleShader.class.getResourceAsStream("/assets/myapp/shaders/grayscale.fsh"));
	}

	public static GrayscaleShader inst() {
		return GrayscaleShader.INSTANCE;
	}

	public void bind(final float amount) {
		super.bind();
		super.shader.uniform("u_Amount", amount);
	}

}
```

`grayscale.vsh`:

```glsl
out vec2 vTexCoord;

void main() {
    vTexCoord = aTexCoord;
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}
```

`grayscale.fsh`:

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

The shaders are written in the JOID GLSL dialect: no `#version`, built-in attributes (`aPosition`, `aTexCoord`, `aColor`), matrices (`uProjectionMatrix`, `uModelViewMatrix`) and output (`fragColor`), see [Custom Shaders](../shaders/custom-shaders.md). The texture holds the rendered node with premultiplied alpha, and the pipeline draws the output with premultiplied blending: keep the output premultiplied (a linear filter like this one does).

The pass binds the shader with its values:

```java
import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;

public final class GrayscaleShaderPass implements ShaderPass {

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
		if (GrayscaleShader.inst().canDraw()) {
			GrayscaleShader.inst().bind(this.amount);
		}
	}

	@Override
	public void unbind() {
		GrayscaleShader.inst().unbind();
	}

}
```

The effect creates a pass with its current values; the node calls `toShaderPasses` every frame:

```java
import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

public class GrayscaleNodeEffect extends NodeEffect<Node> {

	private final float amount;

	protected GrayscaleNodeEffect(final float amount) {
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
ResourceNode.create(100, 100, 200, 200).resource(Resource.of("https://placehold.co/400x400/orange/white.png")).effect(GrayscaleNodeEffect.create(1F)).attach(this);
```

![An orange placeholder image next to the same image in gray](../images/custom-effect-grayscale.png "Left, the image without effect; right, with GrayscaleNodeEffect.create(1F).")

### Choosing the pass priority and expansion

The pipeline sorts the passes of a node by `priority()`, lowest first. The built-in passes use 100 (rounded, circle), 150 and 151 (blur) and 200 (border); the example uses 175 so the grayscale runs after the blur and before the border, which keeps its color.

Override `expansion()` (default `0F`) when the pass draws outside the node's rectangle, as a glow or a shadow would: the node is then rendered into an area enlarged by the largest expansion of its passes, in UI units on each side. Return several passes from `toShaderPasses(T node)` when the effect needs more than one, as the blur does with its horizontal and vertical passes.

The pipeline always renders the node into a framebuffer, then composes it through the passes; `bindForTexture` binds the shader while that texture is drawn (see [Shader Pipeline](../shaders/pipeline.md)). `canDraw()` answers like `isAvailable()` and, in dev mode, prints once `[JOID] The shader <Class> is unavailable, what it draws is skipped`; call `isAvailable()` instead to choose a fallback silently.

## An effect for one node type

An effect typed by a node class receives that class in its hooks. This effect underlines a `RectNode` with its own color:

```java
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class UnderlineNodeEffect extends NodeEffect<RectNode> {

	private final double thickness;

	protected UnderlineNodeEffect(final double thickness) {
		this.thickness = thickness;
	}

	public static UnderlineNodeEffect create(final double thickness) {
		return new UnderlineNodeEffect(thickness);
	}

	@Override
	public void post(final RectNode node, final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(node.getX(), node.getY() + node.getHeight() + 4D, node.getWidth(), this.thickness, node.getColor());
	}

}
```

```java
RectNode.create(100, 100, 300, 60).color(Color.BLUE).effect(UnderlineNodeEffect.create(4D)).attach(this);
```

`effect(...)` takes it on a `RectNode` and its subclasses. The node calls the hooks with itself, so add such an effect only to nodes of its type.

## Extending a built-in effect

The built-in effects are classes you can extend. Their setters return the type the context asks for (`public final <E extends BorderNodeEffect> E fill(boolean fill)`), so they also return your subclass, with a type witness in the middle of a chain. This border is drawn only while the node is hovered:

```java
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;

public class HoverBorderNodeEffect extends BorderNodeEffect {

	private boolean hoverOnly;

	protected HoverBorderNodeEffect(final Color color, final float width) {
		super(color, width, BorderMode.OUT);
		this.hoverOnly = true;
	}

	public static HoverBorderNodeEffect create(final Color color, final float width) {
		return new HoverBorderNodeEffect(color, width);
	}

	@SuppressWarnings("unchecked")
	public final <E extends HoverBorderNodeEffect> E hoverOnly(final boolean hoverOnly) {
		this.hoverOnly = hoverOnly;
		return (E) this;
	}

	@Override
	public boolean shouldApply(final Node node) {
		return !this.hoverOnly || node.hoverValue(1F) > 0F;
	}

}
```

```java
final HoverBorderNodeEffect border = HoverBorderNodeEffect.create(Color.BLUE, 2F).<HoverBorderNodeEffect>fill(false).hoverOnly(true);
RectNode.create(100, 100, 300, 200).color(Color.WHITE).effect(border).attach(this);
```

`fill` is declared by `BorderNodeEffect`: without the witness `<HoverBorderNodeEffect>`, it returns a `BorderNodeEffect` in the middle of the chain and `hoverOnly` is not found. The last setter takes its type from the variable, or from `effect(...)` when you pass the effect straight to it: `.effect(HoverBorderNodeEffect.create(Color.BLUE, 2F).<HoverBorderNodeEffect>fill(false).hoverOnly(true))`. The class of the effect is your subclass, so `getEffect(HoverBorderNodeEffect.class)` finds it and `getEffect(BorderNodeEffect.class)` does not.

## The NodeEffect contract

| Hook | Called | Default |
| --- | --- | --- |
| `init(T node, UI ui)` | When the node is loaded into a UI and on `Node.reload()`, if `shouldApply` returns `true`. An effect added to a node that is already loaded is not initialized until the next reload. | Nothing. |
| `detach(T node)` | When the node is detached (`remove`, `clearChildren`, an `append` that moves it, its UI closing or reloading), for every effect of the node. Release there what `init` acquired: `init` runs again if the node is attached again. | Nothing. |
| `shouldApply(T node)` | Every frame before the other hooks, and before `init`. Returning `false` skips the effect. | `true` |
| `pre(T node, double mouseX, double mouseY)` | Every frame the node is visible, before its render, in priority order. Render-state effects only. | Nothing. |
| `post(T node, double mouseX, double mouseY)` | After the render of the node and its children, in the reverse order of `pre`, in a `finally` block. Render-state effects only. | Nothing. |
| `isShaderEffect()` | Every frame, to sort the effect. | `false` |
| `toShaderPasses(T node)` | Every frame, for shader effects. | A list with the result of `toShaderPass`, or an empty list when it is `null`. |
| `toShaderPass(T node)` | By the default `toShaderPasses`. | `null` |

`mouseX` and `mouseY` are the mouse position in UI coordinates.

> WARNING: When `isShaderEffect()` returns `true`, the node never calls `pre` and `post`. A shader effect that also needs render-state changes must be split into two effects.

## Rules for effect classes

- `Node.effect(...)` takes a `NodeEffect` typed by the node or by one of its parent classes. Extend `NodeEffect<Node>` to add your effect to any node, as the built-in effects do. An effect declared for a specific node type (`MyNodeEffect extends NodeEffect<RectNode>`) receives that type in its hooks and reads its getters; add it only to nodes of that type.
- A node holds one effect per class: a second instance of your class replaces the first.
- The same effect instance can be added to several nodes, and `Node.copy()` shares it with the copy. Keep per-node state out of the effect, or create one effect per node.
- To make values dynamic, store `Supplier`s and read them in the hooks, as the built-in effects do.
- Fluent setters return the type the context asks for, as those of the built-in effects and of the nodes: `public final <E extends MyNodeEffect> E amount(final float amount)` with `return (E) this;` (and `@SuppressWarnings("unchecked")` on the class). Assigned to a variable or at the end of a chain, the setter returns the type you expect; in the middle of a chain it returns the class that declares it, so the setter of a subclass after it needs a type witness.
- A configured effect goes straight into `node.effect(...)`, without witness on its last setter: `node.effect(MyNodeEffect.create().amount(1F))`. To build it from the node, add it in `self(...)`: `node.self(target -> target.effect(MyNodeEffect.create().amount(() -> target.hoverValue(1F))))`.

## Pitfalls

- A shader pass draws the node's texture: keep the output premultiplied.
- Use `canDraw()` in a pass that skips its draw when the shader is missing (dev warning), `isAvailable()` to pick a fallback silently.
- `detach` is called when the node is detached and `init` again when it is attached: release there what the effect holds.

## See also

- Next: [TweenAnimator](../animation/tween-animator.md), the Animation guides
- [Effects](effects.md)
- [Shader Pipeline](../shaders/pipeline.md)
- [Custom Shaders](../shaders/custom-shaders.md)
- [Drawing Overview](../drawing/draw-utils.md)
- [Custom Nodes](../nodes/custom-nodes.md)