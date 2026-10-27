---
summary: Rewrites the integer literals of a GLSL body as float literals where a float is expected, for the dialects whose compiler does not convert them.
---

GLSL 1.10 and GLSL ES refuse `float x = 1;` and need `1.0`.
The shaders of JOID are written for GLSL 1.20, which converts on its own, so [[GlslShaderTranslator]] runs this rewrite for the dialects without [[GlslDialect#hasImplicitConversions]].
An integer variable used as a float still needs `float(...)` in the source.

Learn: [Pick a dialect](/build/shader-translation/#pick-a-dialect).

## apply(String,List)

Returns the body with `.0` after each integer literal that sits where a float is expected.
A literal stays an integer inside brackets, in the arguments of an integer constructor or of an integer variable, after `case`, next to an operator whose other side is an integer variable, and on a preprocessor line.
`variables` lists the declared variables whose integer type marks their names; the integer declarations of the body add to them.
