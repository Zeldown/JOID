---
title: "Render pipeline"
description: "What reaches a render bridge: frames, draw calls, vertices, render state, matrices, samplers and mipmaps."
genre: build
sidebar:
  order: 1
---

Every pixel of JOID, from a rectangle to a 3D model, reaches the render bridge through one method: `draw(primitive, buffer)`.
This page follows a drawing from the node to the API of the backend, then describes each piece of data the bridge reads.
The contract itself is on [Render bridge](/build/bridges/render/); the user side of drawing is on [Drawing](/use/learn/going-further/drawing/).

<figure>

![Two rows of boxes linked by arrows. The first row: Nodes and DrawUtils describe vertices, the Tessellator turns them into TRIANGLES or LINES, RenderBridge.draw picks the shader, then the highlighted drawPrimitive hook makes one draw call; a box below it, read at each draw, lists getState(), the matrices and resolveSampler(). The second row: JOID GLSL, one source per stage, parsed into a ShaderSource, translated once by the Shader, then given to the highlighted compileProgram hook in your dialect.](/captures/build-pipeline.webp)

<figcaption>
Your two hooks: `drawPrimitive` receives each draw with the state of that moment, `compileProgram` receives each shader once, translated from JOID GLSL.
</figcaption>
</figure>

## A frame

[[UIBridgeRegistry#frame]] runs each frame; [The frame lifecycle](/use/in-depth/frame-lifecycle/) shows the same frame from the side of the UIs and the nodes.
For the render bridge, a frame is:

1. `screen(width, height)` when the window size changed, which targets the window ([Render bridge](/build/bridges/render/#provided) lists what it sets).
2. The updates of the UIs, outside the frame of the bridge: they can create, fill and delete textures, framebuffers and shaders.
3. `beginFrame()`, which calls `beginFrameCommands()`.
4. The clears of `drawBackground()` on the lowest UI bridge, then every draw of every UI bridge.
5. `endFrame()`, which calls `submitFrameCommands()`.

## A draw call

Nodes and the drawing API never build GPU commands: they describe vertices to the [[Tessellator]], which turns every mode into one of the two primitives that a bridge draws.

| Mode of the tessellator | Reaches the bridge as |
|---|---|
| `TRIANGLES` | `TRIANGLES` |
| `QUADS` | `TRIANGLES`, two per quad |
| `POLYGON` | `TRIANGLES`, a fan from the first vertex |
| `LINES`, `LINE_STRIP`, `LINE_LOOP` | `LINES`, two vertices per segment |
| any line mode with a width other than 1 or smoothing on | `TRIANGLES`, drawn by the line variant of the shader |

[[RenderBridge#draw]] then picks the shader and calls your [[RenderBridge#drawPrimitive]] with the primitive, the buffer and the shader.

## Vertices

A [[VertexBuffer]] holds `getCount()` vertices of 32 bytes each, in a direct `ByteBuffer` in native order.
[[VertexAttribute]] describes the layout:

| Attribute | Location | Offset | Content |
|---|---|---|---|
| `POSITION` | 0 | 0 | 3 × `float` |
| `TEXTURE_COORDINATE` | 1 | 12 | 2 × `float` |
| `COLOR` | 2 | 20 | 4 × unsigned byte, normalized, RGBA |
| `NORMAL` | 3 | 24 | 3 × signed byte, normalized |

Bytes 27 to 31 are padding.
`isTexture()`, `isColor()` and `isNormal()` tell which attributes the drawing filled, and a missing attribute takes a default:

- texture coordinates `(0, 0)` and normal `(0, 0, 1)`: on OpenGL, constant vertex attributes; on an API without them, [[VertexFill#complete]] copies the buffer and writes the defaults;
- color: the current color of the state, which the translated shader picks when the buffer has no color, so the bridge does nothing.

## The render state

[[RenderState]] is the whole fixed-function state of a draw, held by [[RenderBridge#getState]] and changed by the core between draws.

| Property | Default | Meaning |
|---|---|---|
| `color(red, green, blue, alpha)` | opaque white | The color of the vertices without a color. |
| `blend` | [[BlendState#DISABLED]] | Equation and factors; [[BlendState#NORMAL]] and [[BlendState#PREMULTIPLIED]] are the usual ones. |
| `depthTest`, `depthWrite` | off, on | Depth comparison `LESS`, and depth writes. |
| `colorWrite` | on | Writes to the color buffer. |
| `cull` | off | Back-face culling, counter-clockwise front faces. |
| `stencil` | [[StencilState#DISABLED]] | Function, reference, mask and operations, for the screen only. |
| `alphaCutoff` | `0F` | Discards the fragments whose alpha is at most this value; `0F` turns the test off. |
| `lighting` | off | Diffuse lighting from the normals, applied by the shaders. |
| `lineWidth`, `lineSmooth` | `1F`, off | Read by the tessellator, which turns wide or smooth lines into triangles. |
| `viewport(x, y, width, height)` | set by `screen` | The viewport, in pixels of the target. |
| `frameBuffer` | `null` | The target framebuffer; `null` is the screen. |
| `texture`, `textureFilter`, `textureWrap`, `textureMipmap` | none, `NEAREST`, `REPEAT`, on | The texture of the state, how to sample it, and whether its mip levels may be used. |
| `shader` | `null` | The bound shader; `null` draws with the default one. |

Several values never reach a draw as such: the shader turns the alpha cutoff, the line width and the current color into uniforms ([Shader translation](/build/shader-translation/#what-the-translator-adds)).

## Matrices and coordinates

Read [[RenderBridge#getProjection]] and [[RenderBridge#getModelView]] at the draw; [[Shader#builtins]] writes them, with the normal matrix, into the uniforms of the shader.

The clip space is the one of OpenGL: `x` and `y` from -1 to 1, with `y` up.
An API whose `y` goes down flips its viewport, and the depth range is on [Depth](/build/framebuffers-and-depth/#depth).

## Textures and samplers

In `drawPrimitive`, bind `resolveSampler(sampler)` on the unit of each sampler that the shader declares ([Render bridge](/build/bridges/render/#provided) gives its fallbacks).
Each returns a [[SamplerBinding]]: the texture, and a [[TextureSampling]] with its filter, its wrap and whether it has mip levels.

- [[TextureSampling#isMipmapFiltered]] is `true` for a `LINEAR` filter on a texture with mip levels: sample with linear filtering between the levels too.
- A wrap that [[IRenderBridge#canWrap]] refuses reaches you as `CLAMP_TO_EDGE`; when the refused wrap is `CLAMP_TO_BORDER`, the shaders also emulate its transparent border.

## Mipmaps

JOID computes the mip chain in the core, so that every backend fills the same levels.
A texture has no mip levels until `mipmap(true)`; then its [[MipmapChain]] has `floor(log2(max(width, height))) + 1` levels, each half the size of the previous one and at least 1×1.
Override `getMaxLevels(width, height)` in your [[Texture]] to cap the chain.

Each level is a linear copy of the level before it, never of level 0, and never through a driver function that picks its own filter.
[[MipmapChain#forEachStep]] calls you once per level with both sizes: copy level `n - 1` into level `n` with a linear filter.

| Hook of [[Texture]] | Called when | Fill |
|---|---|---|
| `allocateStorage(chain)` | The size or the number of levels changes. | Storage for every level of the chain, before any copy. |
| `uploadPixels(pixels, chain)` | New pixels arrive for level 0. | Level 0, then every other level with `forEachStep`. |
| `generateMipmapLevels(chain, allocatedLevels)` | Mipmaps are turned on for a texture that already has pixels. | The missing storage when `allocatedLevels` differs, then the levels, keeping level 0. |
| `deleteStorage()` | The texture is deleted, once. | Nothing: release the storage. |

The OpenGL module copies each level with a framebuffer blit, or with a drawn quad when the context cannot blit ([base-opengl](/build/base-modules/opengl/)).

## Clean up between frames

At the start of its drawing, each UI bridge releases the resources whose owners were collected and the framebuffers of the effects unused for a while.
A backend that defers destruction until the GPU is done (Vulkan does) receives these deletions in the middle of a frame: keep them until the GPU has finished reading the object.
