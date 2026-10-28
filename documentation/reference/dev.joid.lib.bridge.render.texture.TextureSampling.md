---
summary: A filter, a wrap and a mipmap flag, one of the 12 shared samplings that a render bridge can cache.
---

Learn: [Textures and samplers](/build/pipeline/#textures-and-samplers).

## of(TextureFilter,TextureWrap,boolean)

Returns the shared sampling of that filter, that wrap and that mipmap flag.

## values()

Returns the 12 samplings, read-only, in the order of their index.

## isMipmapFiltered()

If `true`, the sampling reads mipmaps with a linear filter.

## getIndex()

Returns the index of the sampling, from `0` to `11`, for a cache of samplers.

## getWrap()

Returns the wrap.

## isMipmapped()

If `true`, the texture has mip levels that the draw may use; only a `LINEAR` filter reads them (`isMipmapFiltered()`), and a `NEAREST` sampling reads level 0 alone.

## getFilter()

Returns the filter.
