---
summary: A texture of the render bridge, owned by JOID or borrowed from the host.
---

Learn: [Use the texture of a resource](/use/learn/going-further/framebuffers-and-models/#use-the-texture-of-a-resource), [Textures](/build/writing-a-backend/#textures).

## mipmap(boolean)

If `true`, the texture keeps a full chain of mipmap levels, built from the first level at each upload.

## allocate(int,int)

Allocates the storage at that size in pixels.

## upload(int[])

Uploads the pixels, one ARGB `int` per texel, row after row, then builds the mipmaps.

## delete()

Releases the texture.

## getWidth()

Returns the width, in pixels; `0` before `allocate`.

## getHeight()

Returns the height, in pixels; `0` before `allocate`.

## isMipmapped()

If `true`, the texture has mipmap levels.

## isAllocated()

If `true`, the texture has storage to sample.
