---
summary: An offscreen target of the render bridge, with a color texture and a depth buffer.
---

[[FrameBufferPool]] lends them to the core, and `RenderState.frameBuffer(...)` makes one the target.

Learn: [Framebuffers and depth](/build/framebuffers-and-depth/).

## delete()

Releases the framebuffer and its texture.

## getWidth()

Returns the width, in pixels.

## getHeight()

Returns the height, in pixels.

## getTexture()

Returns the color texture.
