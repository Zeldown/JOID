# Decoders

The low-level objects that turn bytes into GPU textures. You rarely instantiate them directly — `ResourceBuilder` picks the right one based on magic bytes — but knowing the API helps when writing custom decoders.

## `IResourceDecoder`

The contract:

```java
public interface IResourceDecoder {
    default public void init(final @NonNull ResourceData resource) {}
    default public void prepare(final @NonNull ResourceData resource) {}
    default public void decode(final @NonNull ResourceData resource) {}
    default public void upload(final @NonNull ResourceData resource) {}
    default public void bind(final @NonNull ResourceData resource) {}
    default public void clear(final @NonNull ResourceData resource) {}
}
```

Lifecycle:

1. **`init`** — right after construction, with the `ResourceData` parent attached.
2. **`prepare`** — called on the GL thread before decode. Allocate placeholder textures here.
3. **`decode`** — decode bytes into pixels. May run on a background thread (async mode).
4. **`upload`** — GL thread. Upload decoded pixels to GPU.
5. **`bind`** — every frame when the resource is rendered. Bind the current texture.
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
- OpenAL audio streaming synced to video.
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
        final int tex = GL11.glGenTextures();
        AllocatedTextureUtil.allocateTexture(tex, width, height);
        AllocatedTextureUtil.uploadTexture(tex, pixels, width, height);
        resource.textureId(tex);
    }

    @Override
    public void bind(ResourceData resource) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, resource.getTextureId()[0]);
    }

    @Override
    public void clear(ResourceData resource) {
        if (resource.getTextureId() != null) {
            GL11.glDeleteTextures(resource.getTextureId()[0]);
        }
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

- **Use `AllocatedTextureUtil`.** Handles GL texture allocation and mipmap setup consistently.
- **Don't hold the InputStream forever.** Consume it during `decode` and drop the reference.
- **Guard GL calls in `upload` and `clear` by GL thread.** These run on the render thread; others can be any thread.

## See also

- [ResourceBuilder](resource-builder.md).
- [ResourceNode](../nodes/design/resource.md).
- [VideoPlayerNode](../nodes/design/video-player.md).
