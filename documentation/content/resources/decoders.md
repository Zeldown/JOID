# Decoders

The low-level objects that turn bytes into GPU textures. You rarely instantiate them directly — [resolvers](resolvers.md) pick the right decoder based on magic bytes or input type — but knowing the API helps when writing custom decoders.

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
2. **`prepare`** — called on the render thread before decode. Create placeholder textures here with `BridgeHandler.getRender().createTexture()`.
3. **`decode`** — decode bytes into pixels. May run on a background thread (async mode).
4. **`upload`** — render thread. Upload decoded pixels through `ITexture.allocate` and `upload`.
5. **`update`** — every frame before the resource is rendered. Swap the current texture with `resource.texture(...)` when it changes (video frames).
6. **`clear`** — release GPU resources when the cache evicts or the node is destroyed.

## Built-in decoders

### `ImageResourceDecoder`

Static image formats (PNG, JPG, BMP). Uses Java's `ImageIO`. Single texture, no animation.

```java
ResourceDecoder.image(InputStream);      // from stream
ResourceDecoder.image(BufferedImage);    // from pre-decoded image
```

### `VideoResourceDecoder`

Animated formats — MP4, MOV, WebM, MKV, AVI, GIF, APNG. Backed by FFmpeg via JavaCV.

Features:
- Ring-buffered frame queue (5 frames).
- Ping-pong textures for tear-free playback.
- Audio streaming through the audio bridge, synced to video.
- Seek, pause, resume, loop.
- Optional 3D spatial audio.

```java
ResourceDecoder.video(InputStream);
ResourceDecoder.video(InputStream, boolean loopByDefault);
ResourceDecoder.video(File);
```

For playback control, wrap in a [VideoPlayerNode](../nodes/design/video-player.md) — it exposes `play / pause / seek / volume` and progress callbacks.

Access decoder internals via `Resource.getDecoder()`:

```java
VideoResourceDecoder vrd = (VideoResourceDecoder) resource.getDecoder();
vrd.play();
vrd.seek(10D);
double progress = vrd.getProgress();
```

## Magic-bytes helpers

Static detection utilities on `VideoResourceDecoder`:

```java
VideoResourceDecoder.isVideoHeader(byte[] header, int read);   // returns true for MP4/MOV/WebM/MKV/AVI/GIF
VideoResourceDecoder.isLoopByDefault(byte[] header, int read); // returns true for GIF (loop on)
```

`ResourceBuilder.of(InputStream)` uses these to route to the right decoder.

## Writing a custom decoder

Implement `IResourceDecoder` and register it manually via `ResourceDecoder` or by directly constructing a `ResourceData`:

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
        resource.texture(BridgeHandler.getRender().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1));
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
