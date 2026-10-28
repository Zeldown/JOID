---
summary: The resolver that turns a `BufferedImage` into a resource; every resource of one image shares its decoded data.
---

Learn: [How Resource.of picks a resolver](/build/resource-resolvers/#how-resourceof-picks-a-resolver).

## BufferedImageResourceResolver()

Creates the resolver.

## supports(Object)

If `true`, the input is a `BufferedImage`.

## resolve(ResourceBuilder,Object)

Returns a resource of the image; while the image lives, every call shares the same decoded data, each in its own resource.
