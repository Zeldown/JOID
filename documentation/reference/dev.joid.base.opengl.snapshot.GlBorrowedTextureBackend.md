---
summary: The borrowed texture backend of any OpenGL context, over a snapshot backend and a binding.
---

It ships in the `snapshot` jar of base-opengl and in the dev and demo jars of the backends; the published `joid-base-opengl` jar does not hold it.

Learn: [Tools for hosts and tests](/build/base-modules/opengl/#tools-for-hosts-and-tests).

## GlBorrowedTextureBackend(ISnapshotBackend,Supplier)

Creates the backend over that snapshot backend; the supplier gives the binding once the snapshot backend has created its context.

## create(int,int)

Creates the surface of the snapshot backend, then takes the binding.

## destroy()

Deletes the textures it created, then destroys the surface.

## present()

Presents the surface of the snapshot backend.

## capture(int,int)

Captures the surface of the snapshot backend.

## getRenderer()

Returns the renderer of the snapshot backend.

## borrow(Supplier)

Returns a [[GlBorrowedTexture]] over the texture names that the supplier gives.

## isBorrowableTexture(Object)

If `true`, the object is an `Integer` that names a texture of the context.

## createBorrowableTexture(int,int,int,boolean)

Creates an RGBA8 texture of that size in that color, with nearest filtering and repeat wrapping, and every mip level when the boolean is `true`; the texture bound before stays bound.

## readBorrowableParameters(Object)

Returns the filters and the wraps of the texture as `MIN_FILTER`, `MAG_FILTER`, `WRAP_S` and `WRAP_T`; the texture bound before stays bound.
