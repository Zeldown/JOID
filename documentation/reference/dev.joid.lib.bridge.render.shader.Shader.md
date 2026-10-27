---
summary: The base class of a shader of a render bridge, which translates the two stages, lays out the uniforms and compiles the program at the first use.
---

The constructor compiles nothing: `compileProgram` runs at the first `isActive()`, once the fields of the subclass are set.
With the dev jar, a stage that uses a feature beyond GLSL 1.20 prints a warning.

Learn: [Shaders](/build/writing-a-backend/#shaders), [Shader translation](/build/shader-translation/).

## Shader(RenderBridge,GlslShaderTranslator,ShaderSource,ShaderSource,BlendState)

Builds the uniform block and the samplers of the two stages and translates them with the translator of your API.
Turns on the emulation of `CLAMP_TO_BORDER` when the bridge refuses that wrap.

## bind()

Makes the shader and its blend state the current ones of the render state, remembering the previous blend state.

## unbind()

Removes the shader from the render state and puts the previous blend state back.

## isActive()

Compiles the program on the first call, then returns `isProgramActive()`.

## getLineShader()

Returns the variant that draws wide lines as triangles, created on first use, with the current uniform values and samplers copied into it.

## getSamplerMap()

Returns the samplers of the shader by name, read-only, each with its texture unit from `1`.

## getBlend()

Returns the blend state that `bind()` applies.

## getBridge()

Returns the render bridge.

## getVertex()

Returns the parsed vertex stage.

## getFragment()

Returns the parsed fragment stage.

## getBlock()

Returns the uniform block, which a subclass sends at each draw.

## getTranslation()

Returns the translated sources that `compileProgram` receives.

## isBound()

If `true`, the shader is bound.

## isCompiled()

If `true`, `compileProgram` has run.

## getPreviousBlend()

Returns the blend state that `unbind()` puts back, or `null` while unbound.

## delete()

Deletes the line variant, then the program when it was compiled.

## uniform(String,boolean)

Sets a `bool` uniform of the block.

## uniform(String,int[])

Sets an integer uniform of the block.

## uniform(String,float[])

Sets a float uniform of the block.

## sampler(String,ITexture,TextureFilter,TextureWrap)

Sets the texture of a sampler, with its filter and wrap.
Throws an `IllegalArgumentException` that lists the samplers when the shader declares no sampler of that name.

## builtins(RenderState,VertexBuffer,float[],MatrixStack)

Writes the built-in uniforms of a draw into the block: matrices, lighting, alpha test, line width and viewport, vertex colors, current color and border emulation; returns the block.

## isProgramActive()

JOID calls it from `isActive()`.
Return `true` when the program compiled and links.

## compileProgram(ShaderTranslation)

JOID calls it once, at the first `isActive()`.
Compile and link the translated sources; on failure, print one warning that starts with `[JOID]` and leave the program inactive, without throwing.

## deleteProgram()

JOID calls it from `delete()` when the program was compiled.
Release the program.
