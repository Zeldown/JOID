---
summary: The resolver that turns a texture of the render bridge, owned or borrowed, into a resource.
---

Learn: [How Resource.of picks a resolver](/build/resource-resolvers/#how-resourceof-picks-a-resolver).

## TextureResourceResolver()

Creates the resolver.

## supports(Object)

If `true`, the input is an [[ITexture]].

## resolve(ResourceBuilder,Object)

Returns a resource that shows the texture as it is, without decoding; while the texture lives, every call shares the same data, each in its own resource.
