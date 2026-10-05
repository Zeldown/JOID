# Resolvers

A resolver turns an input that is **already decoded** into a `Resource` — a `BufferedImage` you built in memory, an `ITexture` the render bridge already owns. There are no bytes to read, so these inputs never reach the [asset](assets.md) layer.

Everything that *is* bytes goes through an [asset locator](assets.md) instead. That is almost always the extension point you want.

## Which one do I need?

| Your input | Extension point |
|---|---|
| A handle naming a file — `ResourceLocation`, a CDN key, an archive entry | [`IAssetLocator`](assets.md) |
| An object already holding pixels or a texture | `IResourceResolver` |
| A format JOID cannot decode — SVG, KTX | [`IResourceDecoder`](decoders.md) behind a locator |

A locator is the cheaper answer: it is reused by fonts and by any future byte consumer, while a resolver only ever produces a `Resource`.

## `IResourceResolver`

```java
public interface IResourceResolver {

    public boolean supports(final @NonNull Object input);

    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback);

}
```

- **`supports(input)`** — `true` when this resolver handles the input. Typically a single `instanceof`.
- **`resolve(builder, input, callback)`** — produce a cached `Resource` through `builder.compute(...)`, never by instantiating `Resource` directly. Fire `callback` when the resource is ready.

## Built-in resolvers

| Resolver | Input | Behaviour |
|---|---|---|
| `BufferedImageResourceResolver` | `BufferedImage` | Wraps the image in an `RasterResourceDecoder`. |
| `TextureResourceResolver` | `ITexture` | Wraps a texture owned by the render bridge, with no decoder. |

## Resolution order

`ResourceBuilder.of(input, callback)` asks the resolvers first, and falls back to the asset layer:

```
Resource.of(handle)
├─ a resolver supports it ──────────► IResourceResolver
└─ otherwise ─► Asset.of(handle) ─► ResourceFormat.decoder(asset)
```

Registering a resolver therefore lets you take an input type over completely, including one the asset layer would otherwise claim. If nothing matches and no locator recognises the handle either, `Asset.of(...)` throws `IllegalArgumentException`.

## Registering

```java
ResourceResolver.register(new MyResolver());
```

`register(...)` prepends, so the newest registration wins. Register at startup: a resolver added after the first `Resource.of(...)` will not affect handles already cached.

## Writing a resolver

```java
public class PixelsResolver implements IResourceResolver {

    @Override
    public boolean supports(final @NonNull Object input) {
        return input instanceof Pixels;
    }

    @Override
    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
        final Pixels pixels = (Pixels) input;
        final String uniqueId = "pixels:" + pixels.getId();

        final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, new RasterResourceDecoder(pixels.toBufferedImage())));
        if (callback != null) {
            callback.accept(resource);
        }
        return resource;
    }

}
```

The 3-arg `compute(uniqueId, supplier, onCreate)` runs `onCreate` only on a cache miss, so concurrent `Resource.of(sameInput)` calls share one in-flight load instead of starting several.

## Best practices

- **Prefer a locator.** A resolver that opens a stream is doing the asset layer's job by hand, and only serves textures.
- **One input type per resolver.** Split `String` and `Path` rather than branching inside `supports(...)`.
- **Pick a stable `uniqueId`.** It keys the cache.
- **Register at startup**, before the first load.

## See also

- [Assets](assets.md) — the byte layer and its locators.
- [ResourceBuilder](resource-builder.md) — caching and `compute(...)`.
- [Decoders](decoders.md) — what runs once the resolver is done.