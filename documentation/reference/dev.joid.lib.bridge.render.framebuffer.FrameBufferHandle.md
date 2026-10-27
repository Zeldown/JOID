---
summary: The base class of a framebuffer, which owns its color texture and deletes it with its own handle.
---

Learn: [Framebuffers](/build/writing-a-backend/#framebuffers), [Framebuffers and depth](/build/framebuffers-and-depth/).

## FrameBufferHandle(T)

Creates the framebuffer around its color texture.

## delete()

Deletes the handle, then the color texture; does nothing the second time.

## getWidth()

Returns the width of the color texture, in pixels.

## getHeight()

Returns the height of the color texture, in pixels.

## getTexture()

Returns the color texture, which a shader samples once the framebuffer is drawn.

## isDeleted()

If `true`, the framebuffer is deleted.

## deleteHandle()

JOID calls it once, from `delete()`, before it deletes the color texture.
Delete the framebuffer and its depth buffer.
