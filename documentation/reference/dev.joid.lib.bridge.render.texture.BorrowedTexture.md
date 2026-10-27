---
summary: The base class of a texture that belongs to the host: JOID samples it and reads its size, and never allocates, writes or deletes it.
---

The handle comes from a supplier, read again at each use, so that the host can recreate its texture.

Learn: [Lend a texture of the host](/build/resource-resolvers/#lend-a-texture-of-the-host).

## BorrowedTexture(Supplier)

Creates the texture over the supplier of the handle of the host.

## getWidth()

Returns the width of the current handle, or `0` when the supplier returns `null`.

## getWidth(H)

JOID calls it with the current handle.
Return its width in pixels.

## getHeight()

Returns the height of the current handle, or `0` when the supplier returns `null`.

## getHeight(H)

JOID calls it with the current handle.
Return its height in pixels.

## isMipmapped()

If `true`, the current handle has mipmap levels.

## isMipmapped(H)

JOID calls it with the current handle.
Return `true` when it has mipmap levels.

## getHandle()

Returns the current handle of the host, or `null`.

## isValid()

If `true`, the supplier returns a handle that names a texture of the host.

## isValid(H)

JOID calls it with the current handle.
Return `true` when it names a live texture.

## getSupplier()

Returns the supplier of the handle.

## mipmap(boolean)

Does nothing: the host decides the levels of its texture.

## allocate(int,int)

Throws an `UnsupportedOperationException`: JOID never allocates a texture of the host.

## upload(int[])

Throws an `UnsupportedOperationException`: JOID never writes a texture of the host.

## delete()

Does nothing: the host deletes its texture.
