---
summary: Builds the mipmap levels of a texture of OpenGL from its first level.
---

[[GlRenderBridge]] uses a [[BlitGlMipmapBuilder]] when the context can blit framebuffers, a [[DrawGlMipmapBuilder]] otherwise.

Learn: [Mipmaps](/build/pipeline/#mipmaps).

## build(GlRenderBridge,GlTexture,MipmapChain)

Fills every level of the chain after the first, each from the level above it.
