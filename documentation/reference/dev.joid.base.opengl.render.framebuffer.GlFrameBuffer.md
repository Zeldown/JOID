---
summary: A framebuffer of OpenGL with a color texture and a 24-bit depth renderbuffer, without stencil.
---

Learn: [Textures, framebuffers, shaders](/build/base-modules/opengl/#textures-framebuffers-shaders), [Framebuffers and depth](/build/framebuffers-and-depth/).

## GlFrameBuffer(GlRenderBridge,int,int)

Creates the framebuffer, its color texture and its depth renderbuffer at that size in pixels; [[GlRenderBridge#createFrameBuffer]] calls it.

## getId()

Returns the OpenGL name of the framebuffer.

## getDepth()

Returns the OpenGL name of the depth renderbuffer.

## getBridge()

Returns the render bridge that created the framebuffer.
