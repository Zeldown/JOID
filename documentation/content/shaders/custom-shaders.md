# Custom Shaders

A JOID shader is written once, in JOID GLSL, and each backend translates it when it loads: GLSL 1.10 to 3.30 on LWJGL 2 and LWJGL 3 depending on the OpenGL context (3.30 from OpenGL 3.3), GLSL 4.50 compiled to SPIR-V on Vulkan. Write one when the drawing helpers and the effects cannot draw what you need; bind it around your draw calls in a draw hook, or use it in a pass of the [Shader Pipeline](pipeline.md) to post-process a node.

## A first shader

A shader is a pair of files, a vertex shader and a fragment shader, usually in your resources. This one paints moving stripes with the vertex color.

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
	float wave = 0.5 + 0.5 * sin(vTexCoord.x * 24.0 + u_Time * 3.0);
	fragColor = vec4(vColor.rgb * (0.6 + 0.4 * wave), vColor.a);
}
```

A `ShaderProgram` subclass loads the pair and sets the uniforms:

```java
public class WaveShader extends ShaderProgram {

	private static final WaveShader INSTANCE = new WaveShader();

	private WaveShader() {
		super.load(WaveShader.class.getResourceAsStream("/assets/myui/shaders/wave.vsh"), WaveShader.class.getResourceAsStream("/assets/myui/shaders/wave.fsh"));
	}

	public static @NonNull WaveShader inst() {
		return WaveShader.INSTANCE;
	}

	public void bind(final float time) {
		super.bind();
		super.getShader().uniform("u_Time", time);
	}

}
```

A [custom node](../nodes/custom-nodes.md) binds it around its draw calls:

```java
public class WaveNode extends Node {

	protected WaveNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull WaveNode create(final double x, final double y, final double width, final double height) {
		return new WaveNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final WaveShader shader = WaveShader.inst();
		if (!shader.canDraw()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IShader previous = render.getShader();
		shader.bind(BridgeHandler.CLOCK.get().currentTimeMillis() % 60000L / 1000F);
		try {
			final Tessellator tessellator = Tessellator.inst();
			tessellator.start(DrawMode.QUADS);
			tessellator.setColor(221, 221, 221, 255);
			tessellator.addVertexWithUV(super.getX(), super.getY() + super.getHeight(), 0D, 0D, 1D);
			tessellator.addVertexWithUV(super.getX() + super.getWidth(), super.getY() + super.getHeight(), 0D, 1D, 1D);
			tessellator.addVertexWithUV(super.getX() + super.getWidth(), super.getY(), 0D, 1D, 0D);
			tessellator.addVertexWithUV(super.getX(), super.getY(), 0D, 0D, 0D);
			tessellator.draw();
		} finally {
			shader.unbind();
			render.shader(previous);
		}
	}

}
```

```java
WaveNode.create(100, 100, 600, 160).attach(this);
```

![A light gray rectangle crossed by soft vertical stripes of darker gray](../images/shader-wave.png "WaveShader on a quad, at one moment of its animation")

`load(...)` only reads the sources: the shader is compiled through the render bridge the first time it is used (`bind()`, `canDraw()`, `isAvailable()` or `getShader()`), on the render thread. A static instance such as `WaveShader.INSTANCE` can therefore be created anywhere, even before the backend is registered.

## Loading a shader with ShaderProgram

`ShaderProgram` (`dev.joid.lib.shader.impl`) is the base class of the built-in shaders and the simplest way to write one, as in [A first shader](#a-first-shader).

- `load(Object vertexShader, Object fragmentShader)` reads both sources as JOID GLSL (UTF-8) from any [asset handle](../resources/assets.md) (an `InputStream`, a `File`, a URL `String`, or the handle of a locator you registered, such as a resource of a game) and closes what it opened. On any exception it prints `[JOID] Unable to load the shader <ClassName>: <message>` and the stack trace, and the shader stays unavailable.
- `getShader()` creates the shader through the render bridge with `BlendState.NORMAL` on its first call, then returns it; `null` when the sources could not be read or the creation threw (`[JOID] Unable to create the shader <ClassName>: <message>`, printed once).
- `canDraw()` tells whether the shader can draw and, when it cannot, prints `[JOID] The shader <ClassName> is unavailable, what it draws is skipped` once in dev mode. Call it before drawing something that is skipped without the shader, as `WaveNode` does.
- `isAvailable()` gives the same answer without any warning. Call it to choose a fallback (draw without the shader when it is unavailable).

A shader that failed to load must not break the UI: skip what it draws, or draw a fallback.

## Loading a shader with IRenderBridge.createShader

To create a shader from strings, or with another blending mode, call the render bridge directly:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
final IShader shader = render.createShader(ShaderSource.parse(ShaderStage.VERTEX, this.vertexCode), ShaderSource.parse(ShaderStage.FRAGMENT, this.fragmentCode), BlendState.PREMULTIPLIED);
if (!shader.isActive()) {
	System.err.println("[MyUI] The shader did not compile");
}
```

- `createShader(ShaderSource vertex, ShaderSource fragment, BlendState blend)` translates, compiles and links the pair. `blend` is the blending mode applied while the shader is bound. Compile errors do not throw: the shader is returned inactive.
- `ShaderSource.read(ShaderStage stage, Object handle)` reads an asset handle (UTF-8), closes it and parses it; an `IOException` is rethrown as `UncheckedIOException`. `ShaderSource.parse(ShaderStage stage, String code)` parses code.
- `BlendState` (`dev.joid.lib.bridge.render.state`) provides `NORMAL` (straight alpha), `PREMULTIPLIED`, `DISABLED`, and `create(...)` for custom equations and factors.
- `IShader` has no release method: create each shader once and reuse it. `createShader` runs on the render thread, after the backend is registered: the OpenGL backends need their context. A `ShaderProgram` has no such constraint, since it creates its shader on first use.

## Binding and drawing

`bind()` makes the shader current for the following draw calls and applies its blending mode, remembering the previous one; `unbind()` returns to the default shader of the backend and restores that blending mode. `unbind()` does not restore a custom shader bound before yours: to nest correctly (inside a shader pass, or inside a node drawn with an effect), save `render.getShader()` before `bind()` and restore it with `render.shader(previous)` after `unbind()`, in a `finally` block, as `WaveNode` does. `IRenderBridge.shader(null)` selects the default shader.

What the draw calls send to the shader:

- Vertices from the `Tessellator` carry what you add: positions, texture coordinates (`addVertexWithUV`, `setTextureUV`), colors (`setColor`) and normals (`setNormal`). See [Building geometry with Tessellator](../drawing/transformations.md#building-geometry-with-tessellator).
- Without vertex colors, `aColor` is the current render color (`IRenderBridge.color(...)`).
- The `DrawUtils` helpers set their own blending and texture state and restore yours afterwards; the ones that need a shader (rounded rectangles, circles, gradient colors) bind it for their call and then restore yours. A rectangle or an image drawn under a rotation while your shader is bound fades its edges through the vertex alpha: multiply by `aColor` in your shader.
- Text unbinds the current shader: draw text outside your binding.

## Setting uniforms

Set a uniform by name on the `IShader` with `uniform(name, values...)`. Each call returns the shader, so the values of a draw chain. In a `ShaderProgram` subclass:

```java
public void bind(final Color tint, final float[] transform) {
	super.bind();
	super.getShader()
	.uniform("u_Tint", tint.r, tint.g, tint.b, tint.a)
	.uniform("u_Transform", transform);
}
```

The shader knows the type of each uniform from its declaration, so one method takes every type:

| Call | GLSL type |
|---|---|
| `uniform(String, int)` | `int`, `bool` |
| `uniform(String, boolean)` | `bool`, `int` (1 or 0) |
| `uniform(String, float)` | `float` |
| `uniform(String, float, float)` | `vec2` |
| `uniform(String, float, float, float)` | `vec3` |
| `uniform(String, float, float, float, float)` | `vec4` |
| `uniform(String, float[])` | `mat2`, `mat3`, `mat4` (4, 9 or 16 values, column by column) |
| `uniform(String, float[])` | `float[N]`, `vec2[N]`, `vec3[N]`, `vec4[N]`, `mat4[N]`... (whole elements, from the first one, at most `N`) |

- A value is kept by the shader and sent at its next draw, on every backend: set it before or after `bind()`, even while another shader is bound.
- A value stays until you change it: set only what changes between draws. A value equal to the current one sends nothing.
- A value that does not fit the declaration throws an `IllegalArgumentException` that names it, such as `The uniform vec3 u_Tint cannot take 4 floats` or `The uniform float u_Radius cannot take an int`. Write float literals with `F`: `uniform("u_Radius", 4)` passes an int.
- A name that the shader does not declare throws `IllegalArgumentException: The shader declares no uniform <name>`.

## Textures and samplers

- A sampler that you do not assign reads the texture of the draw call: the one bound with `IRenderBridge.texture(...)` (for example by the resource drawing helpers, or the previous result in a [shader pass](pipeline.md#writing-a-shaderpass)), or a 1×1 white texture when none is bound. Most shaders declare a single `uniform sampler2D tex;` used this way.
- To read another texture, assign it: `shader.sampler("u_Mask", texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE);`. A name that the shader does not declare as a sampler throws `IllegalArgumentException: The shader declares no sampler <name>`. The texture can come from a loaded `Resource` (`getTexture()`, `null` until loaded) or from a `FrameBuffer` (`getHandle().getTexture()`).
- `TextureFilter` is `NEAREST` or `LINEAR`; `TextureWrap` is `REPEAT`, `CLAMP_TO_EDGE` or `CLAMP_TO_BORDER` (`dev.joid.lib.bridge.render.texture`).

## JOID GLSL

JOID GLSL is GLSL without the parts that differ between backends: JOID declares the vertex attributes, the matrices and the output for you, and each backend generates its own declarations from yours.

![A JOID GLSL file parsed into a ShaderSource, then translated by each backend: GLSL 1.10 to 3.30 on OpenGL with LWJGL 2 and LWJGL 3, the highest dialect the context compiles, and GLSL 4.50 compiled to SPIR-V on Vulkan](../images/diagram-shader-translation.png "One source, one translation per graphics API")

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
|---|---|---|---|---|
| `aPosition` | `vec3` | attribute | vertex | Vertex position, in the coordinates given to the draw call (canvas units of the current drawing space). |
| `aTexCoord` | `vec2` | attribute | vertex | Vertex texture coordinates. |
| `aColor` | `vec4` | attribute | vertex | Vertex color, or the current render color when the vertices have none. |
| `aNormal` | `vec3` | attribute | vertex | Vertex normal (3D models). |
| `uProjectionMatrix` | `mat4` | uniform | vertex, fragment | Current projection matrix. |
| `uModelViewMatrix` | `mat4` | uniform | vertex, fragment | Current model-view matrix. |
| `uNormalMatrix` | `mat3` | uniform | vertex, fragment | Normal matrix of the model-view matrix. |
| `uLighting` | `bool` | uniform | vertex, fragment | Whether lighting is enabled on the render bridge, read at each draw. |
| `fragColor` | `vec4` | output | fragment | Output color. |

The backend fills the built-in uniforms. The standard vertex transformation is `gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);`.

### Writing portable GLSL

Your code is compiled as the highest of GLSL 1.10, 1.20, 1.30, 1.40, 1.50 and 3.30 that the OpenGL context compiles on LWJGL 2 and LWJGL 3, and as GLSL 4.50 on Vulkan. To run on every backend and every OpenGL context, keep the code within what these versions share:

- GLSL 1.20 features only: no `%` or bitwise operators on integers, no `uint`, no `switch`, no `texelFetch` or `textureSize`, no `flat` varyings. LWJGL 2 and LWJGL 3 on a context below GLSL 1.30 refuse a shader that uses one of them, with a message naming the feature and the GLSL version it needs (`The shader uses unsigned integers, which needs GLSL 1.30, but the dialect is GLSL 1.20`): `createShader` throws `UnsupportedOperationException` and `ShaderProgram` prints it. So that you see it on your own machine, whatever its backend, every shader created in [dev mode](../concepts/dev-tools.md) is checked against GLSL 1.20, once per shader and feature: `[JOID] A fragment shader uses flat varyings, which needs GLSL 1.30: the OpenGL 2.1 contexts (GLSL 1.20) that JOID supports refuse it, keep to GLSL 1.20 to draw on every backend`.
- On an OpenGL 2.0 context, LWJGL 2 and LWJGL 3 compile GLSL 1.10, which converts no integer to a float. The translator does it for you there: an integer literal used as a float (`vec4(1)`, `x * 2`, `pow(x, 2)`) is written `1.0`, while the integers of integer expressions stay (array indices, `int`, `ivec` and `bool` variables, uniforms and functions, `for (int i = 0; i < 4; i++)`). An integer argument of a function of yours that takes an `int` is not recognized: write it from an `int` variable.
- `texture(...)` to sample (below GLSL 1.30 it is defined to `texture2D`), never `texture2D`, `gl_FragColor`, `attribute` or `varying`.

### What the backends generate

| Backend | Generated header |
|---|---|
| LWJGL 2, LWJGL 3 | The `#version` of the dialect of the context (`#version 330 core` from OpenGL 3.3); the built-in uniforms and every uniform of both stages as plain `uniform`s; the built-in attributes, at fixed locations; your samplers. From GLSL 1.30, `fragColor` as an `out vec4` (with `layout(location = 0)` in 3.30) and your varyings as `in` / `out`, with `flat`; below, `attribute` and `varying`, with `texture` and `fragColor` defined to `texture2D` and `gl_FragColor`. |
| Vulkan | `#version 450`; the same `JoidUniforms` block, at binding 0; built-in attributes at fixed locations (vertex stage); `layout(location = 0) out vec4 fragColor` (fragment stage); one binding per sampler, from 1; varyings at locations matched by name. |

A uniform declared in both stages is a single value: give it the same type in both. A sampler declared in both stages is a single sampler as well.

### Lines under a shader

A line drawn while your shader is bound, smooth or wider than 1 pixel (`drawLine`, `drawCurvedLine`, `drawDashedLine`, a line mode of `drawShape` with `lineWidth`), is expanded into triangles and drawn with the line variant of your shader, `getLineShader()`. The translator of the core writes it from your two stages:

- your vertex `main` runs twice, once on the other end of the segment and once on the vertex, and the resulting `gl_Position` is pushed sideways by half the line width in window pixels;
- your fragment `main` runs as is, then the alpha of `fragColor` is multiplied by the coverage of the line, which antialiases its edges and ends;
- the uniforms and samplers you set on your shader are given to the variant before each line, and its blend state is yours: keep a blend such as `BlendState.NORMAL` for smooth edges.

On these vertices `aPosition` and `aColor` keep their values, `aTexCoord` is `(0, 0)` and `aNormal` is `(0, 0, 1)`: a shader that colors lines reads the position or the color, not the texture coordinates. Your shader stays the one bound: `getShader()` returns it.

### Reserved names

- Identifiers starting with `joid_` and the block name `JoidUniforms` are generated by the backends (`joid_main`, `joid_body`, `joid_AlphaTest`, `joid_LineWidth`, `joid_Position`...): do not use them.
- `texture` is a macro below GLSL 1.30: do not use it as a variable name.
- On every backend, the fragment shader's `void main()` is renamed `joid_main()` and wrapped by a `main()` that applies the alpha test of the render state (`IRenderBridge.alphaTest(...)`).

### Errors and line numbers

Declarations and the `#version` line are replaced by empty lines and the backends insert a `#line` directive after their header, so compiler messages give the line numbers of your own file. The backends print compile and link errors to `System.err` (for example `Fragment shader compilation failed: ...` on LWJGL, `Vulkan fragment shader compilation failed: ...` on Vulkan), and the shader is then inactive (`IShader.isActive()` returns `false`).

## Built-in shaders

The built-in shaders are `ShaderProgram` singletons in `dev.joid.lib.shader.impl`, used by the drawing helpers and the built-in shader passes. Each static `use(...)` binds the shader, runs the draw, unbinds it and restores the shader bound before; when the shader is not available, it does nothing at all (the draw is not run) and warns once in dev mode.

| Shader | Methods | Purpose |
|---|---|---|
| `RoundedShader` | `inst()`; `static use(float radius, float x1, float y1, float x2, float y2, Runnable runnable)`; `bind(float radius, float x1, float y1, float x2, float y2)`; `bind(..., RoundedShaderType type)`; `stroke(float stroke)`; `aligned(boolean aligned)`; `gradient(ColorGradient gradient, Vector4f canvas)` | Rounded rectangle mask: `x1, y1, x2, y2` is the inner rectangle (the box minus the radius on each rounded side), in canvas units. `stroke(...)` keeps only an outline of that width inside the edge; `aligned(false)` smooths the edges for a rotated grid. Each `bind` sets the stroke back to `0F` and `aligned` back to `true`. `gradient(...)`, called after `bind`, multiplies the color by a gradient spread over `canvas` (`x1, y1, x2, y2`). |
| `CircleShader` | `inst()`; `static use(float radius, float centerX, float centerY, Runnable runnable)`; `bind(float radius, float centerX, float centerY)`; `bind(..., RoundedShaderType type)`; `gradient(ColorGradient gradient, Vector4f canvas)` | Circle mask in canvas units. The `runnable` of `use` may be `null`. |
| `ShadowShader` | `inst()`; `static use(float radius, float blur, float x1, float y1, float x2, float y2, Runnable runnable)`; `bind(float radius, float blur, float x1, float y1, float x2, float y2)` | Soft shadow of the rounded box `x1, y1, x2, y2`, as drawn by `drawShadow`. |
| `BlurShader` | `inst()`; `bind(float radius, float dirX, float dirY, float texelW, float texelH)` | One-direction Gaussian blur of a texture: radius in pixels, direction `(1, 0)` or `(0, 1)`, texel size of the texture. |
| `BorderShader` | `inst()`; `bind(float borderWidth, Color borderColor, float texelW, float texelH, boolean fill, int mode, float rectX1, float rectY1, float rectX2, float rectY2)` | Border around the opaque shape of a texture: width in pixels, `mode` is `BorderShader.BorderMode.OUT.ordinal()` or `IN.ordinal()`, rectangle in canvas units. Gradient colors are supported. |
| `GradientShader` | `inst()`; `static use(Vector2f startPos, Vector2f endPos, Color startColor, Color endColor, Runnable runnable, Vector4f canvas)`; `static use(..., boolean hasTexture, Runnable runnable, Vector4f canvas)` | Linear gradient from `startColor` at `startPos` to `endColor` at `endPos`, positions as fractions of `canvas` (`x1, y1, x2, y2`; raw canvas coordinates when the canvas is empty), multiplied by the vertex color and, with `hasTexture`, by the texture. |

`RoundedShaderType` (`dev.joid.lib.shader.impl`) chooses the source color of `RoundedShader` and `CircleShader`: `AUTO` (texture × vertex color, the default), `TEXTURE` (texture only, premultiplied output, used by the pipeline passes) or `COLOR` (vertex color only).

```java
GradientShader.use(new Vector2f(0F, 0F), new Vector2f(1F, 0F), Color.RED, Color.YELLOW, () -> DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE), new Vector4f((float) super.getX(), (float) super.getY(), (float) (super.getX() + super.getWidth()), (float) (super.getY() + super.getHeight())));
```

In practice, prefer the higher-level APIs: `DrawUtils.SHAPE.drawRoundedRect(...)` and `drawCircle(...)` ([Shapes](../drawing/shapes.md)), gradient `Color`s ([Colors and Gradients](../styling/colors.md)) and the shader effects ([Effects](../styling/effects.md)).

## Reference

### ShaderProgram

| Member | Description |
|---|---|
| `protected void load(Object vertexShader, Object fragmentShader)` | Reads both sources from asset handles and closes them. On any exception, prints `[JOID] Unable to load the shader <ClassName>: <message>` and the stack trace. A `null` handle throws a `NullPointerException`. |
| `IShader getShader()` | The shader, created with `BlendState.NORMAL` on the first call; `null` when the sources could not be read or the creation failed. |
| `void bind()` | Binds the shader if it is available, otherwise does nothing. |
| `void unbind()` | Unbinds the shader if it was created. |
| `boolean canDraw()` | `true` when the shader can draw; otherwise `false` and, in dev mode, one warning per shader. |
| `boolean isAvailable()` | `true` when the shader is created and compiled (`getShader() != null && getShader().isActive()`), without warning. |

### IShader

`IShader` (`dev.joid.lib.bridge.render.shader`) is the compiled shader, created by the render bridge.

| Method | Description |
|---|---|
| `bind()` | Makes the shader current and applies its blending mode, remembering the previous one. |
| `unbind()` | Returns to the default shader and restores the blending mode saved by `bind()`. |
| `isBound()` | Bound by `bind()` and not unbound yet. |
| `isActive()` | Compiled and linked successfully. |
| `uniform(String name, int value)`, `uniform(String name, boolean value)`, `uniform(String name, float... values)` | Sets a uniform and returns the shader, see [Setting uniforms](#setting-uniforms). |
| `sampler(String name, ITexture texture, TextureFilter filter, TextureWrap wrap)` | Assigns a texture to a sampler and returns the shader, see [Textures and samplers](#textures-and-samplers). |

### ShaderSource

`ShaderSource` (`dev.joid.lib.bridge.render.shader.source`) is a parsed JOID GLSL stage. Backends translate it; you only create it with `read` or `parse`.

| Method | Description |
|---|---|
| `static read(ShaderStage stage, InputStream stream)`, `static parse(ShaderStage stage, String code)` | See [Loading a shader with IRenderBridge.createShader](#loading-a-shader-with-irenderbridgecreateshader). |
| `getStage()` | `ShaderStage.VERTEX` or `ShaderStage.FRAGMENT`. |
| `getBody()` | The code, with the `#version` line and the recognized declarations replaced by empty lines. |
| `getBuiltins()` | Built-in variables used by the code (`Set<ShaderBuiltin>`). |
| `getInputs()` | Varyings read by a fragment shader (`List<ShaderVariable>`). |
| `getOutputs()` | Varyings written by a vertex shader. |
| `getUniforms()` | Uniforms other than samplers. |
| `getSamplers()` | Sampler uniforms. |
| `getFeatures()` | The `ShaderFeature`s used beyond GLSL 1.10 (`FLAT_VARYINGS`, `UNSIGNED_INTEGERS`, `BITWISE_OPERATORS`, `SWITCH`, `TEXEL_FETCH`, `TEXTURE_SIZE`, `DERIVATIVES`). |

### ShaderVariable

| Method | Description |
|---|---|
| `getType()`, `getName()` | GLSL type and name. |
| `getArray()` | Array suffix without spaces (`"[16]"`), or `""`. |
| `isFlat()` | Declared `flat`. |
| `getDeclaration()` | Type, name and array suffix: `"float u_Values[16]"`. |
| `static create(String type, String name, String array, boolean flat)` | Creates a variable. |

### ShaderBuiltin

Enum of the [built-in variables](#built-in-variables): `POSITION`, `TEXTURE_COORDINATE`, `COLOR`, `NORMAL`, `PROJECTION_MATRIX`, `MODEL_VIEW_MATRIX`, `NORMAL_MATRIX`, `LIGHTING`, `FRAGMENT_COLOR`.

| Method | Description |
|---|---|
| `getIdentifier()` | GLSL name (`"aPosition"`...). |
| `getType()` | GLSL type (`"vec3"`...). |
| `getKind()` | `ShaderBuiltin.Kind.ATTRIBUTE`, `UNIFORM` or `OUTPUT`. |
| `static find(String identifier)` | The built-in with this identifier, or `null`. |

## Pitfalls

- Restore the previous shader after `unbind()` (`render.shader(previous)`): `unbind()` alone selects the default shader and breaks an enclosing pass or effect.
- `getShader()` is `null` when the shader failed to load: check `canDraw()` or `isAvailable()` before setting uniforms.
- A declared uniform that the code does not use takes its value without error: a value that seems to have no effect may be unused in the code.
- `uniform("u_Radius", 4)` passes an int and throws on a `float` uniform: write `4F`.
- GLSL that works on one backend can fail on another: keep to the features of GLSL 1.20 (dev mode warns about the others) and test on every backend you ship.

## See also

- Next: [Bridges](../integration/bridges.md)
- [Shader Pipeline](pipeline.md)
- [Custom Effects](../styling/custom-effects.md)
- [Custom Nodes](../nodes/custom-nodes.md)
- [Transformations and Framebuffers](../drawing/transformations.md)
- [Writing a Backend](../integration/writing-a-backend.md)