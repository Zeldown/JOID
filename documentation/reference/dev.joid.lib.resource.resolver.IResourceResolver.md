---
summary: Turns an input that is not a file, a URL or an asset, such as a texture of the host, into a resource.
---

`Resource.of(...)` asks the resolvers, from the last registered, for the first that supports its input.

Learn: [Write the resolver](/build/resource-resolvers/#write-the-resolver).

## supports(Object)

If `true`, the resolver turns that input into a resource.

## resolve(ResourceBuilder,Object)

Returns the resource of the input, built with the builder; called only for an input that `supports` accepts.
