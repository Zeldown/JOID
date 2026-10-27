---
summary: A variable that a shader source declares, an input, an output, a uniform or a sampler, as the translator reads and writes it again.
---

[[ShaderSource]] creates one per `in`, `out` and `uniform` declaration, and [[GlslShaderTranslator]] creates its own for the uniforms that JOID adds.
The translator declares them again in the syntax of the dialect, and [[UniformBlock]] lays out the uniforms from them.

Learn: [Shader translation](/build/shader-translation/#from-source-to-program).

## of(String,String,String,boolean)

Creates a variable from its GLSL type, its name, its array suffix (such as `[4]` or `[COUNT]`, empty for a single value) and whether it is `flat`.

## getDeclaration()

Returns the type, the name and the array suffix, as written in a declaration without qualifier nor semicolon, such as `float weights[3]`.

## getType()

Returns the GLSL type, such as `vec4` or `sampler2D`.

## getName()

Returns the name used in the source.

## getArray()

Returns the array suffix without spaces, such as `[3]`; empty for a variable that is not an array.

## isFlat()

If `true`, the varying is declared `flat` and is not interpolated.
