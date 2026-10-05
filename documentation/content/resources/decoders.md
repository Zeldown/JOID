# Decoders

The low-level objects that turn bytes into GPU textures. You rarely instantiate them directly — `ResourceFormat.decoder(asset)` picks one from the first bytes of an [asset](assets.md), see [Formats](formats.md) — but knowing the API helps when writing custom decoders.

A decoder holds its asset, not its bytes: nothing is read before `decode(...)` runs, on the resource worker.

## `IResourceDecoder`

The contract:

```java
public interface IResourceDecoder {
    default public void init(final @NonNull ResourceData resource) {}
    default public void prepare(final @NonNull ResourceData resource) {}
    default public void decode(final @NonNull ResourceData resource) {}
    default public void upload(final @NonNull ResourceData resource) {}
    default public void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async) {}
    default public void update(final @NonNull ResourceData resource) {}
    default public void clear(final @NonNull ResourceData resource) {}
    default public boolean isSettled() { return true; }
    default public boolean isScalable() { return false; }
}
```

Lifecycle:

1. **`init`** — right after construction, with the `ResourceData` parent attached.
2. **`prepare`** — called on the render thread before decode. Create placeholder textures here with `BridgeHandler.RENDER.get().createTexture()`.
3. **`decode`** — decode bytes into pixels. May run on a background thread (async mode).
4. **`upload`** — render thread. Upload decoded pixels through `ITexture.allocate` and `upload`.
5. **`request`** — render thread, before every draw. Receives the size the resource covers on screen, in pixels, every transform included, and whether the resource is async. Raster decoders ignore it; the vector decoder renders at that size.
6. **`update`** — every frame before the resource is rendered. Swap the current texture with `resource.texture(...)` when it changes (video and animation frames).
7. **`clear`** — release GPU resources when the cache evicts or the node is destroyed.

`isScalable()` tells whether the decoder renders at the size `request(...)` receives, like the vector decoder: such a resource never gets automatic mipmaps, as it is never minified.

`isSettled()` tells whether the decoder still has work that will change the image — a video frame not caught up yet, a vector size still rendering. Snapshot tests wait for every decoder to settle before a shot.

## Built-in decoders

### `RasterResourceDecoder`

Still images: PNG, JPG, BMP and every format ImageIO reads, plus still WebP through an embedded TwelveMonkeys reader. Single texture.

```java
new RasterResourceDecoder(Asset);                    // read with ImageIO, lazily, in decode()
new RasterResourceDecoder(Asset, ImageReaderSpi);    // read with a given ImageIO reader
new RasterResourceDecoder(BufferedImage);            // from a pre-decoded image
```

Transparent pixels take the color of their nearest visible pixel, so linear filtering never darkens the edges of the image.

### `AnimatedResourceDecoder`

GIF, APNG and animated WebP. An `IResourceAnimationReader` decodes the file once into a `ResourceAnimation` — its composed frames and their durations — which the decoder plays from memory and uploads into one texture. It implements `IResourcePlayback`.

```java
new AnimatedResourceDecoder(Asset, new GifResourceAnimationReader());
new AnimatedResourceDecoder(Asset, new ApngResourceAnimationReader());
new AnimatedResourceDecoder(Asset, new WebpResourceAnimationReader());
```

A reader of your own composes its frames with `ResourceAnimationCanvas`, which applies the blending (`SOURCE`, `OVER`) and the disposal (`NONE`, `BACKGROUND`, `PREVIOUS`) of each frame.

### `VectorResourceDecoder`

SVG through JSVG. The document is parsed once, then rendered at the size `request(...)` receives, with a cache of the last sizes. See [Formats](formats.md#svg).

### `VideoResourceDecoder`

Video formats — MP4, MOV, WebM, MKV, AVI. Backed by FFmpeg via JavaCV. A VP8 or VP9 WebM with an alpha channel keeps its transparency.

Features:
- Ring-buffered frame queue (5 frames).
- Ping-pong textures for tear-free playback.
- Audio streaming through the audio bridge, synced to video.
- Seek, pause, resume, loop through `IResourcePlayback`.
- Optional 3D spatial audio.

```java
new VideoResourceDecoder(Asset);
new VideoResourceDecoder(File);
```

For playback control, wrap in a [VideoPlayerNode](../nodes/design/video-player.md) — it exposes `play / pause / seek / volume` and progress callbacks.

Control the playback of any animated resource through `Resource.getPlayback()`:

```java
resource.getPlayback().ifPresent(playback -> playback.seek(10D).play());
double progress = resource.getPlayback().map(IResourcePlayback::getProgress).orElse(0D);
```

## Writing a custom decoder

Implement `IResourceDecoder`, then route bytes to it with an `IResourceFormat` registered through `ResourceFormat.register(...)` — see [Formats](formats.md#adding-a-format):

```java
public class QoiResourceDecoder implements IResourceDecoder {

    private final Asset asset;
    private int[] pixels;
    private int width, height;

    public QoiResourceDecoder(final Asset asset) {
        this.asset = asset;
    }

    @Override
    public void prepare(final ResourceData resource) {
        resource.texture(BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1));
    }

    @Override
    public void decode(final ResourceData resource) {
        try (InputStream stream = this.asset.open()) {
            final QoiImage image = Qoi.read(stream);
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.pixels = image.getArgb();
        } catch (final IOException exception) {
            throw new RuntimeException("Unable to read " + this.asset.getUniqueId(), exception);
        }
        resource.width(this.width).height(this.height).data(new int[][] {this.pixels});
    }

    @Override
    public void upload(final ResourceData resource) {
        resource.getTextures()[0].allocate(this.width, this.height).upload(this.pixels, this.width, this.height);
    }

    @Override
    public void clear(final ResourceData resource) {
        this.pixels = null;
    }

}
```

## Best practices

- **Create textures through the render bridge.** `createTexture()`, `allocate` and `upload` work on every backend.
- **Open the asset in `decode`.** Read it there and drop the stream; the decoder keeps the asset, not its bytes.
- **Keep texture calls in `prepare`, `upload`, `request` and `update`.** These run on the render thread; `decode` can run on any thread.

## See also

- [Formats](formats.md) — every supported format and how it is recognized.
- [ResourceBuilder](resource-builder.md).
- [Resolvers](resolvers.md) — the dispatch layer that selects which decoder to use.
- [ResourceNode](../nodes/design/resource.md).
- [VideoPlayerNode](../nodes/design/video-player.md).