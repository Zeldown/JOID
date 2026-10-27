---
summary: A texture of OpenGL with RGBA8 storage for every level, uploaded as BGRA.
---

A texture larger than the maximum size of the context throws an `IllegalArgumentException` that starts with `A texture of`.

Learn: [Textures, framebuffers, shaders](/build/base-modules/opengl/#textures-framebuffers-shaders).

## create(GlRenderBridge)

Creates the OpenGL texture, without storage; [[GlRenderBridge#createTexture]] calls it.

## sample(TextureSampling)

Sets the filter and the wrap of the bound texture, sending only the parameters that changed since the last sampling.

## forgetSampling()

Forgets the last sampling, so that the next `sample` sends every parameter.

## getId()

Returns the OpenGL name of the texture.

## getBridge()

Returns the render bridge that created the texture.

## getSampling()

Returns the last sampling sent to OpenGL, or `null` when none is known.
