---
summary: How the translator declares a built-in name, as a vertex attribute, a uniform or the fragment output.
---

[[GlslShaderTranslator]] reads the kind of each [[ShaderBuiltin]] to decide where it goes in the translated source.

Learn: [Shader translation](/build/shader-translation/#from-source-to-program).

## ATTRIBUTE

An input of the vertex stage, one value per vertex, declared at the location of its [[VertexAttribute]].

## UNIFORM

A value that stays the same for a whole draw, declared with the other uniforms of the program.

## OUTPUT

The color output of the fragment stage.
