---
summary: Builds the mipmaps of a texture by drawing a quad per level, for an OpenGL context that cannot blit framebuffers.
---

Learn: [Mipmaps](/build/pipeline/#mipmaps).

## DrawGlMipmapBuilder()

Creates the builder and its quad; its shader is created at the first use.

## build(GlRenderBridge,GlTexture,MipmapChain)

Draws each level of the chain from the level above it, with a linear filter.
With the dev jar, prints once that the pixels may differ from the other backends; throws an `IllegalStateException` when its shader does not compile on the context.
