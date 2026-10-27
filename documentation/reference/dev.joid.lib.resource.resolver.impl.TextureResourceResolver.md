---
summary: The resolver that turns a texture of the render bridge, owned or borrowed, into a resource.
---

Learn: [How Resource.of picks a resolver](/build/resource-resolvers/#how-resourceof-picks-a-resolver).

## supports(Object)

If `true`, the input is an [[ITexture]].

## resolve(ResourceBuilder,Object)

Returns the resource that shows the texture as it is, without decoding; the same texture gives the same resource while it lives.
