---
title: "Render bridge"
description: "The contract of the render bridge: what a backend implements, what RenderBridge gives it, and what every draw must respect."
genre: build
sidebar:
  order: 1
---

The render bridge draws every pixel of JOID: it clears targets, draws triangles and lines, and creates textures, framebuffers and shaders.
The core calls it through [[IRenderBridge]], read from [[BridgeHandler#RENDER]].

Extend [[RenderBridge]] rather than implementing the interface: it keeps the matrices, the render state and the frame in Java, and leaves you nine methods to write.
[Writing a backend](/build/writing-a-backend/#implement-the-render-bridge) builds a complete one; [Render pipeline](/build/pipeline/) explains what reaches it.

## Required

What your subclass of [[RenderBridge]] implements.

| Member | What it does |
|---|---|
| [[RenderBridge#drawPrimitive]] | Draws `TRIANGLES` or `LINES` from a [[VertexBuffer]], with the given shader and the current state. |
| [[RenderBridge#clearColorBuffer]] | Clears the color of the current target. |
| [[RenderBridge#clearDepthBuffer]] | Clears the depth of the current target to 1. |
| [[RenderBridge#clearStencilBuffer]] | Clears the stencil of the screen to 0. |
| [[IRenderBridge#createTexture]] | Returns an empty texture, a subclass of [[Texture]]. |
| [[IRenderBridge#createFrameBuffer]] | Returns a framebuffer of that size in pixels, a subclass of [[FrameBufferHandle]]. |
| [[IRenderBridge#createShader]] | Returns a shader for two parsed stages and a blend state, a subclass of [[Shader]]. |
| [[IRenderBridge#suspend]] | Runs a drawing of a host with its own state inside a JOID frame: it sets the state and the pass in progress aside, runs the drawing, then takes them back ([Embedding in a host](/build/embedding-in-a-host/#let-the-host-draw-inside-a-frame)). |
| [[IRenderBridge#raster]] | Runs a drawing into a JOID framebuffer at the given size in pixels, for [[DrawUtils#RASTER]], and requires an open frame. |

## Provided

What [[RenderBridge]] does for you.

| Member | What you get |
|---|---|
| [[RenderBridge#getState]] | The live [[RenderState]]: color, blend, depth, cull, lighting, color write, alpha cutoff, lines, stencil, viewport, target framebuffer, texture with its filter and wrap, shader. |
| [[RenderBridge#pushState]], [[RenderBridge#popState]] | A stack of copies: `popState` loads the copy back into the same instance, so a reference to `getState()` stays valid. |
| [[RenderBridge#getModelView]], [[RenderBridge#getProjection]] | Two [[MatrixStack]]s, read as column-major `float[16]`: a copy with `getMatrix()`, the live array with `getRawMatrix()`. |
| [[RenderBridge#beginFrame]], [[RenderBridge#endFrame]] | The frame flag around your two frame hooks; a second `beginFrame` or an `endFrame` outside a frame throws an `IllegalStateException`. |
| [[RenderBridge#requireFrame]], [[RenderBridge#isFrameActive]] | The frame flag for your hooks: `requireFrame()` throws an `IllegalStateException` with the message the contract expects. |
| [[RenderBridge#clearColor]], [[RenderBridge#clearDepth]], [[RenderBridge#clearStencil]] | The calls of the core, which reach your clear hooks under the rules of [Clears](/build/framebuffers-and-depth/#clears). |
| [[RenderBridge#draw]] | Picks the default shader when none is bound, and skips the draw when the shader is not active, the buffer is empty or the viewport has no area. |
| [[RenderBridge#screen]] | Targets the window: no framebuffer, a viewport of the window size, and an orthographic projection in screen pixels from the top-left corner, with depth from `0` to `10000`. |
| [[RenderBridge#getPixelGrid]], [[RenderBridge#quantize]] | The [[PixelGrid]] of the current matrices and viewport, which places edges on screen pixels ([Pixel alignment](/use/in-depth/pixel-alignment/)). |
| [[RenderBridge#resolveSampler]], [[RenderBridge#resolveTexture]] | The texture to bind on the unit of a sampler, with its filter and wrap: the texture of the sampler, else the texture of the state, else a 1×1 opaque white texture. |
| [[RenderBridge#getDefaultShader]], [[RenderBridge#getEmptyTexture]] | The shader and the texture that `draw` and `resolveTexture` fall back to, created on first use. |

The members with a default, which you override when your API needs it:

| Member | Default | Override it to |
|---|---|---|
| [[RenderBridge#beginFrameCommands]], [[RenderBridge#submitFrameCommands]] | Do nothing. | Start recording and submit, for an API that records commands per frame, or give a host its state back. |
| [[IRenderBridge#canWrap]] | Returns `true`. | Return `false` for a texture wrap your API lacks: JOID then samples with `CLAMP_TO_EDGE`, and emulates a refused `CLAMP_TO_BORDER` in the shaders. |

## Guarantees

What your implementation must ensure.
[[RenderBridgeContractSuite]] checks most rows in a few seconds ([Testing a backend](/build/testing-a-backend/)); the rows marked *review* are covered only by the snapshots or by reading your code.

| Rule | Why |
|---|---|
| Apply the whole current state at each `drawPrimitive`: target, viewport, blend, depth test and write, cull, color write, stencil, samplers. | The core changes the state between draws without telling the bridge. |
| Call `requireFrame()` in `drawPrimitive`, in the three clear hooks and in `raster`. | A draw outside a frame must fail with an `IllegalStateException`, never land in the target of the previous frame. |
| Create, fill and delete textures, framebuffers and shaders inside a frame and outside one. | The UIs load their resources before `beginFrame()`, and the contract suite creates its resources outside any frame. |
| Draw every frame that began before your present: several `beginFrame()`/`endFrame()` pairs can share one present. | A host may draw two JOID frames into one of its own frames. |
| Read the vertices with the layout of [[VertexAttribute]]: 32 bytes per vertex, missing attributes completed. | Every vertex buffer of JOID uses it ([Render pipeline](/build/pipeline/#vertices)). |
| Follow the OpenGL clip space, in which the projection of `screen` maps the top-left corner of the window to `(0, 0)`, and the rules of [Depth](/build/framebuffers-and-depth/#depth). | The core builds its projections and sorts its 3D content for that clip space. |
| Store a framebuffer as OpenGL does: texture coordinate `v = 0` samples the bottom row of what was drawn. | The core samples framebuffers with that convention ([Orientation](/build/framebuffers-and-depth/#orientation)). |
| Draw `LINES` one pixel wide (*review*). | Wide and smooth lines reach the bridge as triangles already. |
| Send the uniforms of a shader at the draw, never at `bind()`. | JOID sets uniform values before it binds the shader. |
| Let a shader that fails to compile report `isActive() == false` and print one warning that starts with `[JOID]` and names the compilation, without throwing. | `draw` skips its draws and the frame goes on. |
| Throw an `IllegalArgumentException` that starts with `A texture of <width>x<height> exceeds the maximum size` when a texture is too large for your API. | The message tells the developer to downscale the image, on every backend alike. |
| Keep drawing after a framebuffer is deleted in the middle of its own pass. | A node can recreate its framebuffer when its size changes, during a frame. |

## Frame and host

[[UIBridgeRegistry#frame]] wraps every frame in `beginFrame()` and `endFrame()`; [The frame lifecycle](/use/in-depth/frame-lifecycle/) places that frame among the steps of the UIs.
A backend shared with a host gives the host its state back in `submitFrameCommands`, and around a drawing of the host in `suspend`: [Embedding in a host](/build/embedding-in-a-host/) explains both.
