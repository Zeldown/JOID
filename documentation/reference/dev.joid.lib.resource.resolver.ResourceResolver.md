---
summary: The registry of resource resolvers that `Resource.of(...)` asks for inputs it cannot read itself.
---

It holds a [[TextureResourceResolver]] and a [[BufferedImageResourceResolver]] by default.

Learn: [How Resource.of picks a resolver](/build/resource-resolvers/#how-resourceof-picks-a-resolver).

## register(IResourceResolver)

Registers a resolver before the others: the last registered is asked first.

## unregister(IResourceResolver)

Removes a resolver.

## supports(Object)

If `true`, a registered resolver supports the input.

## resolve(ResourceBuilder,Object)

Returns the resource of the first resolver that supports the input.
Throws an `IllegalArgumentException` that names the type of the input when none supports it.
