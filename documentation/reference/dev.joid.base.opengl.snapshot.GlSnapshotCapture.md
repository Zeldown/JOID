---
summary: Reads the back buffer of an OpenGL context into a snapshot image.
---

It ships in the dev and demo jars of the backends; the published `joid-base-opengl` jar does not hold it.

Learn: [Tools for hosts and tests](/build/base-modules/opengl/#tools-for-hosts-and-tests).

## capture(GlRenderBridge,int,int)

Returns the pixels of the back buffer of the default framebuffer, at that size in pixels, top row first.
