# Custom Shaders

Write your own shaders and wire them into the pipeline.

## The flow

1. Write your `.vsh` / `.fsh` files in JOID GLSL and put them in `/assets/shaders/<name>/`.
2. Extend `ShaderImpl` to load and expose uniforms.
3. Implement a `ShaderPass` that binds the shader.
4. Optionally wrap in a `NodeEffect` for ergonomic use.

## 1. Shader files

Shaders are written once, in JOID GLSL, and every backend translates them when they are loaded — GLSL 120 on LWJGL 2, GLSL 330 on LWJGL 3, Vulkan GLSL 450 on Vulkan. JOID GLSL is regular GLSL without the engine-specific parts:

- No `#version` line.
- Vertex attributes, matrices and the fragment output are built-in — use them without declaring them.
- Declare varyings with `out` in the vertex shader and `in` in the fragment shader, one per line. `flat` is allowed.
- Declare uniforms and samplers as `uniform` lines, one per line.
- Sample textures with `texture(...)` and write the final color to `fragColor`.

| Built-in | Type | Stage |
|---|---|---|
| `aPosition` | `vec3` | vertex |
| `aTexCoord` | `vec2` | vertex |
| `aColor` | `vec4` | vertex |
| `aNormal` | `vec3` | vertex |
| `uProjectionMatrix` | `mat4` | vertex, fragment |
| `uModelViewMatrix` | `mat4` | vertex, fragment |
| `uNormalMatrix` | `mat3` | vertex, fragment |
| `uLighting` | `bool` | vertex, fragment |
| `fragColor` | `vec4` | fragment |

As long as the LWJGL 2 backend is supported, stick to the features GLSL 120 shares with newer versions: no bitwise operators, `uint`, `switch` or uniform blocks.

**`/assets/shaders/outline/outline.vsh`**:
```glsl
out vec2 vTexCoord;

void main() {
    vTexCoord = aTexCoord;
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}
```

**`/assets/shaders/outline/outline.fsh`**:
```glsl
in vec2 vTexCoord;

uniform sampler2D tex;
uniform vec4 u_OutlineColor;
uniform float u_Thickness;
uniform vec2 u_TexelSize;

void main() {
    vec4 color = texture(tex, vTexCoord);
    float alpha = 0.0;
    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            vec2 offset = vec2(i, j) * u_TexelSize * u_Thickness;
            alpha = max(alpha, texture(tex, vTexCoord + offset).a);
        }
    }
    fragColor = color.a > 0.5 ? color : vec4(u_OutlineColor.rgb, alpha * u_OutlineColor.a);
}
```

Compilation errors report the line numbers of your own file. The Java side is identical on every backend: uniforms are looked up by name.

## 2. Shader class

```java
package your.package.shader;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.impl.ShaderImpl;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;
import lombok.NonNull;

public class OutlineShader extends ShaderImpl {

    private static final OutlineShader INSTANCE = new OutlineShader();

    private OutlineShader() {
        this.load(
            JOID.class.getResourceAsStream("/assets/shaders/outline/outline.vsh"),
            JOID.class.getResourceAsStream("/assets/shaders/outline/outline.fsh")
        );
    }

    public void bind(final float thickness, final @NonNull Color color, final float texelW, final float texelH) {
        OutlineShader.INSTANCE.bind();

        final FloatUniform thick = INSTANCE.shader.getFloatUniform("u_Thickness");
        thick.setValue(thickness);

        final Float4Uniform col = INSTANCE.shader.getFloat4Uniform("u_OutlineColor");
        col.setValue(color.r, color.g, color.b, color.a);

        final Float2Uniform tex = INSTANCE.shader.getFloat2Uniform("u_TexelSize");
        tex.setValue(texelW, texelH);
    }

    public static @NonNull OutlineShader inst() {
        return OutlineShader.INSTANCE;
    }
}
```

## 3. Shader pass

```java
public class OutlineShaderPass implements ShaderPass {

    private final float thickness;
    private final Color color;

    public OutlineShaderPass(final float thickness, final @NonNull Color color) {
        this.thickness = thickness;
        this.color = color;
    }

    @Override
    public void bindDirect(final Node node) {
        this.bindInternal(node);
    }

    @Override
    public void bindForTexture(final Node node) {
        this.bindInternal(node);
    }

    @Override
    public void unbind() {
        OutlineShader.inst().unbind();
    }

    @Override
    public int priority() {
        return 180;  // before border, after blur
    }

    @Override
    public float expansion() {
        return this.thickness;
    }

    @Override
    public boolean supportsDirectBind() {
        return false;  // we sample the framebuffer
    }

    private void bindInternal(final Node node) {
        if (!OutlineShader.inst().isAvailable()) return;

        final int scaleFactor = ShaderPipeline.scaleFactor(node != null ? node.getUi() : null);
        final double w = node != null ? node.getWidth() : 200D;
        final double h = node != null ? node.getHeight() : 120D;
        final float pixelW = (float) Math.ceil((w + this.thickness * 2F) * scaleFactor);
        final float pixelH = (float) Math.ceil((h + this.thickness * 2F) * scaleFactor);
        final float texelW = 1F / Math.max(1F, pixelW);
        final float texelH = 1F / Math.max(1F, pixelH);

        OutlineShader.inst().bind(this.thickness * scaleFactor, this.color, texelW, texelH);
    }
}
```

## 4. Node effect (optional)

```java
@Getter
@SuppressWarnings("unchecked")
public class OutlineNodeEffect<T extends Node> extends NodeEffect<T> {

    private Supplier<Color> colorSupplier;
    private Supplier<Float> thicknessSupplier;

    private OutlineNodeEffect(final @NonNull Color color, final float thickness) {
        this.colorSupplier = () -> color;
        this.thicknessSupplier = () -> thickness;
    }

    public static <T extends Node> @NonNull OutlineNodeEffect<T> create(final @NonNull Color color, final float thickness) {
        return new OutlineNodeEffect<>(color, thickness);
    }

    @Override
    public boolean isShaderEffect() {
        return true;
    }

    @Override
    public ShaderPass toShaderPass(final @NonNull T node) {
        return new OutlineShaderPass(this.thicknessSupplier.get(), this.colorSupplier.get());
    }
}
```

## Usage

```java
node.effect(OutlineNodeEffect.create(Color.WHITE, 2F));
```

Done — your custom effect is now composable with all the built-ins.

## Best practices

- **Use `BooleanUniform` / `IntUniform` / `FloatUniform` / `Float2Uniform` / etc.** from `lib/bridge/render/shader/uniform` rather than engine calls.
- **Don't leak uniform lookups.** The shader caches them; repeated `getXUniform(name)` is cheap.
- **Test at multiple scale factors.** The pipeline supersamples on high-DPI displays — make sure your `u_TexelSize` accounts for it.
- **Respect the `supportsDirectBind()` contract.** If your shader needs to read the previous framebuffer, return false.

## See also

- [Shader Pipeline](pipeline.md) — the orchestrator.
- [Effects Overview](../effects/overview.md).
