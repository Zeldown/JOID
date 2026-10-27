---
summary: The translated sources of a shader, ready for the graphics API, the vertex and fragment stages and the optional stencil write stage.
---

[[GlslShaderTranslator#translate]] returns it, and the constructor of [[Shader]] hands it to the `compileProgram` hook of the backend.

Learn: [Shader translation](/build/shader-translation/#from-source-to-program).

## of(GlslDialect,String,String,String)

Creates a translation from a dialect, the vertex and fragment sources, and the stencil fragment source, or `null` when the shader has no stencil write stage.

## getDialect()

Returns the dialect of the sources, which tells the backend how to compile and link them, such as whether it binds the attribute locations itself.

## getVertex()

Returns the complete source of the vertex stage.

## getFragment()

Returns the complete source of the fragment stage.

## getStencilFragment()

Returns the fragment stage of the stencil write pass, which outputs the updated stencil value; `null` without stencil emulation.
