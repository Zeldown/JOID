---
summary: A GLSL or GLSL ES version that the shader translator writes, with the language features that each version allows.
---

A backend picks one dialect for its [[GlslShaderTranslator]]: the translator writes the version line, the precision statements and the declarations of that dialect, and refuses a source that uses a [[ShaderFeature]] the dialect lacks.
The shaders of JOID keep to GLSL 1.20, which every OpenGL 2.1 context compiles.

Backend note: the OpenGL backends pick the highest desktop dialect that the context reports, up to `GLSL_330`; Vulkan uses `GLSL_450`, compiled to SPIR-V.

Learn: [Pick a dialect](/build/shader-translation/#pick-a-dialect).

## supports(ShaderFeature)

If `true`, a shader in this dialect may use the feature: the version reaches its first desktop or ES version.

## hasInputOutputs()

If `true`, the dialect declares varyings with `in` and `out` and writes the color to an output (GLSL 1.30, ESSL 3.00 and later); otherwise the translator writes `attribute`, `varying`, `gl_FragColor` and `texture2D`.

## hasUniformBlocks()

If `true`, the dialect has uniform blocks, so the translator accepts `UniformLayout.BLOCK` (GLSL 1.40, ESSL 3.00 and later).

## hasExplicitLocations()

If `true`, the translator declares the attributes and the fragment output with `layout(location = n)` (GLSL 3.30, ESSL 3.00 and later); otherwise the backend binds the attribute locations when it links the program.

## hasImplicitConversions()

If `true`, the compiler converts integer literals to floats where a float is expected (desktop GLSL 1.20 and later); otherwise the translator rewrites them with [[ImplicitConversion]].

## getName()

Returns the name used in messages, such as `GLSL 3.30` or `ESSL 1.00`.

## getPrecision()

Returns the precision statements written after the version line: none for desktop GLSL, `highp` for ESSL 3.00, and `highp` when the fragment stage has it, else `mediump`, for ESSL 1.00.

## getLineDirective()

Returns the `#line` directive written before the body, so that the line numbers of a compiler error are those of the source file; its value depends on how the version counts lines.

## getVersion()

Returns the number of the `#version` line, such as `330`.

## isEs()

If `true`, the dialect is GLSL ES, for OpenGL ES and WebGL.

## getDeclaration()

Returns the `#version` line written at the top of each translated stage, such as `#version 330 core`.

## GLSL_110

GLSL 1.10, OpenGL 2.0: no implicit conversion of integer literals.

## GLSL_120

GLSL 1.20, OpenGL 2.1, the version that every shader of JOID keeps to.

## GLSL_130

GLSL 1.30, OpenGL 3.0: `in` and `out`, `flat` varyings, unsigned integers, bitwise operators, `switch`, `texelFetch` and `textureSize`.

## GLSL_140

GLSL 1.40, OpenGL 3.1: uniform blocks.

## GLSL_150

GLSL 1.50, OpenGL 3.2.

## GLSL_330

GLSL 3.30 core, OpenGL 3.3: explicit locations.

## GLSL_450

GLSL 4.50, the dialect that the Vulkan backend compiles to SPIR-V.

## ESSL_100

GLSL ES 1.00, OpenGL ES 2.0 and WebGL 1.

## ESSL_300

GLSL ES 3.00, OpenGL ES 3.0 and WebGL 2.
