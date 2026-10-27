---
summary: What the current OpenGL context offers, read once when the render bridge is created: versions, profile, extensions and limits.
---

[[GlRenderBridge]] picks one path per feature from it: GLSL dialect, framebuffer family, vertex arrays, mipmap builder.

Learn: [Capabilities and paths](/build/base-modules/opengl/#capabilities-and-paths).

## read(IGlBinding)

Reads the capabilities of the current context.
Throws an `IllegalStateException` when no OpenGL context is current on the calling thread.

## require()

Returns the capabilities when JOID can draw with them.
Throws an `IllegalStateException` that names the context below OpenGL 2.1 with GLSL 1.20, or without any framebuffer object.

## hasExtension(String)

If `true`, the context lists that extension, such as `"GL_ARB_sampler_objects"`.

## hasDepthClamp()

If `true`, the context has depth clamping (OpenGL 3.2 or `GL_ARB_depth_clamp`), which the state guard turns off for JOID.

## hasVertexArrays()

If `true`, the context has vertex array objects (OpenGL 3.0 or `GL_ARB_vertex_array_object`).

## hasPixelBuffers()

If `true`, the context has pixel buffer objects (OpenGL 2.1 or an extension).

## hasSamplerObjects()

If `true`, the context has sampler objects (OpenGL 3.3 or `GL_ARB_sampler_objects`): the bridge unbinds the sampler of each unit it uses.

## hasFrameBufferSrgb()

If `true`, the context has sRGB framebuffers (OpenGL 3.0 or an extension), which the state guard turns off for JOID.

## hasFrameBufferBlit()

If `true`, the context can blit framebuffers: core framebuffers or `GL_EXT_framebuffer_blit`.

## hasPrimitiveRestart()

If `true`, the context has primitive restart (OpenGL 3.1), which the state guard turns off for JOID.

## hasRasterizerDiscard()

If `true`, the context has rasterizer discard (OpenGL 3.0), which the state guard turns off for JOID.

## getName()

Returns a readable name of the context, such as `OpenGL 3.3 with GLSL 3.30 (renderer)`, which the messages of JOID quote.

## getDialect()

Returns the highest desktop GLSL dialect of the context, up to GLSL 3.30; at least GLSL 1.20.

## getFrameBufferFamily()

Returns `CORE` for OpenGL 3.0 or `GL_ARB_framebuffer_object`, `EXT` for `GL_EXT_framebuffer_object`, `UNSUPPORTED` otherwise.

## getVersion()

Returns the OpenGL version times 100, such as `330` for 3.3.

## getGlslVersion()

Returns the GLSL version times 100, such as `330` for 3.30.

## getProfile()

Returns the profile of the context.

## getRenderer()

Returns the renderer string of the driver.

## getExtensionSet()

Returns the extensions of the context, read-only.

## getMaxTextureSize()

Returns the largest width and height of a texture, in pixels.
