# ResourceBuilder

The entry point for loading resources (images, videos, GIFs) into JOID. Handles caching, async loading, and decoder selection.

## Quick load

For most cases, use the static `Resource.of(...)` helpers:

```java
Resource image = Resource.of(InputStream);     // throws IOException
Resource image = Resource.of(BufferedImage);
Resource image = Resource.of(String url);       // downloads async
Resource image = Resource.of(int textureId);    // wrap an existing GL texture
```

These go through the default `ResourceBuilder` with a global 5-minute cache.

## Custom builder

For more control, create your own builder:

```java
final ResourceBuilder builder = ResourceBuilder.create()
    .async()                       // or .blocking()
    .linear()                      // or .nearest() — texture filtering
    .textureCoords(0, 0, 1, 1)     // custom UV mapping
    .cache(myCache);

Resource res = builder.of(stream);
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

## Magic-bytes detection

`of(InputStream)` reads the first 12 bytes to detect the format:

| Signature | Decoder |
|---|---|
| `GIF87a` / `GIF89a` | `VideoResourceDecoder` (loop enabled) |
| `ftyp` at offset 4 | `VideoResourceDecoder` (MP4/MOV) |
| `1A 45 DF A3` | `VideoResourceDecoder` (WebM/MKV) |
| `RIFF...AVI` | `VideoResourceDecoder` |
| anything else | `ImageResourceDecoder` (ImageIO) |

You don't need to pre-classify — drop any supported format in and it Just Works.

## Loading from URL

```java
Resource res = Resource.of("https://example.com/image.png");
```

Downloads in the background via `ResourceDownloadThread`. Automatic HTTPS → HTTP fallback for misconfigured hosts. On download, the same magic-bytes detection picks the decoder.

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

## See also

- [Decoders](decoders.md) — `ImageResourceDecoder`, `VideoResourceDecoder`.
- [ResourceNode](../nodes/design/resource.md) — render resources as nodes.
