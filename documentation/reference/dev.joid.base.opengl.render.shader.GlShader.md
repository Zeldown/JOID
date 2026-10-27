---
summary: A shader program of OpenGL, linked from the translation of the two stages of a JOID shader.
---

A compilation or a link error prints the log of the driver and leaves the shader inactive, without throwing.

Learn: [Textures, framebuffers, shaders](/build/base-modules/opengl/#textures-framebuffers-shaders), [Shader translation](/build/shader-translation/).

## create(GlRenderBridge,ShaderSource,ShaderSource,BlendState)

Creates the shader, translated to the dialect of the context with loose uniforms; [[GlRenderBridge#createShader]] calls it.

## use(RenderState,VertexBuffer)

Makes the program current and uploads its built-in uniforms and its uniform values, for a draw with that state and those vertices.

## getLocation(String)

Returns the location of a uniform, cached per name, or `-1` when the program has none.

## getProgram()

Returns the OpenGL name of the program, `0` once deleted.

## isProgramActive()

If `true`, the program compiled and linked.
