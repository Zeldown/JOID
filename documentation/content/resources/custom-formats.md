# Custom Formats and Decoders

Every step of the resource pipeline is open: you can add a format JOID does not detect, write a decoder for a new kind of content, build animations from your own frames, or accept a new kind of in-memory input. Use this page when [Supported Formats](formats.md) does not cover your content.

## The loading pipeline

`ResourceBuilder.of(input)` goes through these steps:

| Step | Extension point | Role |
|---|---|---|
| 1. Resolver | `IResourceResolver`, registered in `ResourceResolver` | Turns an input that is already in memory (`BufferedImage`, `ITexture`) into a resource. When one supports the input, the next steps are skipped. |
| 2. Asset | `IAssetLocator`, registered in `AssetLocator` | Turns the input into an `Asset`, a source of bytes. See [Assets](assets.md). |
| 3. Format | `IResourceFormat`, registered in `ResourceFormat` | Reads the first 512 bytes of the asset and chooses the decoder. |
| 4. Decoder | `IResourceDecoder` | Decodes the bytes into pixels, uploads them into textures and updates them over time. |
| 5. Data | `ResourceData` | Holds the decoder, the textures and the state, cached and shared by every `Resource` of the same unique id. |

## Adding a format with IResourceFormat

A format recognizes its content from a header and returns the decoder. This one sends Ogg files, which JOID does not detect, to the video decoder:

```java
import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.resource.dto.format.IResourceFormat;

public final class OggResourceFormat implements IResourceFormat {

    @Override
    public boolean supports(final byte[] header) {
        return header.length >= 4 && header[0] == 'O' && header[1] == 'g' && header[2] == 'g' && header[3] == 'S';
    }

    @Override
    public IResourceDecoder decoder(final Asset asset, final byte[] header) {
        return new VideoResourceDecoder(asset);
    }

}
```

Register it once at startup:

```java
ResourceFormat.register(new OggResourceFormat());
```

| Method of `IResourceFormat` | Description |
|---|---|
| `supports(byte[] header)` | `true` when the content is this format. `header` holds the first 512 bytes, fewer for a shorter content, none when the asset cannot be opened. |
| `decoder(Asset asset, byte[] header)` | A new decoder for the asset. Called once per decoded `ResourceData`. |

| Method of `ResourceFormat` (`dev.joid.lib.resource.dto.format`) | Description |
|---|---|
| `static register(IResourceFormat format)` | Adds a format in front of the others: the latest registered is asked first, before every built-in format. |
| `static decoder(Asset asset)` | Runs the detection on an asset and returns the decoder, a `RasterResourceDecoder` when no format matches. |

## Writing a decoder with IResourceDecoder

An `IResourceDecoder` (`dev.joid.lib.resource.dto.decoder`) turns an asset into textures. JOID calls its methods in this order:

| Method | When | Thread | What to do |
|---|---|---|---|
| `init(ResourceData)` | When the `ResourceData` is created with this decoder. | the thread that creates the data | Nothing heavy: the content may never be drawn. |
| `prepare(ResourceData)` | At the first draw, before `decode`. | render thread | Create the textures with `BridgeHandler.RENDER.get().createTexture()` and give the data a placeholder, usually a 1×1 transparent texture, with `resource.texture(...)`. |
| `decode(ResourceData)` | Right after `prepare`. | `ResourceAsync/<n>` pool when the resource is asynchronous, render thread when it is blocking | Read the asset, call `resource.width(...)` and `resource.height(...)`, and store ARGB pixels with `resource.data(new int[][] {pixels})`. Never touch the GPU here. Throw to report a failure. |
| `upload(ResourceData)` | At the first draw after `decode`, when `getData()[0]` is not `null`. | render thread | Allocate the textures at the decoded size and upload the pixels. The data then drops its pixels and turns `isUploaded()` to `true`. |
| `update(ResourceData)` | At every draw, after `prepare` and `upload`. | render thread | Advance an animation, swap the displayed texture with `resource.texture(...)`, upload work done in the background. |
| `request(ResourceData, int width, int height, boolean async)` | At every draw through `DrawUtils.RESOURCE` without texture coordinates, with the size in window pixels. | render thread | Adapt the resolution. Default: nothing. |
| `clear(ResourceData)` | When the resource is cleared, after the data deleted the textures of its array. | the calling thread | Release what the data does not own: files, threads, extra textures. |
| `isSettled()` | When a tool waits for a stable picture, such as the [testkit](../integration/testkit.md). | any | `false` while background work will change the picture although the clock does not move. Default: `true`. |
| `isMipmappable()` | Before mipmaps are generated. | render thread | `false` to never mipmap the textures. Default: `true`. |

Pixels are `int` values in ARGB order (`0xAARRGGBB`), row by row from the top-left corner, as returned by `BufferedImage.getRGB`.

This decoder reads binary PPM images (`P6`, 8 bits per channel), a format JOID does not support:

```java
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;

public final class PpmResourceDecoder implements IResourceDecoder {

    private final Asset asset;

    private ITexture texture;

    public PpmResourceDecoder(final Asset asset) {
        this.asset = asset;
    }

    @Override
    public void init(final ResourceData resource) {}

    @Override
    public void prepare(final ResourceData resource) {
        this.texture = BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1);
        resource.texture(this.texture);
    }

    @Override
    public void decode(final ResourceData resource) {
        try (InputStream stream = new BufferedInputStream(this.asset.open())) {
            PpmResourceDecoder.token(stream);
            final int width = Integer.parseInt(PpmResourceDecoder.token(stream));
            final int height = Integer.parseInt(PpmResourceDecoder.token(stream));
            final int maximum = Integer.parseInt(PpmResourceDecoder.token(stream));

            final int[] pixels = new int[width * height];
            for (int i = 0; i < pixels.length; i++) {
                final int red = stream.read() * 255 / maximum;
                final int green = stream.read() * 255 / maximum;
                final int blue = stream.read() * 255 / maximum;
                pixels[i] = 0xFF000000 | red << 16 | green << 8 | blue;
            }

            resource.width(width).height(height).data(new int[][] {pixels});
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public void upload(final ResourceData resource) {
        this.texture.allocate(resource.getWidth(), resource.getHeight()).upload(resource.getData()[0], resource.getWidth(), resource.getHeight());
    }

    @Override
    public void update(final ResourceData resource) {}

    @Override
    public void clear(final ResourceData resource) {}

    private static String token(final InputStream stream) throws IOException {
        int read = stream.read();
        while (read == '#' || Character.isWhitespace(read)) {
            if (read == '#') {
                while (read != '\n' && read != -1) {
                    read = stream.read();
                }
            }
            read = stream.read();
        }

        final StringBuilder token = new StringBuilder();
        while (read != -1 && !Character.isWhitespace(read)) {
            token.append((char) read);
            read = stream.read();
        }
        return token.toString();
    }

}
```

Its format checks the `P6` magic number followed by a whitespace:

```java
import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.resource.dto.format.IResourceFormat;

public final class PpmResourceFormat implements IResourceFormat {

    @Override
    public boolean supports(final byte[] header) {
        return header.length >= 3 && header[0] == 'P' && header[1] == '6' && Character.isWhitespace(header[2]);
    }

    @Override
    public IResourceDecoder decoder(final Asset asset, final byte[] header) {
        return new PpmResourceDecoder(asset);
    }

}
```

```java
ResourceFormat.register(new PpmResourceFormat());

ResourceNode.create(0, 0, 256, 256).resource(Resource.of(new File("render.ppm"))).attach(this);
```

### Using a decoder without a format

`ResourceBuilder.compute` creates a resource around a decoder you build yourself, without format detection. The unique id keys the cache, so pick one that cannot collide with another source. This loads a large video in place, without the temporary copy that format detection makes:

```java
final File file = new File("videos/archive.mkv");
final String uniqueId = "video:" + file.getAbsolutePath();
final Resource video = ResourceBuilder
    .create()
    .async()
    .linear()
    .compute(uniqueId, () -> new ResourceData(uniqueId, new VideoResourceDecoder(file)));
```

`Resource.decoder(IResourceDecoder)` replaces the decoder of an existing resource, but does not call `init` on it: prefer `compute`.

### Built-in decoders

| Decoder | Constructors | Notes |
|---|---|---|
| `RasterResourceDecoder` | `(Asset asset)`, `(Asset asset, ImageReaderSpi reader)`, `(BufferedImage image)` | Decodes with `ImageIO`, with a given ImageIO reader, or from an image in memory. One texture. |
| `AnimatedResourceDecoder` | `(Asset asset, IResourceAnimationReader reader)` | Reads every frame with `reader`, implements `IResourcePlayback`. |
| `VectorResourceDecoder` | `(Asset asset)` | SVG rendered at the requested size, never mipmapped. |
| `VideoResourceDecoder` | `(Asset asset)`, `(File file)` | FFmpeg video with audio, implements `IResourcePlayback`. |

See [Supported Formats](formats.md) for their behavior and [Playback](playback.md) for the playback API.

## Animations with IResourceAnimationReader

`AnimatedResourceDecoder` plays any animation that an `IResourceAnimationReader` (`dev.joid.lib.resource.dto.animation`) can read. A reader returns the whole animation at once:

| Class | Members |
|---|---|
| `IResourceAnimationReader` | `read(InputStream stream)`: the animation, or an `IOException`. |
| `ResourceAnimation` | `static create(int width, int height, int plays, List<ResourceAnimationFrame> frames)`: `plays` is the number of plays, `0` to loop forever; throws an `IllegalArgumentException` without frames. `getWidth()`, `getHeight()`, `getPlays()`, `getFrames()`, `getDuration()` (milliseconds), `indexAt(long time)` (frame shown at `time` milliseconds). |
| `ResourceAnimationFrame` | `static create(int[] pixels, long duration)`: full-size ARGB pixels and a duration in milliseconds; frames shorter than 10 ms last 100 ms. `getPixels()`, `getDuration()`. |
| `ResourceAnimationCanvas` | `static create(int width, int height)`, then `compose(int[] frame, int x, int y, int frameWidth, int frameHeight, Blend blend, Disposal disposal)`: draws a partial frame at its offset and returns a copy of the whole canvas, transparent pixels bled for clean linear filtering. |
| `ResourceAnimationCanvas.Blend` | `SOURCE` replaces the canvas pixels, `OVER` blends the frame over them. |
| `ResourceAnimationCanvas.Disposal` | What the canvas becomes before the next frame: `NONE` keeps the frame, `BACKGROUND` clears its area to transparent, `PREVIOUS` restores the canvas as it was before the frame. |

This reader plays a horizontal sprite strip, where every frame has the same width:

```java
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import dev.joid.lib.resource.dto.animation.IResourceAnimationReader;
import dev.joid.lib.resource.dto.animation.ResourceAnimation;
import dev.joid.lib.resource.dto.animation.ResourceAnimationCanvas;
import dev.joid.lib.resource.dto.animation.ResourceAnimationCanvas.Blend;
import dev.joid.lib.resource.dto.animation.ResourceAnimationCanvas.Disposal;
import dev.joid.lib.resource.dto.animation.ResourceAnimationFrame;

public final class StripAnimationReader implements IResourceAnimationReader {

    private final int  frameCount;
    private final long frameDuration;

    public StripAnimationReader(final int frameCount, final long frameDuration) {
        this.frameCount = frameCount;
        this.frameDuration = frameDuration;
    }

    @Override
    public ResourceAnimation read(final InputStream stream) throws IOException {
        final BufferedImage strip = ImageIO.read(stream);
        if (strip == null) {
            throw new IOException("Unreadable sprite strip");
        }

        final int width = strip.getWidth() / this.frameCount;
        final int height = strip.getHeight();
        final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(width, height);
        final List<ResourceAnimationFrame> frames = new ArrayList<>();
        for (int i = 0; i < this.frameCount; i++) {
            final int[] pixels = strip.getRGB(i * width, 0, width, height, null, 0, width);
            frames.add(ResourceAnimationFrame.create(canvas.compose(pixels, 0, 0, width, height, Blend.SOURCE, Disposal.NONE), this.frameDuration));
        }
        return ResourceAnimation.create(width, height, 0, frames);
    }

}
```

```java
final File file = new File("sprites/coin.png");
final String uniqueId = "strip:" + file.getAbsolutePath();
final Resource coin = ResourceBuilder
    .create()
    .async()
    .nearest()
    .compute(uniqueId, () -> new ResourceData(uniqueId, new AnimatedResourceDecoder(FileAsset.create(file), new StripAnimationReader(8, 80L))));

ResourceNode.create(0, 0, 32, 32).resource(coin).attach(this);
```

The built-in readers are `GifResourceAnimationReader`, `ApngResourceAnimationReader` and `WebpResourceAnimationReader` (`dev.joid.lib.resource.dto.animation.impl`). Their static helpers tell formats apart from a header: `ApngResourceAnimationReader.isAnimated(byte[])` returns `Boolean.TRUE` or `Boolean.FALSE`, or `null` when the bytes end before the answer; `WebpResourceAnimationReader.isWebp(byte[])` and `isAnimated(byte[])` return booleans.

## Resolvers for in-memory inputs

An `IResourceResolver` (`dev.joid.lib.resource.dto.resolver`) handles inputs that need no asset, because they are already decoded. Resolvers are asked before asset locators.

| Built-in resolver | Input | Result | Unique id |
|---|---|---|---|
| `TextureResourceResolver` | `ITexture` | Wraps the texture, without decoder. | `texture_` and the identity hash of the texture |
| `BufferedImageResourceResolver` | `BufferedImage` | `RasterResourceDecoder` over the image. | the image's `toString()` |

This resolver accepts Swing icons:

```java
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.Icon;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.dto.resolver.IResourceResolver;

public final class IconResourceResolver implements IResourceResolver {

    @Override
    public boolean supports(final Object input) {
        return input instanceof Icon;
    }

    @Override
    public Resource resolve(final ResourceBuilder builder, final Object input, final Consumer<Resource> callback) {
        final Icon icon = (Icon) input;
        final String uniqueId = "icon@" + Integer.toHexString(System.identityHashCode(icon));
        final Resource resource = builder.compute(uniqueId, () -> {
            final BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = image.createGraphics();
            icon.paintIcon(null, graphics, 0, 0);
            graphics.dispose();
            return new ResourceData(uniqueId, new RasterResourceDecoder(image));
        });

        if (callback != null) {
            callback.accept(resource);
        }
        return resource;
    }

}
```

```java
ResourceResolver.register(new IconResourceResolver());

final Resource folder = Resource.of(UIManager.getIcon("FileView.directoryIcon"));
```

A resolver creates its resource through `builder.compute`, so the cache and the properties of the builder apply, and calls the callback itself when it is not `null`.

| Method of `IResourceResolver` | Description |
|---|---|
| `supports(Object input)` | `true` when this resolver handles `input`. |
| `resolve(ResourceBuilder builder, Object input, Consumer<Resource> callback)` | The resource for `input`. `callback` may be `null`. |

| Method of `ResourceResolver` | Description |
|---|---|
| `static register(IResourceResolver resolver)` | Adds a resolver in front of the others: the latest registered is asked first. |
| `static supports(Object input)` | `true` when a registered resolver supports `input`. |
| `static resolve(ResourceBuilder builder, Object input, Consumer<Resource> callback)` | Resolves with the first resolver that supports `input`; throws an `IllegalArgumentException` when none does. |

## ResourceData reference

`ResourceData` (`dev.joid.lib.resource.dto`) is the state shared by every `Resource` with the same unique id. Decoders fill it; `Resource.getResourceData()` returns it.

| Method | Description |
|---|---|
| `new ResourceData(String uniqueId, IResourceDecoder decoder)` | Creates the data and calls `decoder.init(this)` when `decoder` is not `null`. |
| `uniqueId(String)` / `getUniqueId()` | The id. |
| `decoder(IResourceDecoder)` / `getDecoder()` | The decoder, `null` for a wrapped texture. |
| `getDecoder(Class<T> clazz)` | The decoder when it is an instance of `clazz`, `null` otherwise. |
| `texture(ITexture)` / `textures(ITexture[])` / `getTextures()` | The textures drawn; `Resource.getTexture()` returns the first. |
| `data(int[][])` / `getData()` | ARGB pixels waiting for upload, one array per texture. |
| `width(int)` / `height(int)` / `getWidth()` / `getHeight()` | Size of the content in pixels. |
| `generated(boolean)` / `loaded(boolean)` / `uploaded(boolean)` and `isGenerated()` / `isLoaded()` / `isUploaded()` | Lifecycle flags. |
| `generate(boolean async)` | Calls `prepare`, then `decode` on the `ResourceAsync` pool or at once, and marks the data loaded. Without decoder, takes the size of its first texture and marks it loaded. |
| `upload()` | Calls the decoder's `upload` (without decoder, uploads `getData()[i]` into texture `i`), marks the data uploaded and drops the pixels. Does nothing without pixels. |
| `dispatch(Runnable task, boolean async)` / `await()` / `getTasks()` | Runs a task on a new daemon thread named `ResourceTask/<id>` when `async`, at once otherwise; `await()` waits for the tasks still running. |
| `clear()` | Deletes the textures, drops the pixels and calls the decoder's `clear`. |

## See also

- [Supported Formats](formats.md) — the built-in formats and their detection.
- [Resources](resources.md) — builders, cache and lifecycle.
- [Assets](assets.md) — asset locators, the step before formats.
- [Playback, Video and Audio](playback.md) — `IResourcePlayback`.