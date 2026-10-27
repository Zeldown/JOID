---
summary: One stage of a shader written in JOID GLSL, parsed so that every backend can translate it.
---

A source is a GLSL body in the dialect of JOID: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl) gives its rules and its built-in names.
Parsing drops the `#version` line and the `in`, `out` and `uniform` declarations, which the translator writes again for each backend, and notes the built-in names and the [[ShaderFeature]]s that the code uses.
A [[ShaderProgram]] reads both of its stages for you.

Learn: [Shaders](/use/learn/going-further/shaders/), [Shader translation](/build/shader-translation/#from-source-to-program).

## from(ShaderStage,String)

Parses a source from its code.
Throws `IllegalArgumentException` when a vertex source declares an `in` variable (read the built-in attributes instead) or a fragment source declares an `out` variable (write the color to `fragColor`).

## read(ShaderStage,Object)

Reads and parses a source from anything that [[Asset#of]] opens: a `File`, an `InputStream` such as a classpath resource, a URL, an [[Asset]].
Throws `UncheckedIOException` when the source cannot be read, and `IllegalArgumentException` as `from` does.

## getInputs()

Returns the `in` variables that a fragment source declares, in their order.

## getBuiltins()

Returns the built-in attributes, uniforms and outputs that the code uses, found by name outside the comments.

## getFeatures()

Returns the GLSL features beyond the baseline that the code and its declarations use.

## getOutputs()

Returns the `out` variables that a vertex source declares, in their order.

## getUniforms()

Returns the `uniform` variables that are not samplers, in their order.

## getSamplers()

Returns the `sampler` uniforms, in their order.

## toLine()

Returns the line variant of a vertex source: the same code, run twice per vertex by a wrapper that widens each segment into a quad of the line width.
The core calls it to build the line shader of a shader; your own shaders never need it.

## getStage()

Returns the stage that the source was parsed for.

## getBody()

Returns the code without the `#version` line and without the declarations.
Each removed line stays as an empty line, so the line numbers of a compiler error match the source.

## isLine()

If `true`, the source comes from `toLine()`.
