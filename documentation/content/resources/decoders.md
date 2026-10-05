# Decoders

The low-level objects that turn bytes into GPU textures. You rarely instantiate them directly — `ResourceFormat.decoder(asset)` picks one from the first bytes of an [asset](assets.md) — but knowing the API helps when writing custom decoders.

A decoder holds its asset, not its bytes: nothing is read before `decode(...)` runs, on the resource worker.

## `IResourceDecoder`

The contract:

```java
public interface IResourceDecoder {
    default public void init(final @NonNull ResourceData resource) {}
    default public void prepare(final @NonNull ResourceData resource) {}
    default public void decode(final @NonNull ResourceData resource) {}
    default public void upload(final @NonNull ResourceData resource) {}
    default public void update(final @NonNull ResourceData resource) {}
    default public void clear(final @NonNull ResourceData resource) {}
}
```

Lifecycle:

1. **`init`** — right after construction, with the `ResourceData` parent attached.
2. **`prepare`** — called on the render thread before decode. Create placeholder textures here with `BridgeHandler.RENDER.get().createTexture()`.
3. **`decode`** — decode bytes into pixels. May run on a background thread (async mode).
4. **`upload`** — render thread. Upload decoded pixels through `ITexture.allocate` and `upload`.
5. **`update`** — every frame before the resource is rendered. Swap the current texture with `resource.texture(...)` when it changes (video frames).
6. **`clear`** — release GPU resources when the cache evicts or the node is destroyed.

## Built-in decoders

### `RasterResourceDecoder`

Static image formats (PNG, JPG, BMP). Uses Java's `ImageIO`. Single texture, no animation.

```java
new RasterResourceDecoder(Asset);            // read lazily, in decode()
new RasterResourceDecoder(BufferedImage);    // from pre-decoded image
```

### `VideoResourceDecoder`

Video formats — MP4, MOV, WebM, MKV, AVI. Backed by FFmpeg via JavaCV. A VP8 or VP9 WebM with an alpha channel keeps its transparency.

Features:
- Ring-buffered frame queue (5 frames).
- Ping-pong textures for tear-free playback.
- Audio streaming through the audio bridge, synced to video.
- Seek, pause, resume, loop.
- Optional 3D spatial audio.

```java
new VideoResourceDecoder(Asset);
new VideoResourceDecoder(File);
```

For playback control, wrap in a [VideoPlayerNode](../nodes/design/video-player.md) — it exposes `play / pause / seek / volume` and progress callbacks.

Control the playback of any animated resource through `Resource.getPlayback()`:

```java
resource.getPlayback().ifPresent(playback -> playback.seek(10D).play());
double progress = resource.getPlayback().map(IPlayback::getProgress).orElse(0D);
```

## Format detection

`ResourceFormat.decoder(asset)` reads the first 512 bytes of the asset through `peek(...)`, so the asset stays untouched, and asks every registered `IResourceFormat` whether it recognizes them, the last registered first. When none does, the asset is read as a raster image.

## Writing a custom decoder

Implement `IResourceDecoder`, then route bytes to it with an `IResourceFormat` registered through `ResourceFormat.register(...)`, or construct a `ResourceData` directly:

```java
public class SVGResourceDecoder implements IResourceDecoder {

    private final InputStream stream;
    private int[] pixels;
    private int width, height;

    public SVGResourceDecoder(final InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void prepare(ResourceData resource) {
        resource.texture(BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1));
    }

    @Override
    public void decode(ResourceData resource) {
        // Parse SVG, rasterize to int[] ARGB
        final BufferedImage img = rasterize(stream);
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.pixels = extractPixels(img);
        resource.width(width).height(height);
    }

    @Override
    public void upload(ResourceData resource) {
        resource.getTextures()[0].allocate(width, height).upload(pixels, width, height);
    }

    @Override
    public void clear(ResourceData resource) {
        this.pixels = null;
    }
}
```

Wrap in a helper:

```java
public static IResourceDecoder svg(InputStream s) {
    return new SVGResourceDecoder(s);
}
```

Use it directly:

```java
new Resource(builder, new ResourceData(uniqueId, svg(stream)));
```

## Best practices

- **Create textures through the render bridge.** `createTexture()`, `allocate` and `upload` work on every backend.
- **Don't hold the InputStream forever.** Consume it during `decode` and drop the reference.
- **Keep texture calls in `prepare`, `upload` and `update`.** These run on the render thread; `decode` can run on any thread.

## See also

- [ResourceBuilder](resource-builder.md).
- [Resolvers](resolvers.md) — the dispatch layer that selects which decoder to use.
- [ResourceNode](../nodes/design/resource.md).
- [VideoPlayerNode](../nodes/design/video-player.md).
