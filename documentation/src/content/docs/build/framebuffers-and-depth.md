---
title: "Framebuffers and depth"
description: "Framebuffers, depth and stencil seen from a backend: attachments, orientation, depth range, stencil and clears."
genre: build
sidebar:
  order: 3
---

JOID draws into framebuffers for its effects and shader passes, sorts 3D content with a depth buffer, and clips masks and scroll areas with the stencil of the screen.
This page gives the rules a backend follows for each of them.
The user side of framebuffers is on [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/).

## What a framebuffer holds

[[IRenderBridge#createFrameBuffer]] returns an [[IFrameBuffer]] of a size in pixels.
Extend [[FrameBufferHandle]]: it keeps the color texture, answers the size from it, and deletes once, your `deleteHandle()` first, then the texture.

**Required**

| Attachment | Rule |
|---|---|
| Color | A texture of your backend, the one `getTexture()` returns, so that JOID samples it like any texture. |
| Depth | Required: JOID tests the depth inside framebuffers as on the screen. |
| Stencil | None that the core relies on. |

The core sets a framebuffer as the target with `getState().frameBuffer(target)` and its viewport with `viewport(...)`; your bridge applies both at each draw and each clear.
`null` is the screen.

### Why there is no stencil

JOID clips masks and scroll areas with the stencil of the screen: each mask increments the stencil inside its area and draws its content with an `EQUAL` test.
[[RenderBridge#clearStencil]] clears only the screen, so a mask drawn inside a framebuffer would test against a stencil that nobody cleared, and its content would vanish.
Without a stencil attachment, the test always passes offscreen, which is what effects need.
A backend whose API pairs depth and stencil in one image turns the stencil test off when the target is not the screen, as the Vulkan backend does.

## Orientation

A framebuffer stores its rows as OpenGL does: row 0 is the bottom of what was drawn, and texture coordinate `v = 0` samples it.
JOID draws a framebuffer back with `v = 0` at the bottom of the quad, so the content comes out upright.

On an API whose framebuffer coordinates go down (Vulkan), flip the viewport when drawing to the screen and leave it unflipped for a framebuffer: the image lands in memory with the same orientation as on OpenGL, and the faces keep their winding.
The contract suite checks the horizontal orientation with `rendersIntoFrameBuffers`, the vertical one with `rastersADrawingUprightAtItsPixelSize`, and the winding with `cullsTheBackFacesInFrameBuffersLikeOpenGl`.

## Depth

The projection follows OpenGL: depth from -1 to 1 in clip space.
[[DepthRange#toZeroToOne]] rewrites a projection for an API whose depth goes from 0 to 1, as Vulkan does.

**Guarantees**

| Rule | Why |
|---|---|
| `clearDepthBuffer()` clears to 1, whatever the depth write state. | A drawing may turn depth writes off and still clear the depth; the next draw applies the depth write state again. |
| The depth test compares with `LESS`. | Nearer fragments win, equal ones lose. |
| The screen and every framebuffer have a depth buffer of at least 24 bits. | 3D models and stacked UIs share one depth range. |
| A framebuffer keeps its depth while another target is drawn. | An effect can draw into a framebuffer, switch to the screen, and come back (`keepsTheFrameBufferDepthAcrossTargets`). |

The UI bridge places the UIs at increasing depths, so that a UI drawn later stays in front of the 3D content of the UIs below it; the user side of that is the `zlevel` of a UI.

## Stencil

Only the screen has a stencil, of 8 bits.
At the start of each frame, `drawBackground()` of the lowest UI bridge clears it to 0: [[WindowUIBridge]] does, and a UI bridge of a host must too ([UI bridge](/build/bridges/ui/#guarantees)).
A [[StencilState]] holds the function, the reference, the mask and the three operations; apply them as they are, on the front and back faces.

## Clears

| Call of the core | Reaches your bridge when | Target |
|---|---|---|
| [[RenderBridge#clearColor]] | color write is on | the current target |
| [[RenderBridge#clearDepth]] | always | the current target |
| [[RenderBridge#clearStencil]] | the current target is the screen | the screen |

The core clears a framebuffer only when its drawing asks for it: [[FrameBuffer#fill]] draws over the previous content, while the shader pipeline of the effects and [[DrawUtils#RASTER]] clear their framebuffers before they draw.
