---
summary: A texture of the host, by OpenGL name, that JOID samples without owning it.
---

Create it after the OpenGL backend is registered; JOID reads its size from OpenGL and never deletes it.

Learn: [Show a texture of your application](/use/learn/describing-the-ui/media/#show-a-texture-of-your-application), [Lend a texture of the host](/build/resource-resolvers/#lend-a-texture-of-the-host).

## of(int)

Wraps the texture of that OpenGL name.
Throws an `IllegalStateException` when no OpenGL render bridge is registered.

## of(IntSupplier)

Wraps the texture whose name the supplier returns, read again at each use, for a host that recreates its texture.
Throws an `IllegalStateException` when no OpenGL render bridge is registered.

## getId()

Returns the current OpenGL name of the texture.

## sample(TextureSampling)

Sets the filter and the wrap of the bound texture to that sampling.

## getBridge()

Returns the OpenGL render bridge.
