---
summary: The base class of a texture owned by JOID, which checks every call and leaves the storage, the upload and the mipmaps to its subclass.
---

Learn: [Textures](/build/writing-a-backend/#textures).

## Texture()

Creates a texture, for a subclass.

## getWidth()

Returns the width, in pixels.

## getHeight()

Returns the height, in pixels.

## isAllocated()

If `true`, the texture has storage and is not deleted.

## getLevels()

Returns the number of allocated levels.

## isDeleted()

If `true`, the texture is deleted.

## isMipmapped()

If `true`, the texture keeps mipmap levels.

## mipmap(boolean)

Turns the mipmap levels on or off; turning them on for an allocated texture builds them at once.

## allocate(int,int)

Allocates the storage at that size, with every level when mipmapped; does nothing when the size and the levels are already those.
Throws an `IllegalStateException` once the texture is deleted.

## upload(int[])

Uploads one ARGB pixel per texel and builds the mipmaps.
Throws an `IllegalStateException` before `allocate` or once deleted, and an `IllegalArgumentException` with fewer pixels than texels.

## delete()

Releases the storage; does nothing the second time.

## getMaxLevels(int,int)

JOID calls it before each allocation and upload.
Return the most levels your API gives a texture of that size; no limit by default.

## allocateStorage(MipmapChain)

JOID calls it from `allocate`.
Allocate every level of the chain; throw an `IllegalArgumentException` that starts with `A texture of <width>x<height> exceeds the maximum size` when the size is too large.

## uploadPixels(int[],MipmapChain)

JOID calls it from `upload`.
Write the pixels into the first level, then build the other levels of the chain.

## generateMipmapLevels(MipmapChain,int)

JOID calls it when mipmaps are turned on for an allocated texture, with the number of levels already allocated.
Allocate the missing levels and build them from the first one.

## deleteStorage()

JOID calls it once, from `delete()`.
Release the storage.
