# Resources

A `Resource` (`dev.joid.lib.resource`) is an image, an animation, a vector graphic or a video that JOID decodes into textures and draws. You create it from a file, a URL, a stream or any other source: JOID detects the format from the content, decodes it the first time it is drawn, uploads it to the GPU and shares the decoded data between every `Resource` that points at the same source.

## Loading a resource with Resource.of

```java
import java.io.File;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;

public class UIGallery extends UI {

    @Override
    public void init() {
        final Resource banner = Resource.of(new File("images/banner.png"));
        final Resource avatar = Resource.of("https://placehold.co/400x400.png");
        final Resource icon = Resource.of(UIGallery.class.getResourceAsStream("/assets/icons/play.svg"));

        ResourceNode.create(10, 10, 640, 160).resource(banner).attach(this);
        ResourceNode.create(10, 180, 100, 100).resource(avatar).attach(this);
        ResourceNode.create(120, 180, 24, 24).resource(icon).attach(this);
    }

}
```

`Resource.of` returns at once: nothing is read until the resource is drawn for the first time, and a `ResourceNode` draws a loading placeholder until the pixels are ready.

`Resource.of` accepts these inputs:

| Input | Handled as | Unique id |
|---|---|---|
| `File` | [`FileAsset`](assets.md), read from the disk | the absolute path |
| `String` | [`UrlAsset`](assets.md), downloaded over HTTP or HTTPS | the URL |
| `InputStream` | [`StreamAsset`](assets.md), read once | the stream's `toString()`, so two streams never share data |
| `Asset` | the asset itself | its `getUniqueId()` |
| `BufferedImage` | decoded from memory | the image's `toString()` |
| `ITexture` | wrapped as is, without decoding | `texture_` followed by its identity hash |
| any other object | a registered [locator or resolver](custom-formats.md#resolvers-for-in-memory-inputs) | chosen by the locator or resolver |

An input that nothing accepts throws an `IllegalArgumentException`, and `null` throws a `NullPointerException`.

> WARNING: A `String` is always a URL. To load a file from a path, pass `new File(path)`; to load a file of your jar, pass `getResourceAsStream(path)` or an [`Asset`](assets.md) of your own.

The format (PNG, JPEG, SVG, GIF, APNG, WebP, MP4, WebM...) is detected from the first bytes of the content, never from the file name. See [Supported Formats](formats.md).

## Drawing a resource

| Where | How |
|---|---|
| A node in a UI | [`ResourceNode`](../nodes/visual/resource.md) for images, SVG and animations, with stretch modes and a hovered variant. |
| A video or a controllable animation | [`ResourcePlayerNode`](../nodes/visual/resource-player.md), with play, pause, seek, volume and callbacks. |
| A `draw` hook of your own | `DrawUtils.RESOURCE.drawResource(x, y, width, height, resource)`, see [Drawing Resources](../drawing/resources.md). |

## Changing the options of a resource

Every `Resource` has its own [`ResourceProperties`](#per-resource-properties-with-resourceproperties). Chain the options right after `of`:

```java
final Resource tiles = Resource.of(new File("sprites/tiles.png")).nearest().mipmap(false);
```

`Resource.of` uses a default builder with these values:

| Option | Default with `Resource.of` | Default with `ResourceBuilder.create()` |
|---|---|---|
| Decoding | asynchronous | blocking |
| Interpolation | `TextureFilter.LINEAR` | `TextureFilter.NEAREST` |
| Mipmaps | automatic | automatic |
| Texture coordinates | none (whole image) | none (whole image) |
| Cache | `ResourceBuilder.DEFAULT_CACHE` | `ResourceBuilder.DEFAULT_CACHE` |

### Async and blocking decoding

- `async()` decodes the pixels on a pool of 16 daemon threads named `ResourceAsync/<n>`. The render thread never waits: the resource draws its transparent placeholder texture until the pixels are uploaded.
- `blocking()` decodes on the thread that draws the resource, so the image appears on its first frame. On a `Resource`, `blocking()` also waits for the tasks its data already started, such as choosing the decoder of a URL.

A blocking resource loaded from a URL downloads on the thread that creates it and on the thread that draws it. Keep URLs asynchronous.

### Interpolation with linear and nearest

`linear()` smooths the texture when it is scaled; `nearest()` keeps hard pixel edges, for pixel art. `interpolation(TextureFilter)` sets either value of `TextureFilter` (`dev.joid.lib.bridge.render.texture`).

### Mipmaps

`mipmap(true)` generates mipmaps, which keep a texture drawn much smaller than its size from shimmering; `mipmap(false)` never generates them. When you set neither, the first draw decides: a resource drawn with linear interpolation at a size smaller than its texture turns mipmaps on and keeps them. Mipmaps only change the result with linear interpolation. Vector resources (SVG) are re-rendered at the drawn size instead and are never mipmapped (`isMipmappable()` returns `false`).

### Sprites with textureCoords

`textureCoords(u, v, width, height)` draws only a region of the source, given in source pixels. Load the sheet once per sprite: the decoded data is shared through the cache, and each `Resource` keeps its own region.

```java
final File sheet = new File("sprites/sheet.png");
ResourceNode.create(0, 0, 64, 64).resource(Resource.of(sheet).nearest().textureCoords(0, 0, 32, 32)).attach(this);
ResourceNode.create(70, 0, 64, 64).resource(Resource.of(sheet).nearest().textureCoords(32, 0, 32, 32)).attach(this);
```

With texture coordinates, `DrawUtils.RESOURCE.drawResource(x, y, resource)` draws the region at its size in UI units, and the resource neither asks its decoder for a resolution nor turns mipmaps on by itself. Give a `ResourceNode` an explicit size: its automatic size is the size of the whole source.

## Custom loaders with ResourceBuilder

A `ResourceBuilder` holds default properties and a cache, and creates resources with them. Create one per kind of resource and keep it in a constant:

```java
import java.io.File;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;

public final class Textures {

    private static final ResourceBuilder PIXEL_ART = ResourceBuilder.create().async().nearest().mipmap(false);

    public static Resource sprite(final String name) {
        return Textures.PIXEL_ART.of(new File("sprites/" + name + ".png"));
    }

}
```

`copy()` creates a builder with the same cache and a copy of the properties, to derive a variant:

```java
private static final ResourceBuilder ICONS = ResourceBuilder.create().async().linear();
private static final ResourceBuilder BLOCKING_ICONS = ICONS.copy().blocking();
```

Every builder is listed in `ResourceBuilder.getBuilders()`, in creation order.

## Caching and unique ids

A builder keeps the decoded data (`ResourceData`) in a Guava `Cache<String, ResourceData>`, keyed by the unique id of the input. Two `of` calls with the same id return two `Resource` objects over the same data: the source is decoded and uploaded once, and each `Resource` keeps its own properties.

```java
final Resource first = Resource.of(new File("images/logo.png"));
final Resource second = Resource.of(new File("images/logo.png")).nearest();
// first.getResourceData() == second.getResourceData()
```

| Method | Effect |
|---|---|
| `cache(Cache<String, ResourceData>)` | Uses your own cache. Every builder uses `ResourceBuilder.DEFAULT_CACHE` until you change it, so builders share their data by default. |
| `cache(null)` | Disables caching: every `of` decodes the source again. |
| `reload()` | Empties the cache of the builder. The next `of` decodes the sources again; resources created before keep their data. |
| `compute(uniqueId, supplier)` | Returns a resource over the cached data of `uniqueId`, creating it with `supplier` when it is missing. |
| `compute(uniqueId, supplier, onCreate)` | Same, and calls `onCreate` with the new resource only when the data was created. |

`ResourceBuilder.DEFAULT_CACHE` drops an entry 5 minutes after its last lookup. A dropped entry stays alive as long as a `Resource` references it; the next `of` with the same id decodes the source again. Because `reload()` empties the cache of the builder, calling it on a builder that uses `DEFAULT_CACHE` empties it for every builder that shares it.

```java
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;

import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;

public final class Thumbnails {

    public static final ResourceBuilder BUILDER = ResourceBuilder
        .create()
        .async()
        .linear()
        .cache(CacheBuilder.newBuilder().maximumSize(200).expireAfterAccess(1, TimeUnit.MINUTES).<String, ResourceData>build());

}
```

Resources created from an `InputStream` never share data, since every stream has its own id. To share a jar file between several `of` calls, give it a stable id with an [`Asset`](assets.md#loading-files-of-your-jar) of your own.

## Load callbacks

`of(input, callback)` calls `callback` with the resource once its decoder is chosen:

| Input | When the callback runs |
|---|---|
| Local input (file, stream, local asset, `BufferedImage`, `ITexture`) | At once, on the calling thread, before `of` returns. |
| Remote input (URL, an asset whose `isRemote()` is `true`), not cached | After the format is detected: on a daemon thread named `ResourceTask/<id>` when the resource is asynchronous, on the calling thread when it is blocking. |
| Remote input already cached | At once, on the calling thread. |

The callback does not mean that the pixels are loaded: check `isLoaded()` before reading the size or the pixels.

```java
Resource.of("https://placehold.co/800x400.png", resource -> System.out.println("Decoder: " + resource.getDecoder().getClass().getSimpleName()));
```

## Lifecycle

A resource goes through these steps, triggered by drawing it:

| Step | When | Thread | Visible through |
|---|---|---|---|
| Creation | `of(...)`; the decoder is chosen at once for a local input, in a task for a remote one | calling thread, or the `ResourceTask/<id>` thread | `getDecoder()` |
| Generation | first draw: the decoder creates its textures, usually a 1×1 transparent placeholder | render thread | `isGenerated()` |
| Decoding | right after generation | `ResourceAsync/<n>` pool when asynchronous, render thread when blocking | `isLoaded()`, `getWidth()`, `getHeight()`, `getData()` |
| Upload | first draw after decoding: the pixels go to the GPU and are released from memory | render thread | `isUploaded()`; `getData()` returns `null` again |
| Update | every draw | render thread | the current frame of an animation or a video, the current raster of an SVG |

A resource wrapping an `ITexture` has no decoder: it is loaded at its first draw, with the size of the texture.

If decoding fails, the resource never gets its pixels and its node keeps drawing the loading placeholder. Image decoders throw the exception on the decoding thread; the video decoder prints it to `System.err`.

## Per-resource properties with ResourceProperties

The options of a resource live in a `ResourceProperties` (`dev.joid.lib.resource.dto`). A resource starts with a copy of the properties of its builder, so changing one never changes another.

| Method on `Resource` | Effect |
|---|---|
| `getProperties()` | The properties of this resource. |
| `properties(ResourceProperties)` | Uses this object, without copying it: several resources can share one. |
| `reset()` | Replaces the properties with a fresh copy of the builder's. |
| `copy()` | A new `Resource` over the same data, with a fresh copy of the builder's properties (not of this resource's). |

## Releasing resources

`clear()` frees a resource immediately: it deletes its textures, releases its decoder (a video stops decoding and closes its file and its audio) and removes its data from the builder cache. The data is shared, so every `Resource` over the same data is released with it: do not draw any of them afterwards.

```java
banner.clear();
```

Call `clear()` on the render thread. Without it, data that no `Resource` and no cache holds any more is released automatically: once the garbage collector finds it, the next `UIBridge.draw()` deletes its textures and releases its decoder on the render thread. `ResourceData.releaseCollected()` does the same for a host that draws without a UI bridge.

Without `clear()`, the data and its textures stay alive while a `Resource` references them.

## Binding a resource in custom drawing

These methods let a custom `draw` hook, shader or effect sample a resource. `DrawUtils.RESOURCE` uses them for you.

| Method | Effect |
|---|---|
| `prepareBind()` | Generates the resource at its first call, applies its mipmap choice and uploads the decoded pixels once they are ready. |
| `bind(TextureWrap wrap, Runnable runnable)` | Prepares the resource, updates its decoder, binds its current texture with the resource's interpolation and `wrap`, runs `runnable`, then unbinds the texture, even when `runnable` throws. |
| `bindTextureOnly(TextureWrap wrap)` | Same without running anything or unbinding: call `unbind()` yourself. |
| `unbind()` | Resets the bound texture of the render bridge. |
| `request(int width, int height)` | Tells the decoder the size in window pixels the resource is drawn at. An SVG re-renders at that size; other formats ignore it. Ignored when a size is 0 or less. |
| `generate()` | Starts the generation and the decoding without drawing. Call it on the render thread: the decoder creates its textures. |
| `upload()` | Uploads the decoded pixels on the calling thread, which must be the render thread. |
| `dispatch(Runnable task)` | Runs `task` on a new daemon thread named `ResourceTask/<id>` when the resource is asynchronous, at once otherwise. `blocking()` waits for these tasks. |

## Reference

### Resource

| Method | Description |
|---|---|
| `static of(Object input)` | Creates a resource through the default builder (asynchronous, linear, `DEFAULT_CACHE`). |
| `static of(Object input, Consumer<Resource> callback)` | Same, with a [load callback](#load-callbacks). |
| `async()` / `blocking()` | Decodes on the background pool, or on the render thread. `blocking()` also waits for the dispatched tasks. Default with `of`: asynchronous. |
| `interpolation(TextureFilter)` / `linear()` / `nearest()` | Texture filtering. Default with `of`: linear. |
| `mipmap(boolean)` | Forces mipmaps on or off. Default: automatic. |
| `textureCoords(double u, double v, double width, double height)` | Draws only this region of the source, in source pixels. Default: the whole source. |
| `properties(ResourceProperties)` / `reset()` / `getProperties()` | Replaces, resets or reads the properties. |
| `copy()` | A new resource over the same data, with the builder's properties. |
| `uniqueId(String)` | Renames the shared data. The cache key does not change. |
| `decoder(IResourceDecoder)` | Replaces the decoder of the shared data. `init` is not called on it. |
| `clear()` | [Releases](#releasing-resources) the data, its textures and its cache entry. |
| `getWidth()` / `getHeight()` | Size of the source in pixels, `0` until loaded. |
| `getData()` / `getData(int index)` | Decoded ARGB pixels between decoding and upload, `null` otherwise. |
| `getTexture()` / `getTexture(int index)` | Current texture, `null` before generation. |
| `getDecoder()` | The decoder, `null` for a wrapped texture or while a URL is being detected. |
| `getPlayback()` | `Optional<IResourcePlayback>`, present for animations and videos. See [Playback](playback.md). |
| `isMipmappable()` | `false` when the decoder refuses mipmaps. |
| `getUniqueId()` | The id of the shared data. |
| `getResourceData()` | The shared `ResourceData`. See [Custom Formats](custom-formats.md#resourcedata-reference). |
| `getBuilder()` | The builder that created the resource. |
| `isGenerated()` / `isLoaded()` / `isUploaded()` | [Lifecycle](#lifecycle) state. |
| `prepareBind()`, `bind(...)`, `bindTextureOnly(...)`, `unbind()`, `request(...)`, `generate()`, `upload()`, `dispatch(...)` | [Low-level access](#binding-a-resource-in-custom-drawing). |

### ResourceBuilder

| Method | Description |
|---|---|
| `static create()` | A builder with blocking decoding, nearest interpolation, automatic mipmaps and `DEFAULT_CACHE`. |
| `static getBuilders()` | Every builder created, in creation order. |
| `static DEFAULT_CACHE` | The shared cache: entries expire 5 minutes after their last lookup. |
| `async()` / `blocking()` / `interpolation(TextureFilter)` / `linear()` / `nearest()` / `mipmap(boolean)` / `textureCoords(...)` | Default properties copied into each new resource. |
| `cache(Cache<String, ResourceData>)` | The cache of the builder, `null` to disable it. |
| `copy()` | A new builder with the same cache and a copy of the properties. |
| `of(Object input)` / `of(Object input, Consumer<Resource> callback)` | Creates a resource. |
| `compute(String uniqueId, Supplier<ResourceData> supplier)` / `compute(..., Consumer<Resource> onCreate)` | Creates a resource over cached or new data. |
| `reload()` | Empties the cache. |
| `getProperties()` / `getCache()` | The default properties and the cache. |

### ResourceProperties

| Method | Description |
|---|---|
| `static create()` | Blocking, nearest, automatic mipmaps, no texture coordinates. |
| `async()` / `blocking()` / `isAsync()` | Decoding thread. |
| `interpolation(TextureFilter)` / `linear()` / `nearest()` / `getInterpolation()` | Texture filtering. |
| `mipmap(boolean)` / `getMipmap()` | `getMipmap()` returns an empty `Optional` while the choice is automatic. |
| `textureCoords(double u, double v, double width, double height)` / `getTextureCoords()` | Region in source pixels; `getTextureCoords()` returns `{u, v, width, height}` or `null`. |
| `copy()` | A new object with the same values. |
| `copy(ResourceProperties properties)` | Copies the values of `properties` into this object. |

## See also

- [Assets](assets.md) — where the bytes come from.
- [Supported Formats](formats.md) — what JOID decodes and how it detects it.
- [Playback, Video and Audio](playback.md) — controlling animations and videos.
- [Custom Formats and Decoders](custom-formats.md) — extending the pipeline.
- [ResourceNode](../nodes/visual/resource.md) and [Drawing Resources](../drawing/resources.md) — displaying resources.