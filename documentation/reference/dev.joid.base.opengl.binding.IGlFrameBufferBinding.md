---
summary: The framebuffer calls of OpenGL for one family: the core functions or those of `GL_EXT_framebuffer_object`.
---

[[IGlBinding#getFrameBufferBinding]] returns one per family, and [[GlRenderBridge]] uses the family that [[GlCapabilities#getFrameBufferFamily]] picks.

Learn: [Create the bridge](/build/base-modules/opengl/#create-the-bridge).

## genFramebuffer()

Creates a framebuffer and returns its name.
Maps to `glGenFramebuffers`.

## deleteFramebuffer(int)

Deletes a framebuffer.
Maps to `glDeleteFramebuffers`.

## bindFramebuffer(int,int)

Binds a framebuffer to the draw, read or both targets, `0` for the default framebuffer.
Maps to `glBindFramebuffer`.

## framebufferTexture2D(int,int,int,int,int)

Attaches a level of a texture to an attachment of the bound framebuffer.
Maps to `glFramebufferTexture2D`.

## genRenderbuffer()

Creates a renderbuffer and returns its name.
Maps to `glGenRenderbuffers`.

## deleteRenderbuffer(int)

Deletes a renderbuffer.
Maps to `glDeleteRenderbuffers`.

## bindRenderbuffer(int,int)

Binds a renderbuffer, `0` for none.
Maps to `glBindRenderbuffer`.

## renderbufferStorage(int,int,int,int)

Allocates the storage of the bound renderbuffer, in that format and size.
Maps to `glRenderbufferStorage`.

## framebufferRenderbuffer(int,int,int,int)

Attaches a renderbuffer to an attachment of the bound framebuffer.
Maps to `glFramebufferRenderbuffer`.

## blitFramebuffer(int,int,int,int,int,int,int,int,int,int)

Copies a rectangle of the read framebuffer into a rectangle of the draw framebuffer, scaled with the filter.
Maps to `glBlitFramebuffer`.
