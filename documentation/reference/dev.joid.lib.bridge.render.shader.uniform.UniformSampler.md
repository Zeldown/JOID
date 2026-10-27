---
summary: One sampler of a shader, its texture unit and the texture, filter and wrap bound to it.
---

The constructor of [[Shader]] creates one per sampler of its stages, numbered from unit `1`: the backend keeps unit `0` for its own use.
At each draw, the render bridge resolves each sampler with `resolveSampler`, which falls back to the texture of the render state when the sampler has no allocated texture.

Learn: [Textures and samplers](/build/pipeline/#textures-and-samplers).

## create(String,int)

Creates a sampler from its GLSL name and its texture unit.

## value(ITexture,TextureFilter,TextureWrap)

Binds a texture to the sampler, with its filter and its wrap; `IShader.sampler` calls it.

## getName()

Returns the name of the sampler in the GLSL source.

## getUnit()

Returns the texture unit of the sampler, from `1`, in the order of the vertex then the fragment stage.

## getTexture()

Returns the bound texture, or `null` when none is bound.

## getWrap()

Returns the wrap bound with the texture, or `null` when none is bound.

## getFilter()

Returns the filter bound with the texture, or `null` when none is bound.
