# ResourceBuilder

The entry point for loading resources (images, videos, GIFs) into JOID. Handles caching, async loading, and dispatching to the right decoder.

## Quick load

A single static method handles every supported source type:

```java
Resource res = Resource.of(myStream);              // InputStream
Resource res = Resource.of(myImage);               // BufferedImage
Resource res = Resource.of("https://...");         // String URL — downloads async
Resource res = Resource.of(textureId);             // Integer — wraps a GL texture id

Resource res = Resource.of(input, callback);       // any of the above + notify when ready
```

The input is dispatched to the appropriate [resolver](resolvers.md) based on its runtime type. Plug in your own resolver to handle custom inputs (file paths, bundled assets, MC `ResourceLocation`, etc.) — see [Resolvers](resolvers.md).

All forms go through a default `ResourceBuilder` (`.async().linear()`) that writes into `ResourceBuilder.DEFAULT_CACHE` — a 5-minute TTL cache shared across every default load.

## Custom builder

For more control, create your own builder:

```java
final ResourceBuilder builder = ResourceBuilder.create()
    .async()                       // or .blocking()
    .linear()                      // or .nearest() — texture filtering
    .textureCoords(0, 0, 1, 1)     // custom UV mapping
    .cache(myCache);

Resource res = builder.of(myStream);
```

Setters are chainable and return the builder.

## Cache

By default, a shared `DEFAULT_CACHE` expires entries after 5 minutes of inactivity:

```java
ResourceBuilder.DEFAULT_CACHE     // global shared cache
```

Opt out or supply your own:

```java
final ResourceBuilder builder = ResourceBuilder.create().cache(null);  // no cache
final ResourceBuilder builder = ResourceBuilder.create().cache(myCache);
```

## Async loading

```java
Resource res = ResourceBuilder.create().async().of(stream);
```

With `async()`, the decode happens on a background thread pool. The node renders a skeleton placeholder until ready.

`blocking()` forces sync decode — the call blocks until the image is on the GPU. Use for startup-time assets only.

## Loading from URL

```java
Resource res = Resource.of("https://example.com/image.png");
```

The default `UrlResourceResolver` downloads the URL on a dedicated thread (with automatic HTTPS → HTTP fallback for misconfigured hosts) and routes the bytes to the right decoder. The download only fires on cache miss — back-to-back `of(sameUrl)` calls reuse the cached `Resource`.

## Magic-bytes detection

`InputStream` and URL resolvers read the first 12 bytes (or the URL extension for URLs) to pick the right decoder:

| Signature / extension | Decoder |
|---|---|
| `GIF87a` / `GIF89a` | `VideoResourceDecoder` (loop enabled) |
| `ftyp` at offset 4 | `VideoResourceDecoder` (MP4/MOV) |
| `1A 45 DF A3` | `VideoResourceDecoder` (WebM/MKV) |
| `RIFF...AVI` | `VideoResourceDecoder` |
| anything else | `ImageResourceDecoder` (ImageIO) |

You don't need to pre-classify — drop any supported format in and it Just Works.

## Resolving from inside a custom resolver

If you write a resolver, you build the cached `Resource` through `compute(...)`:

```java
public class MySourceResolver implements IResourceResolver {

    @Override
    public boolean supports(final @NonNull Object input) {
        return input instanceof MySource;
    }

    @Override
    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
        final MySource source = (MySource) input;
        final String uniqueId = source.getId();

        final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, ResourceDecoder.image(source.openStream())));

        if (callback != null) {
            callback.accept(resource);
        }
        return resource;
    }
}
```

For async loads (download / IO on a worker thread), use the 3-arg overload — the `onCreate` block runs only on cache miss, so concurrent `of(...)` calls reuse the same in-flight download:

```java
return builder.compute(url, () -> new ResourceData(url, null), resource -> new MyDownloadThread(url, stream -> {
    resource.decoder(ResourceDecoder.image(stream));
    if (callback != null) {
        callback.accept(resource);
    }
}).start());
```

## Copy the builder

```java
final ResourceBuilder base = ResourceBuilder.create().async().linear();
final ResourceBuilder nearestBuilder = base.copy().nearest();
```

`copy()` clones the builder's config for parallel variations.

## Invalidate the cache

```java
builder.reload();  // invalidates all entries in the builder's cache
```

Useful for hot-reload during dev.

## Best practices

- **Keep resources long-lived.** Loading a resource each frame defeats the cache. Store `Resource` in a field.
- **Use the URL form for user-supplied images.** It handles downloading, caching, and format detection for you.
- **Pre-load on UI open.** Fetching in `init()` kicks off async decodes while the UI appears.
- **Close streams you don't pass to JOID.** `Resource.of(InputStream)` consumes the stream — don't call `close()` on it separately.
- **Register your resolvers once at startup.** `ResourceResolver.register(myResolver)` adds the resolver to the front of the queue, so custom resolvers take precedence over the defaults.

## See also

- [Resolvers](resolvers.md) — `IResourceResolver`, registry, and writing custom resolvers for new input types.
- [Decoders](decoders.md) — `ImageResourceDecoder`, `VideoResourceDecoder`.
- [ResourceNode](../nodes/design/resource.md) — render resources as nodes.