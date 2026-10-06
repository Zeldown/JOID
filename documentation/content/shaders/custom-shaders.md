# Custom Shaders

JOID shaders are written once, in JOID GLSL, and each backend translates them when they load: GLSL 1.20 on LWJGL 2, GLSL 3.30 core on LWJGL 3, GLSL 4.50 compiled to SPIR-V on Vulkan. This page covers the dialect, loading a shader (`ShaderImpl` or `IRenderBridge.createShader`), binding it around your draw calls, the uniforms API and the built-in shaders. To post-process a node with a shader, combine it with a pass of the [Shader Pipeline](pipeline.md).

## A first shader

A shader is a pair of files, a vertex shader and a fragment shader, usually in your resources. This one paints animated stripes with the draw color:

`/assets/myui/shaders/wave.vsh`:

```glsl
out vec2 vTexCoord;
out vec4 vColor;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}
```

`/assets/myui/shaders/wave.fsh`:

```glsl
in vec2 vTexCoord;
in vec4 vColor;

uniform float u_Time;

void main() {
    float wave = 0.5 + 0.5 * sin(vTexCoord.x * 12.0 + u_Time * 3.0);
    fragColor = vec4(vColor.rgb * wave, vColor.a);
}
```

A `ShaderImpl` subclass loads the pair and sets the uniforms:

```java
public class WaveShader extends ShaderImpl {

    private static final WaveShader INSTANCE = new WaveShader();

    private WaveShader() {
        this.load(WaveShader.class.getResourceAsStream("/assets/myui/shaders/wave.vsh"), WaveShader.class.getResourceAsStream("/assets/myui/shaders/wave.fsh"));
    }

    public static WaveShader inst() {
        return WaveShader.INSTANCE;
    }

    public void bind(final float time) {
        super.bind();
        this.shader.getFloatUniform("u_Time").setValue(time);
    }

}
```

A [custom node](../nodes/custom-nodes.md) binds it around its draw calls:

```java
public class WaveNode extends Node {

    protected WaveNode(final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
    }

    public static WaveNode create(final double x, final double y, final double width, final double height) {
        return new WaveNode(x, y, width, height);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        final WaveShader shader = WaveShader.inst();
        if (!shader.isAvailable()) {
            return;
        }

        final IRenderBridge render = BridgeHandler.RENDER.get();
        final IShader previous = render.getShader();
        shader.bind(BridgeHandler.CLOCK.get().currentTimeMillis() % 60000L / 1000F);
        try {
            final double x = super.getX();
            final double y = super.getY();
            final double width = super.getWidth();
            final double height = super.getHeight();
            final Tessellator tessellator = Tessellator.inst();
            tessellator.start(DrawMode.QUADS);
            tessellator.addVertexWithUV(x, y + height, 0D, 0D, 1D);
            tessellator.addVertexWithUV(x + width, y + height, 0D, 1D, 1D);
            tessellator.addVertexWithUV(x + width, y, 0D, 1D, 0D);
            tessellator.addVertexWithUV(x, y, 0D, 0D, 0D);
            tessellator.draw();
        } finally {
            shader.unbind();
            render.shader(previous);
        }
    }

}
```

```java
WaveNode.create(660, 440, 600, 200).attach(this);
```

The first call to `WaveShader.inst()` loads the class, which compiles the shader: make it from a draw hook (or any code running on the render thread once the backend is registered), as above.

## JOID GLSL

JOID GLSL is GLSL without the parts that differ between backends: JOID declares the vertex attributes, the matrices and the output for you, and generates the right declarations for each backend from yours.

### Rules

- No `#version` line (it is removed if present).
- The vertex shader reads only the [built-in attributes](#built-in-variables). Declaring another `in` variable in a vertex shader throws `IllegalArgumentException: Vertex shaders cannot declare the input <name>, use the built-in attributes aPosition, aTexCoord, aColor and aNormal`.
- The fragment shader writes its color to `fragColor`. Declaring an `out` variable in a fragment shader throws `IllegalArgumentException: Fragment shaders cannot declare the output <name>, write the color to fragColor`.
- Varyings are declared with `out` in the vertex shader and `in` in the fragment shader, with the same name and type. `flat` is accepted.
- Uniforms are declared with `uniform`. A uniform whose type starts with `sampler` is a sampler (use `sampler2D`).
- Each declaration is alone on its line, in the form `[layout(...)] [flat] in|out|uniform <type> <name>[[size]];`. An optional `layout(...)` prefix is dropped. Comments around it are ignored.
- One name per declaration, no initializer (`= ...`), no precision or other qualifier, no interface block. A line that does not match the form above is not recognized as a declaration and is passed to the compiler unchanged, which fails on some backends.
- Declarations of built-in variables are ignored, so declaring them for an editor or a linter is harmless.
- Sample textures with `texture(sampler2D, vec2)`.
- The fragment shader's entry point is `void main()`, with empty parentheses (not `void main(void)`).

### Built-in variables

Use them without declaring them; JOID detects which ones your code uses (comments excluded) and declares only those.

| Identifier | Type | Kind | Stages | Content |
| --- | --- | --- | --- | --- |
| `aPosition` | `vec3` | attribute | vertex | Vertex position, in the coordinates given to the draw call (UI units of the current drawing space). |
| `aTexCoord` | `vec2` | attribute | vertex | Vertex texture coordinates. |
| `aColor` | `vec4` | attribute | vertex | Vertex color, or the current render color when the vertices have none. |
| `aNormal` | `vec3` | attribute | vertex | Vertex normal (3D models). |
| `uProjectionMatrix` | `mat4` | uniform | vertex, fragment | Current projection matrix. |
| `uModelViewMatrix` | `mat4` | uniform | vertex, fragment | Current model-view matrix. |
| `uNormalMatrix` | `mat3` | uniform | vertex, fragment | Normal matrix of the model-view matrix. |
| `uLighting` | `bool` | uniform | vertex, fragment | Whether lighting is enabled on the render bridge. |
| `fragColor` | `vec4` | output | fragment | Output color. |

The backend fills the built-in uniforms. The standard vertex transformation is `gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);`.

### Writing portable GLSL

Your code is compiled as GLSL 1.20 on LWJGL 2, as GLSL 3.30 core on LWJGL 3 and as GLSL 4.50 on Vulkan. To run on every backend, keep the code within what these versions share:

- GLSL 1.20 features only: no `%` or bitwise operators on integers, no `uint`, no `switch`, no `texelFetch` or `textureSize`.
- `texture(...)` to sample (LWJGL 2 maps it to `texture2D`), never `texture2D`, `gl_FragColor`, `attribute` or `varying`.
- `flat` varyings are interpolated normally on LWJGL 2, which does not support `flat`.

### What the backends generate

| Backend | Generated header |
| --- | --- |
| LWJGL 2 | `#version 120`; `#define texture texture2D`; built-in attributes, matrices and `fragColor` defined to `gl_Vertex.xyz`, `gl_MultiTexCoord0.xy`, `gl_Color`, `gl_Normal`, `gl_ProjectionMatrix`, `gl_ModelViewMatrix`, `gl_NormalMatrix`, `gl_FragColor`; `uniform bool uLighting`; your uniforms and samplers; your varyings as `varying` (without `flat`). |
| LWJGL 3 | `#version 330 core`; built-in uniforms; built-in attributes at fixed locations (vertex stage); `out vec4 fragColor` (fragment stage); your uniforms and samplers; your varyings as `in` / `out`, with `flat`. |
| Vulkan | `#version 450`; built-in attributes at fixed locations (vertex stage); `layout(location = 0) out vec4 fragColor` (fragment stage); one `std140` uniform block holding the built-in uniforms and every uniform of both stages; one binding per sampler; varyings at locations matched by name. |

On Vulkan, a uniform declared in both stages is a single value: give it the same type in both. A sampler declared in both stages shares one binding as well.

### Reserved names

- Identifiers starting with `joid_` and the block names `JoidUniforms` and `JoidAlphaTest` are generated by the backends (`joid_main`, `joid_AlphaTest`, `joid_Color`...): do not use them.
- `texture` is a macro on LWJGL 2: do not use it as a variable name.
- On LWJGL 3 and Vulkan, the fragment shader's `void main()` is renamed `joid_main()` and wrapped by a `main()` that applies the alpha test of the render state (`IRenderBridge.alphaTest(...)`).

### Errors and line numbers

Declarations and the `#version` line are replaced by empty lines and the backends insert a `#line` directive after their header, so compiler messages give the line numbers of your own file. The backends print compile and link errors to `System.err` (for example `Fragment shader compilation failed: ...` on LWJGL, `Vulkan fragment shader compilation failed: ...` on Vulkan), and the shader is then inactive (`IShader.isActive()` returns `false`).

## Loading a shader

### With ShaderImpl

`ShaderImpl` (`dev.joid.lib.shader.impl`) is the base class of the built-in shaders and the simplest way to write one, as in [A first shader](#a-first-shader).

| Member | Description |
| --- | --- |
| `protected void load(InputStream vertexShader, InputStream fragmentShader)` | Reads both streams as JOID GLSL (UTF-8) and creates the shader through the render bridge, with `BlendState.NORMAL`. On any exception, prints `[JOID] Unable to load the shader <ClassName>: <message>` and the stack trace, and leaves the shader `null`. A `null` stream throws a `NullPointerException`. |
| `protected IShader shader` / `getShader()` | The loaded shader, `null` when loading failed. |
| `void bind()` | Binds the shader if it is available, otherwise does nothing. |
| `void unbind()` | Unbinds the shader if it was created. |
| `boolean isAvailable()` | `true` when the shader was created and compiled (`getShader() != null && getShader().isActive()`). |

Check `isAvailable()` before setting uniforms: a shader that failed to load must not break the UI, so draw without it (or skip the effect) when it is unavailable.

### With IRenderBridge.createShader

To create a shader from strings, or with another blending mode, call the render bridge directly:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
final IShader shader = render.createShader(ShaderSource.parse(ShaderStage.VERTEX, vertexCode), ShaderSource.parse(ShaderStage.FRAGMENT, fragmentCode), BlendState.NORMAL);
if (!shader.isActive()) {
    System.err.println("The shader did not compile");
}
```

| Method | Description |
| --- | --- |
| `IRenderBridge.createShader(ShaderSource vertex, ShaderSource fragment, BlendState blend)` | Translates, compiles and links the pair. `blend` is the blending mode applied while the shader is bound. Compile errors do not throw: the shader is returned inactive. |
| `ShaderSource.read(ShaderStage stage, InputStream stream)` | Reads a stream (UTF-8) and parses it. The stream is not closed. An `IOException` is rethrown as `UncheckedIOException`. |
| `ShaderSource.parse(ShaderStage stage, String code)` | Parses JOID GLSL code. Throws `IllegalArgumentException` for a vertex input or a fragment output. |

`BlendState` (`dev.joid.lib.bridge.render.state`) provides `NORMAL` (straight alpha), `PREMULTIPLIED`, `DISABLED`, and `create(...)` for custom equations and factors.

- `IShader` has no release method: create each shader once and reuse it.
- Create shaders on the render thread, after the backend is registered: the OpenGL backends need their context.

## Binding and drawing

| Method | Description |
| --- | --- |
| `bind()` | Makes the shader current for the following draw calls and applies its blending mode, remembering the previous one. |
| `unbind()` | Returns to the backend's default shader and restores the blending mode saved by `bind()`. |
| `isBound()` | Whether the shader is bound. |
| `isActive()` | Whether the shader compiled and linked. |

`unbind()` does not restore a custom shader that was bound before yours. To nest correctly (inside a shader pass, or inside a node drawn with an effect), save the current shader with `render.getShader()` before `bind()`, and restore it with `render.shader(previous)` after `unbind()`, in a `finally` block: `WaveNode` does it in [A first shader](#a-first-shader), and so do the built-in shaders. `IRenderBridge.shader(null)` selects the backend's default shader.

What the draw calls send to the shader:

- Vertices from the `Tessellator` (`dev.joid.lib.render.tessellator`) carry what you add: positions, texture coordinates (`addVertexWithUV`, `setTextureUV`), colors (`setColor`) and normals (`setNormal`). See [Transformations and Framebuffers](../drawing/transformations.md).
- Without vertex colors, `aColor` is the current render color (`IRenderBridge.color(...)`).
- The `DrawUtils` helpers set their own blending and texture state, and the ones that need a shader (rounded rectangles, circles, gradient colors) bind it for their call and then restore yours. Draw with the `Tessellator` when the shader needs texture coordinates.

## Uniforms

Get a uniform handle by name from the `IShader`, then set its value. In a `ShaderImpl` subclass:

```java
public void bind(final Color tint, final float[] transform) {
    super.bind();
    this.shader.getFloat4Uniform("u_Tint").setValue(tint.r, tint.g, tint.b, tint.a);
    this.shader.getFloatMatrixUniform("u_Transform").setValue(transform);
}
```

| Getter | Handle | `setValue(...)` | GLSL type |
| --- | --- | --- | --- |
| `getIntUniform(String)` | `IntUniform` | `int` | `int` |
| `getBooleanUniform(String)` | `BooleanUniform` | `boolean` | `bool` |
| `getFloatUniform(String)` | `FloatUniform` | `float` | `float` |
| `getFloat2Uniform(String)` | `Float2Uniform` | `float, float` | `vec2` |
| `getFloat3Uniform(String)` | `Float3Uniform` | `float, float, float` | `vec3` |
| `getFloat4Uniform(String)` | `Float4Uniform` | `float, float, float, float` | `vec4` |
| `getFloatArrayUniform(String)` | `FloatArrayUniform` | `float[]` | `float[N]` |
| `getFloat4ArrayUniform(String)` | `Float4ArrayUniform` | `float[]`, 4 values per element; another length throws `IllegalArgumentException("Invalid array size")` | `vec4[N]` |
| `getFloatMatrixUniform(String)` | `FloatMatrixUniform` | `float[]` of 4, 9 or 16 values, column by column; another length throws `IllegalArgumentException("Invalid matrix size")` | `mat2`, `mat3`, `mat4` |
| `getSamplerUniform(String)` | `SamplerUniform` | `ITexture, TextureFilter, TextureWrap` | `sampler2D` |

The handle types are in `dev.joid.lib.bridge.render.shader.uniform` and all extend `ShaderUniform`.

- Bind the shader before setting its uniforms: LWJGL 2 sends values to the shader currently in use. (LWJGL 3 uploads them at the next draw with this shader, Vulkan with every draw.)
- A value stays until you change it: set only what changes between draws.
- A name that the shader does not declare, or that the compiler removed because it is unused, is silently ignored.

## Textures and samplers

- A sampler that you do not assign reads the texture of the draw call: the one bound with `IRenderBridge.texture(...)` (for example by the resource drawing helpers, or the previous result in a [shader pass](pipeline.md#writing-a-shaderpass)), or a 1×1 white texture when none is bound. Most shaders declare a single `uniform sampler2D tex;` used this way.
- To read another texture, assign it: `shader.getSamplerUniform("u_Mask").setValue(texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE);`. The texture can come from a loaded `Resource` (`getTexture()`, `null` until loaded) or from a `FrameBuffer` (`getHandle().getTexture()`).
- `TextureFilter` is `NEAREST` or `LINEAR`; `TextureWrap` is `REPEAT`, `CLAMP_TO_EDGE` or `CLAMP_TO_BORDER` (`dev.joid.lib.bridge.render.texture`).

## Built-in shaders

The built-in shaders are `ShaderImpl` singletons in `dev.joid.lib.shader.impl`, used by the drawing helpers and the built-in shader passes. You can use them in your own drawing; each `use(...)` method binds the shader, runs the draw, then unbinds it, and does nothing at all (the draw is not run) when the shader is not available.

| Shader | Methods | Purpose |
| --- | --- | --- |
| `RoundedShader` | `inst()`; `static use(float radius, float x1, float y1, float x2, float y2, Runnable runnable)`; `bind(float radius, float x1, float y1, float x2, float y2)`; `bind(..., RoundedShaderType type)`; `gradient(ColorGradient gradient, Vector4f canvas)` | Rounded rectangle mask: `x1, y1, x2, y2` is the inner rectangle (the box minus the radius on each rounded side), in UI units. `use` restores the previous shader. `gradient(...)`, called after `bind`, multiplies the color by a gradient spread over `canvas` (`x1, y1, x2, y2`). |
| `CircleShader` | `inst()`; `static use(float radius, float centerX, float centerY, Runnable runnable)`; `bind(float radius, float centerX, float centerY)`; `bind(..., CircleShader.RoundedShaderType type)`; `gradient(ColorGradient gradient, Vector4f canvas)` | Circle mask in UI units. `use` restores the previous shader; its `runnable` may be `null`. |
| `BlurShader` | `inst()`; `bind(float radius, float dirX, float dirY, float texelW, float texelH)` | One-direction Gaussian blur of a texture: radius in pixels, direction `(1, 0)` or `(0, 1)`, texel size of the texture. |
| `BorderShader` | `inst()`; `bind(float borderWidth, Color borderColor, float texelW, float texelH, boolean fill, int mode, float rectX1, float rectY1, float rectX2, float rectY2)` | Border around the opaque shape of a texture: width in pixels, `mode` is `BorderShader.BorderMode.OUT.ordinal()` or `IN.ordinal()`, rectangle in UI units. Gradient colors are supported. |
| `GradientShader` | `inst()`; `static use(Vector2f startPos, Vector2f endPos, Color startColor, Color endColor, Runnable runnable, Vector4f canvas)`; `static use(..., boolean hasTexture, Runnable runnable, Vector4f canvas)` | Linear gradient from `startColor` at `startPos` to `endColor` at `endPos`, positions as fractions of `canvas` (`x1, y1, x2, y2`; raw UI coordinates when the canvas is empty), multiplied by the vertex color and, with `hasTexture`, by the texture. It unbinds after the draw without restoring a previous shader. |

`RoundedShader.RoundedShaderType` and `CircleShader.RoundedShaderType` (two separate enums with the same values) choose the source color: `AUTO` (texture × vertex color, the default), `TEXTURE` (texture only, premultiplied output, used by the pipeline passes) or `COLOR` (vertex color only).

In practice, prefer the higher-level APIs: `DrawUtils.SHAPE.drawRoundedRect(...)` and `drawCircle(...)` ([Shapes](../drawing/shapes.md)), gradient `Color`s ([Colors and Gradients](../styling/colors.md)) and the shader effects ([Effects](../styling/effects.md)).

## Reference

### IShader

`IShader` (`dev.joid.lib.bridge.render.shader`) is the compiled shader, created by the render bridge.

| Method | Description |
| --- | --- |
| `bind()`, `unbind()` | See [Binding and drawing](#binding-and-drawing). |
| `isBound()` | Bound since the last `bind()`. |
| `isActive()` | Compiled and linked successfully. |
| `getIntUniform`, `getBooleanUniform`, `getFloatUniform`, `getFloat2Uniform`, `getFloat3Uniform`, `getFloat4Uniform`, `getFloatArrayUniform`, `getFloat4ArrayUniform`, `getFloatMatrixUniform`, `getSamplerUniform` (`String name`) | Uniform handles, see [Uniforms](#uniforms). |

### ShaderSource

`ShaderSource` (`dev.joid.lib.bridge.render.shader.source`) is a parsed JOID GLSL stage. Backends translate it; you only create it with `read` or `parse`.

| Method | Description |
| --- | --- |
| `static read(ShaderStage stage, InputStream stream)`, `static parse(ShaderStage stage, String code)` | See [With IRenderBridge.createShader](#with-irenderbridge-createshader). |
| `getStage()` | `ShaderStage.VERTEX` or `ShaderStage.FRAGMENT`. |
| `getBody()` | The code, with the `#version` line and the recognized declarations replaced by empty lines. |
| `getBuiltins()` | Built-in variables used by the code (`Set<ShaderBuiltin>`). |
| `getInputs()` | Varyings read by a fragment shader (`List<ShaderVariable>`). |
| `getOutputs()` | Varyings written by a vertex shader. |
| `getUniforms()` | Uniforms other than samplers. |
| `getSamplers()` | Sampler uniforms. |

### ShaderVariable

| Method | Description |
| --- | --- |
| `getType()`, `getName()` | GLSL type and name. |
| `getArray()` | Array suffix without spaces (`"[16]"`), or `""`. |
| `isFlat()` | Declared `flat`. |
| `getDeclaration()` | Type, name and array suffix: `"float u_Values[16]"`. |
| `static create(String type, String name, String array, boolean flat)` | Creates a variable. |

### ShaderBuiltin

Enum of the [built-in variables](#built-in-variables): `POSITION`, `TEXTURE_COORDINATE`, `COLOR`, `NORMAL`, `PROJECTION_MATRIX`, `MODEL_VIEW_MATRIX`, `NORMAL_MATRIX`, `LIGHTING`, `FRAGMENT_COLOR`.

| Method | Description |
| --- | --- |
| `getIdentifier()` | GLSL name (`"aPosition"`...). |
| `getType()` | GLSL type (`"vec3"`...). |
| `getKind()` | `ShaderBuiltin.Kind.ATTRIBUTE`, `UNIFORM` or `OUTPUT`. |
| `static find(String identifier)` | The built-in with this identifier, as an `Optional`. |

## See also

- [Shader Pipeline](pipeline.md)
- [Custom Effects](../styling/custom-effects.md)
- [Custom Nodes](../nodes/custom-nodes.md)
- [Transformations and Framebuffers](../drawing/transformations.md)
- [Writing a Backend](../integration/writing-a-backend.md)
- [Backends](../integration/backends.md)