---
summary: The two programmable stages of a JOID shader.
---

[[ShaderSource#from]] and [[ShaderSource#read]] take the stage, so that JOID checks the declarations that the stage allows.

Learn: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## VERTEX

The stage that places each vertex: it reads the built-in attributes `aPosition`, `aTexCoord`, `aColor` and `aNormal`, writes `gl_Position`, and declares `out` variables for the fragment stage.

## FRAGMENT

The stage that colors each pixel: it declares as `in` the `out` variables of the vertex stage and writes its color to `fragColor`.
