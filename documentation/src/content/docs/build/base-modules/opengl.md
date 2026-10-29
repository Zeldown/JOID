---
title: "base-opengl"
description: "The OpenGL module: a render bridge for OpenGL 2.1 and later over a binding, with capability detection, a state guard, textures, framebuffers, shaders and mipmaps."
genre: build
sidebar:
  order: 2
---

`joid-base-opengl` is a complete [render bridge](/build/bridges/render/) for OpenGL.
It calls OpenGL only through an [[IGlBinding]], so the LWJGL 2 and LWJGL 3 backends share it, and an engine that exposes OpenGL can use it too.

## Create the bridge

[[GlRenderBridge]] reads the current context in its constructor, `new GlRenderBridge(binding)`, so the context must be current on the calling thread; without one, it throws an `IllegalStateException`.

| Binding | Library |
|---|---|
| [[Lwjgl3GlBinding#inst]] | LWJGL 3 OpenGL, in [base-lwjgl3-opengl](/build/base-modules/lwjgl3-opengl/) |
| [[Lwjgl2GlBinding#inst]] | LWJGL 2 OpenGL, in [base-lwjgl2](/build/base-modules/lwjgl2/) |
| your [[IGlBinding]] | the OpenGL functions of your engine |

An [[IGlBinding]] gives the queries and the calls of OpenGL, one method for one function, and an [[IGlFrameBufferBinding]] for each family of framebuffer objects, the core one and the EXT one.
`canPointClientMemory()` returns `false` for a binding that cannot hand OpenGL a pointer into client memory: the state guard then leaves the client arrays of the host alone.
Return `null` from `getString` for [[GlConstants#VERSION]] when no context is current, as the official bindings do: the bridge then throws its `IllegalStateException`.

## Capabilities and paths

At creation, [[GlCapabilities]] reads the version, the GLSL version, the [[GlProfile]] (compatibility, core, forward-compatible core), the renderer, the extensions and the maximum texture size.
The bridge picks one path per feature:

| Feature | Path | Fallback |
|---|---|---|
| GLSL dialect | the highest desktop dialect of the context, up to GLSL 3.30, with loose uniforms | GLSL 1.20 |
| Framebuffers | OpenGL 3.0 or `GL_ARB_framebuffer_object` | `GL_EXT_framebuffer_object` |
| Vertex input | a vertex array object, pointed once ([[GlVertexInput]]) | attributes pointed at each draw |
| Mipmaps | a framebuffer blit per level ([[BlitGlMipmapBuilder]]) | a quad drawn per level ([[DrawGlMipmapBuilder]]), with a dev warning |

Below OpenGL 2.1 with GLSL 1.20, or without any framebuffer object, creation throws an `IllegalStateException` that names what the context offers.

## The state guard

Every call of the bridge that changes the state goes through a [[GlStateGuard]], which notes the state of the host and gives it back at the end of the frame and around a drawing of the host.
[The state journal](/build/embedding-in-a-host/#the-state-journal) explains it.

## Textures, framebuffers, shaders

| Class | Holds |
|---|---|
| [[GlTexture]] | RGBA8 storage for every level, uploaded as BGRA; a texture larger than the maximum size of the context throws an `IllegalArgumentException`. |
| [[GlFrameBuffer]] | A color texture and a 24-bit depth renderbuffer, without stencil ([Framebuffers and depth](/build/framebuffers-and-depth/)). |
| [[GlShader]] | A program linked from the translation, with the attribute locations bound before linking; a compilation or link error prints the log of the driver and leaves the shader inactive. |
| [[GlBorrowedTexture]] | A texture of the host, by name or by `IntSupplier` ([Resource resolvers](/build/resource-resolvers/)). |

The bridge draws into the framebuffer that the host had bound when the frame began ([Embedding in a host](/build/embedding-in-a-host/#draw-after-the-host-frame)), and binds only the samplers that the shader declares.

## Tools for hosts and tests

| Class | Role |
|---|---|
| [[FixedMatrixImport]] | Loads the fixed-function matrices of a compatibility context into JOID ([Embedding in a host](/build/embedding-in-a-host/#import-the-matrices-of-a-fixed-function-host)). |
| [[GlSnapshotCapture]] | Reads the back buffer into a snapshot image, for the snapshot backends. |
