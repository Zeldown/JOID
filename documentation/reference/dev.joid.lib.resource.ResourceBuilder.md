---
summary: Creates resources with shared default settings and a cache that makes two loads of the same input share their data.
---

[[Resource#of]] uses a default builder with linear filtering and a shared [[MemoryResourceCache]].
Make your own when a group of resources needs other defaults or another cache:

```java fragment
private static final ResourceBuilder PIXEL_ART = ResourceBuilder.create().nearest().mipmap(false);
```

Every setting returns a new builder and leaves this one unchanged.

Learn: [Media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/).

## create()

Creates a builder with the shared memory cache and linear filtering.

## getProperties()

Returns a copy of the default settings that each handle starts with.

## getBuilders()

Returns every builder still in use, as a new list.

## getCache()

Returns the cache of the builder, or `null` when it caches nothing.

## cache(IResourceCache)

Returns a builder that caches in `cache`; `null` caches nothing, so each `of` decodes the input again.

## interpolation(TextureFilter)

Returns a builder whose resources are filtered with `interpolation`.

## linear()

Returns a builder whose resources are filtered smoothly.

## nearest()

Returns a builder whose resources keep their pixels sharp.

## mipmap(boolean)

Returns a builder whose resources always (`true`) or never (`false`) get mipmaps.

## textureCoords(double,double,double,double)

Returns a builder whose resources draw the region at `u`, `v` of `width` × `height` pixels.

## of(Object)

Returns a handle on the resource of `input`: a texture of the application or another input that a resolver knows, otherwise an [[Asset]], whose format is detected from its first bytes.
A path found nowhere gives a failed resource; an input of a type that nothing knows throws `IllegalArgumentException`.

## compute(String,Supplier)

Returns a handle on the data cached under `uniqueId`, creating it with `dataSupplier` when the cache does not have it.
Use it in a resolver or for data you build yourself.

## compute(String,Supplier,Consumer)

Same as `compute(String, Supplier)`, and calls `onCreate` with the handle when the data was created rather than found in the cache.

## remove(ResourceData)

Removes `data` from the cache, under the id it was cached with, when the cache still holds it.

## reload()

Removes from the cache every data that this builder put there and that is still cached, so the next `of` reads the files again.
