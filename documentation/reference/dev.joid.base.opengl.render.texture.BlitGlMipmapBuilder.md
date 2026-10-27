---
summary: Builds the mipmaps of a texture with one linear framebuffer blit per level, the path that matches the other backends pixel for pixel.
---

Learn: [Mipmaps](/build/pipeline/#mipmaps).

## BlitGlMipmapBuilder()

Creates the builder.

## build(GlRenderBridge,GlTexture,MipmapChain)

Blits each level of the chain from the level above it, with a linear filter.
