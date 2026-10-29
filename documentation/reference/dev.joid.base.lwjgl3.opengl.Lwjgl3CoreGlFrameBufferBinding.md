---
summary: The LWJGL 3 framebuffer binding that calls the core framebuffer functions of OpenGL 3.0 and `GL_ARB_framebuffer_object`.
---

[[Lwjgl3GlBinding#getFrameBufferBinding]] returns it for the `CORE` family.

Learn: [base-lwjgl3-opengl](/build/base-modules/lwjgl3-opengl/).

## Lwjgl3CoreGlFrameBufferBinding()

Creates the binding.

## genFramebuffer()

Calls `glGenFramebuffers`.

## deleteFramebuffer(int)

Calls `glDeleteFramebuffers`.

## bindFramebuffer(int,int)

Calls `glBindFramebuffer`.

## framebufferTexture2D(int,int,int,int,int)

Calls `glFramebufferTexture2D`.

## genRenderbuffer()

Calls `glGenRenderbuffers`.

## deleteRenderbuffer(int)

Calls `glDeleteRenderbuffers`.

## bindRenderbuffer(int,int)

Calls `glBindRenderbuffer`.

## renderbufferStorage(int,int,int,int)

Calls `glRenderbufferStorage`.

## framebufferRenderbuffer(int,int,int,int)

Calls `glFramebufferRenderbuffer`.

## blitFramebuffer(int,int,int,int,int,int,int,int,int,int)

Calls `glBlitFramebuffer`.
