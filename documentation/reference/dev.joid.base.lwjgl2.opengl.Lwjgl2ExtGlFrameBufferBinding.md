---
summary: The LWJGL 2 framebuffer binding that calls the functions of `GL_EXT_framebuffer_object`.
---

[[Lwjgl2GlBinding#getFrameBufferBinding]] returns it for the `EXT` family.

Learn: [base-lwjgl2](/build/base-modules/lwjgl2/#bindings).

## Lwjgl2ExtGlFrameBufferBinding()

Creates the binding.

## genFramebuffer()

Calls `glGenFramebuffersEXT`.

## deleteFramebuffer(int)

Calls `glDeleteFramebuffersEXT`.

## bindFramebuffer(int,int)

Calls `glBindFramebufferEXT`.

## framebufferTexture2D(int,int,int,int,int)

Calls `glFramebufferTexture2DEXT`.

## genRenderbuffer()

Calls `glGenRenderbuffersEXT`.

## deleteRenderbuffer(int)

Calls `glDeleteRenderbuffersEXT`.

## bindRenderbuffer(int,int)

Calls `glBindRenderbufferEXT`.

## renderbufferStorage(int,int,int,int)

Calls `glRenderbufferStorageEXT`.

## framebufferRenderbuffer(int,int,int,int)

Calls `glFramebufferRenderbufferEXT`.

## blitFramebuffer(int,int,int,int,int,int,int,int,int,int)

Calls `glBlitFramebufferEXT`.
