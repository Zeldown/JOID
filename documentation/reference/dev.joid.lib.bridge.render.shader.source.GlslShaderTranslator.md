---
summary: Rewrites the JOID GLSL stages of a shader into the dialect of a backend, with the declarations, uniforms and wrappers that JOID adds around the body.
---

A translator turns a vertex and fragment pair of [[ShaderSource]] into complete sources for one [[GlslDialect]] and one [[UniformLayout]], with the body numbered from line 1.
A backend passes one to the constructor of [[Shader]], which translates both stages once; it extends this class when its compiler expects other declarations, as Vulkan adds `binding` and `location` qualifiers.
A stage that uses a [[ShaderFeature]] the dialect lacks throws `UnsupportedOperationException` with the feature and the version it needs.

Learn: [Shader translation](/build/shader-translation/#what-the-translator-adds).

## GlslShaderTranslator(GlslDialect,UniformLayout)

Creates a translator for a dialect and a uniform layout.
Throws `IllegalArgumentException` when the layout is `BLOCK` and the dialect has no uniform blocks.

## clampToBorder

If `true`, each `texture(sampler, ...)` call samples through a function driven by the `vec3` uniform `joid_Border_<sampler>`, which returns transparent black outside the texture for a sampler in `CLAMP_TO_BORDER` mode, with an edge that fades over half a texel for a `LINEAR` filter.
The constructor of [[Shader]] turns it on when the render bridge cannot wrap to `CLAMP_TO_BORDER`.

## stencil

Stencil emulation of the fragment stage: `NONE` adds nothing, `TEST` discards the fragments that fail the stencil comparison, `WRITE` outputs the updated stencil value instead of a color.
Any value but `NONE` declares the stencil uniforms and sampler, adds the `WRITE` stage to `translate`, and needs GLSL 1.30 or ESSL 3.00.

## translateVertex(ShaderSource,ShaderSource)

Returns the translated vertex stage.
It reads the fragment source too, so that both stages declare the same uniforms and the same varying locations.

## translateFragment(ShaderSource,ShaderSource)

Returns the translated fragment stage, with the stencil emulation set by `stencil`.

## translate(ShaderSource,ShaderSource)

Translates both stages into a [[ShaderTranslation]].
With a stencil emulation, the translation also carries the fragment stage of the `WRITE` pass; otherwise its stencil fragment is `null`.

## createBlock(ShaderSource,ShaderSource)

Returns the [[UniformBlock]] of the translated uniforms of both stages, with their std140 offsets.

## getUniforms(ShaderSource,ShaderSource)

Lists the uniforms of the translated program in block order: the built-in uniforms that a stage uses, the uniforms that JOID adds, then those of the vertex and the fragment stages, each name once.
The samplers are not in the list.

## getSamplers(ShaderSource,ShaderSource)

Lists the samplers of the vertex then the fragment stage, each name once, then `joid_Stencil` under a stencil emulation; a sampler takes the unit of its place in the list, from `1`.

## getDialect()

Returns the dialect that the translator writes.

## getLayout()

Returns the uniform layout of the translated program.

## getBlockLayout()

Hook that returns the qualifiers written in `layout(...)` before the uniform block: `std140` by default.
Override it to add a binding, as Vulkan does with `std140, binding = 0`.

## declareSampler(ShaderVariable,int)

Hook that declares a sampler; `unit` is its texture unit, from `1`.
Override it to add a binding qualifier.

## declareAttribute(ShaderBuiltin,int)

Hook that declares a vertex attribute at its location from [[VertexAttribute]]: `layout(location = n) in` when the dialect has explicit locations, `in` or `attribute` otherwise.

## declareVarying(ShaderVariable,int,boolean)

Hook that declares a varying: `output` is `true` in the vertex stage and `false` in the fragment stage, and `location` is the same in both stages.
Override it to add a location qualifier.

## BLOCK

Name of the uniform block that holds every uniform under `UniformLayout.BLOCK`.

## ALPHA_TEST

Name of the `int` uniform that turns on the alpha test: when it is not `0`, the fragment stage discards the fragments whose alpha is at most `ALPHA_THRESHOLD`.
[[Shader#builtins]] writes it from the alpha cutoff of the render state.

## ALPHA_THRESHOLD

Name of the `float` uniform that holds the alpha cutoff of the render state, written by [[Shader#builtins]].

## STENCIL

Name of the sampler that holds the stencil values, one byte in the red channel, read at the position of the fragment.
The backend binds it for the `TEST` and `WRITE` passes.

## STENCIL_TEST

Name of the `int` uniform that turns on the stencil comparison of the `TEST` pass, written by [[StencilEmulation#write]] with the other stencil uniforms.

## STENCIL_MASK

Name of the `int` uniform that holds the mask applied to the reference and to the stored value before the comparison.

## STENCIL_FAIL

Name of the `int` uniform that holds the operation of the `WRITE` pass when the comparison fails, as the ordinal of [[StencilOperation]].

## STENCIL_PASS

Name of the `int` uniform that holds the operation of the `WRITE` pass when the comparison passes, as the ordinal of [[StencilOperation]].

## STENCIL_FUNCTION

Name of the `int` uniform that holds the comparison, as the ordinal of [[StencilFunction]].

## STENCIL_REFERENCE

Name of the `int` uniform that holds the reference of the comparison and of the `REPLACE` operation.

## LINE_WIDTH

Name of the `float` uniform of a line shader that holds the line width of the render state, in screen pixels.

## LINE_VIEWPORT

Name of the `vec2` uniform of a line shader that holds the size of the viewport in pixels, which turns the width of a line into clip space.

## VERTEX_COLOR

Name of the `int` uniform that tells whether the vertex buffer carries colors: when it is `0`, every read of `aColor` gives `CURRENT_COLOR`.

## CURRENT_COLOR

Name of the `vec4` uniform that holds the current color of the render state, for the vertices without a color.

## BORDER

Prefix of the `vec3` uniform of each sampler under `clampToBorder`, followed by the name of the sampler.
[[Shader#builtins]] writes the mode (`0` off, `1` nearest, `2` linear), then the width and the height of the texture.
