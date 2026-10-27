---
summary: The resolver that turns a `BufferedImage` into a resource, one resource per image.
---

Learn: [How Resource.of picks a resolver](/build/resource-resolvers/#how-resourceof-picks-a-resolver).

## BufferedImageResourceResolver()

Creates the resolver.

## supports(Object)

If `true`, the input is a `BufferedImage`.

## resolve(ResourceBuilder,Object)

Returns the resource of the image, decoded once; the same image gives the same resource while it lives.
