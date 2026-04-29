# Resolvers

Resolvers turn a generic `Object` input into a `Resource`. The dispatch is type-driven: each resolver answers `supports(Object)`, and the first matching resolver in the registry handles the call.

This is the extension point for plugging new resource sources into JOID — bundled assets, file paths, custom IDs, MC `ResourceLocation`, anything you can map to bytes.

## `IResourceResolver`

The contract:

```java
public interface IResourceResolver {

    public boolean supports(final @NonNull Object input);

    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback);

}
```

- **`supports(input)`** — return `true` when this resolver can handle the input. Typically a single `instanceof` check.
- **`resolve(builder, input, callback)`** — produce a cached `Resource` via `builder.compute(...)`. If `callback` is non-null, fire it once the resource is ready (or immediately for sync loads).

A resolver never instantiates `Resource` directly — it returns a `Supplier<ResourceData>` to `builder.compute(...)`, which handles caching and construction.

## Built-in resolvers

JOID ships with four resolvers, registered automatically:

| Resolver | Input type | Behavior |
|---|---|---|
| `InputStreamResourceResolver` | `InputStream` | Magic-byte detection → image or video. Sync. |
| `BufferedImageResourceResolver` | `BufferedImage` | Wraps the image in `ImageResourceDecoder`. Sync. |
| `UrlResourceResolver` | `String` | Downloads on a thread (with HTTPS → HTTP fallback). Async. |
| `TextureIdResourceResolver` | `Integer` | Wraps an existing GL texture id with no decoder. Sync. |

Built-ins are registered in `ResourceResolver`'s static initializer and live at the bottom of the registry — meaning your custom resolvers always take precedence.

## Registering a custom resolver

Call once at app startup, before any `Resource.of(...)` is used:

```java
ResourceResolver.register(new MyCustomResolver());
```

`register(...)` adds the resolver at the **front** of the queue — newer registrations beat older ones for inputs both can match.

## Writing a resolver

Synchronous example — wrap a custom `Asset` POJO:

```java
public class AssetResolver implements IResourceResolver {

    @Override
    public boolean supports(final @NonNull Object input) {
        return input instanceof Asset;
    }

    @Override
    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
        final Asset asset = (Asset) input;
        final String uniqueId = "asset:" + asset.getId();

        final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, ResourceDecoder.image(asset.openStream())));

        if (callback != null) {
            callback.accept(resource);
        }
        return resource;
    }

}
```

Asynchronous example — fetch over a worker thread, with the `onCreate` block firing only on cache miss:

```java
public class CdnResolver implements IResourceResolver {

    @Override
    public boolean supports(final @NonNull Object input) {
        return input instanceof CdnRequest;
    }

    @Override
    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
        final CdnRequest request = (CdnRequest) input;
        final String uniqueId = request.getCacheKey();

        return builder.compute(uniqueId, () -> new ResourceData(uniqueId, null), resource -> new CdnDownloadThread(request, stream -> {
            resource.decoder(ResourceDecoder.image(stream));
            if (callback != null) {
                callback.accept(resource);
            }
        }).start());
    }

}
```

The 3-arg `compute(uniqueId, supplier, onCreate)` runs `onCreate` only when no cache entry existed for `uniqueId` — concurrent `Resource.of(sameRequest)` calls share the in-flight download instead of starting parallel ones.

## Resolution order

`ResourceResolver.resolve(builder, input, callback)` walks the list and returns the first match. If nothing matches, it throws `IllegalArgumentException("No resolver found for input of type ...")`.

Because `register(...)` prepends, the registry is walked latest-first. Default order at runtime (top → bottom):

1. Custom resolvers registered after startup (most recent wins).
2. Built-in resolvers (in JOID startup order).

If you need to override a built-in (e.g., handle `String` differently), just register your own — it'll be checked before `UrlResourceResolver`.

## Best practices

- **One resolver per input type.** Don't try to make a single resolver handle `String` and `Path`; split them. Cleaner `supports(...)` checks, easier to reason about precedence.
- **Pick a stable `uniqueId`.** It keys the cache. Two inputs that should resolve to the same texture must produce the same id; two distinct sources must not collide.
- **Use `compute(uniqueId, supplier)` for sync, `compute(uniqueId, supplier, onCreate)` for async.** Don't try to start an async fetch inside the supplier — the supplier should be cheap and produce the placeholder `ResourceData` only.
- **Register at startup.** Resolvers added after the first `Resource.of(...)` calls won't help inputs that have already been cached.

## See also

- [ResourceBuilder](resource-builder.md) — caching, `compute(...)`, builder configuration.
- [Decoders](decoders.md) — what `ResourceData.decoder` does once the resolver is done.