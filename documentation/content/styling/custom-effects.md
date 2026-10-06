# Custom Effects

A custom effect is a subclass of `NodeEffect<T extends Node>` (`dev.joid.lib.ui.node.effect`). Write one when the [built-in effects](effects.md) do not cover a visual treatment you want to reuse on any node: a shadow, a hover lift, a color filter.

There are two kinds of effects, and you pick one by overriding different hooks:

| Kind | Override | Use it to |
| --- | --- | --- |
| Render state | `pre(...)` and `post(...)` | Draw something before or after the node, or change the render state (matrix, stencil, color) around it. |
| Shader | `isShaderEffect()` and `toShaderPass(...)` or `toShaderPasses(...)` | Process the rendered pixels of the node with a shader. |

## A render-state effect

This effect draws a hard shadow under the node while it is hovered:

```java
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

public class ShadowNodeEffect<T extends Node> extends NodeEffect<T> {

    private final Color color;
    private final double offset;

    private ShadowNodeEffect(final Color color, final double offset) {
        this.color = color;
        this.offset = offset;
    }

    public static <T extends Node> ShadowNodeEffect<T> create(final Color color, final double offset) {
        return new ShadowNodeEffect<>(color, offset);
    }

    @Override
    public boolean shouldApply(final T node) {
        return node.hoverValue(1F) > 0F;
    }

    @Override
    public void pre(final T node, final double mouseX, final double mouseY) {
        final Color shadow = this.color.copyAlpha(this.color.a * node.hoverValue(1F));
        DrawUtils.SHAPE.drawRect(node.getX() + this.offset, node.getY() + this.offset, node.getWidth(), node.getHeight(), shadow);
    }

}
```

```java
RectNode.create(100, 100, 300, 200).color(Color.WHITE).effect(ShadowNodeEffect.create(Color.BLACK.copyAlpha(0.3F), 6D)).attach(this);
```

![On a light background, the cursor hovers a white card and a gray shadow appears under it](../images/custom-effect-shadow.gif "The shadow fades in with the hover progress (shown on a light background so the 30 % black stands out).")

When `pre` runs, the current matrix is the one the node draws with: draw at `node.getX()`, `node.getY()`, in UI units, as in the node's own `draw`.

### Restoring the state in post

Whatever `pre` changes, `post` restores. This effect lifts the node while it is hovered, by pushing a matrix in `pre` and popping it in `post`:

```java
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

public class LiftNodeEffect<T extends Node> extends NodeEffect<T> {

    private final float height;

    private LiftNodeEffect(final float height) {
        this.height = height;
    }

    public static <T extends Node> LiftNodeEffect<T> create(final float height) {
        return new LiftNodeEffect<>(height);
    }

    @Override
    public void pre(final T node, final double mouseX, final double mouseY) {
        final IRenderBridge render = BridgeHandler.RENDER.get();
        render.pushMatrix();
        render.translate(0D, -node.hoverValue(this.height), 0D);
    }

    @Override
    public void post(final T node, final double mouseX, final double mouseY) {
        BridgeHandler.RENDER.get().popMatrix();
    }

}
```

![On a light background, the cursor hovers a white card that moves up](../images/custom-effect-lift.gif "RectNode.create(100, 100, 300, 200).color(Color.WHITE).effect(LiftNodeEffect.create(8F)): the card lifts by 8 units while hovered.")

The node calls `post` in a `finally` block, so the matrix is popped even if the render of the node throws.

## A shader effect

A shader effect returns shader passes; the node renders into a framebuffer and the [Shader Pipeline](../shaders/pipeline.md) runs the passes on the result. This example turns a node to grayscale. It needs three pieces: the shader, a pass and the effect.

The shader loads its GLSL from the classpath through `ShaderImpl` (`dev.joid.lib.shader.impl`), like the built-in shaders:

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
        super.shader.getFloatUniform("u_Amount").setValue(amount);
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
    public void bindDirect(final ShaderPassContext context) {
        this.bindForTexture(context);
    }

    @Override
    public void bindForTexture(final ShaderPassContext context) {
        if (GrayscaleShader.inst().isAvailable()) {
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

public class GrayscaleNodeEffect<T extends Node> extends NodeEffect<T> {

    private final float amount;

    private GrayscaleNodeEffect(final float amount) {
        this.amount = amount;
    }

    public static <T extends Node> GrayscaleNodeEffect<T> create(final float amount) {
        return new GrayscaleNodeEffect<>(amount);
    }

    @Override
    public boolean isShaderEffect() {
        return true;
    }

    @Override
    public ShaderPass toShaderPass(final T node) {
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

`supportsDirectBind()` (default `false`) and `bindDirect(...)` let a single pass skip the framebuffers and bind its shader while the node draws; the details are in [Shader Pipeline](../shaders/pipeline.md).

## The NodeEffect contract

| Hook | Called | Default |
| --- | --- | --- |
| `init(T node, UI ui)` | When the node is loaded into a UI and on `Node.reload()`, if `shouldApply` returns `true`. An effect added to a node that is already loaded is not initialized until the next reload. | Nothing. |
| `shouldApply(T node)` | Every frame before the other hooks, and before `init`. Returning `false` skips the effect. | `true` |
| `pre(T node, double mouseX, double mouseY)` | Every frame the node is visible, before its render, in priority order. Render-state effects only. | Nothing. |
| `post(T node, double mouseX, double mouseY)` | After the render of the node and its children, in the reverse order of `pre`, in a `finally` block. Render-state effects only. | Nothing. |
| `isShaderEffect()` | Every frame, to sort the effect. | `false` |
| `toShaderPasses(T node)` | Every frame, for shader effects. | A list with the result of `toShaderPass`, or an empty list when it is `null`. |
| `toShaderPass(T node)` | By the default `toShaderPasses`. | `null` |

`mouseX` and `mouseY` are the mouse position in UI coordinates.

> WARNING: When `isShaderEffect()` returns `true`, the node never calls `pre` and `post`. A shader effect that also needs render-state changes must be split into two effects.

## Rules for effect classes

- `Node.effect(...)` takes a `NodeEffect` typed by the node or by one of its parent classes. Declare your effect generic (`MyNodeEffect<T extends Node> extends NodeEffect<T>`) with a generic factory, as the built-in effects do, or extend `NodeEffect<Node>` directly, to add it to any node. An effect declared for a specific node type (`NodeEffect<RectNode>`) receives that type in its hooks and reads its getters; add it only to nodes of that type.
- A node holds one effect per class: a second instance of your class replaces the first.
- The same effect instance can be added to several nodes, and `Node.copy()` shares it with the copy. Keep per-node state out of the effect, or create one effect per node.
- To make values dynamic, store `Supplier`s and read them in the hooks, as the built-in effects do.
- Fluent setters in the library style return `<E extends MyNodeEffect<T>> E` with `return (E) this;` and `@SuppressWarnings("unchecked")`. Such setters cannot be chained inline inside `node.effect(...)` (see [Effects](effects.md#applying-effects-with-effect)).

## See also

- [Effects](effects.md)
- [Shader Pipeline](../shaders/pipeline.md)
- [Custom Shaders](../shaders/custom-shaders.md)
- [Drawing Overview](../drawing/draw-utils.md)
- [Custom Nodes](../nodes/custom-nodes.md)