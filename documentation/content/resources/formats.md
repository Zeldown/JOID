# Formats

`Resource.of(...)` reads images, animations, videos and vectors through the same call: JOID looks at the first bytes of the [asset](assets.md), never at its extension, and picks the decoder of its format. A node draws every format the same way.

```java
ResourceNode.create(0, 0, 64, 64).resource(Resource.of("/icons/check.svg")).attach(flex);
ResourceNode.create(0, 0, 160, 160).resource(Resource.of("/textures/spinner.webp")).attach(flex);
ResourceNode.create(0, 0, 256, 256).resource(Resource.of("/videos/overlay.webm")).attach(flex);
```

## Supported formats

| Format | Recognized by | Decoder |
|---|---|---|
| SVG | an `<svg>` root, after an optional BOM, XML declaration, comments and doctype | `VectorResourceDecoder` |
| WebP, still | `RIFF` … `WEBP` | `RasterResourceDecoder` |
| WebP, animated | `RIFF` … `WEBP` with the animation flag of its `VP8X` chunk | `AnimatedResourceDecoder` |
| GIF | `GIF87a` / `GIF89a` | `AnimatedResourceDecoder` |
| APNG | a PNG with an `acTL` chunk before its first `IDAT` | `AnimatedResourceDecoder` |
| MP4, MOV | `ftyp` at offset 4 | `VideoResourceDecoder` |
| WebM, MKV | the EBML header `1A 45 DF A3` | `VideoResourceDecoder` |
| AVI | `RIFF` … `AVI ` | `VideoResourceDecoder` |
| PNG, JPG, BMP and every other ImageIO format | anything else | `RasterResourceDecoder` |

`ResourceFormat.decoder(asset)` peeks the first 512 bytes of the asset, so the asset stays untouched, and asks every registered format whether it recognizes them, the last registered first.

## SVG

A vector is rendered at the **real pixel size of each draw**: the size of the node, multiplied by the interface scale, the zoom, the resolution of the window and every transform of the node — a `TransformNodeEffect` scale included. One texel of the texture lands on one pixel of the screen, so the image stays sharp at 24 px as at ×8.

- **Intrinsic size** — the `width` / `height` of the document, or its `viewBox`. A `ResourceNode` without a size takes it, like the size of an image.
- **Stretch** — drawn in a rectangle of another ratio, the document is stretched like an image.
- **Changing size** — while the size keeps changing, during a zoom animation for instance, the document is rendered at the next ×1.25 step instead of every frame, then at the exact size once the size has not moved for 200 ms. The last 8 sizes stay cached as textures.
- **Threading** — an async resource renders on a background thread and keeps showing the previous texture meanwhile. A blocking resource renders synchronously, so every draw shows the exact size.
- **Content** — JSVG renders gradients, strokes, clips, masks, patterns, text and the common filters. External resources are not loaded.

## Animations

GIF, APNG and animated WebP share `AnimatedResourceDecoder`. The file is decoded once into composed frames — the disposal and blending of each frame are applied — then played from memory on the clock of the bridge, so snapshots stay deterministic.

- **Loops** — the animation plays as many times as its file says, forever for most GIFs. `loop(true)` or `loop(false)` overrides it.
- **Durations** — a frame shorter than 10 ms lasts 100 ms, like in browsers.
- **Memory** — frames stay in memory: width × height × 4 bytes per frame. For a long or large animation, prefer a WebM, which streams its frames.

## Playback

Animations and videos implement `IPlayback`, reached through `Resource.getPlayback()`:

```java
resource.getPlayback().ifPresent(playback -> playback.loop(false).seek(0D).play());
double progress = resource.getPlayback().map(IPlayback::getProgress).orElse(0D);
```

| Method | Effect |
|---|---|
| `play()` | Starts from the beginning |
| `stop()` | Stops on the current frame |
| `pause()` / `resume()` | Freezes and resumes the playback |
| `seek(seconds)` | Jumps to a time |
| `loop(boolean)` / `autoplay(boolean)` | Repeats the playback, starts it on load |
| `isPlaying()`, `isPaused()`, `isLoop()`, `isAutoplay()` | Playback state |
| `getDuration()`, `getCurrentTime()`, `getProgress()` | Length and position, in seconds and from 0 to 1 |

A [VideoPlayerNode](../nodes/design/video-player.md) drives any of them with the same controls and callbacks: an animated WebP plays, pauses and loops like a video.

## Transparent WebM

A VP8 or VP9 WebM with an alpha channel keeps its transparency: when the stream announces its alpha, JOID decodes it with libvpx, as FFmpeg's own decoders drop it. Encode one with:

```
ffmpeg -i input.mov -c:v libvpx-vp9 -pix_fmt yuva420p overlay.webm
```

## Adding a format

Implement `IResourceFormat` and register it once, at startup:

```java
public class QoiResourceFormat implements IResourceFormat {

    @Override
    public boolean supports(final @NonNull byte[] header) {
        return header.length >= 4 && header[0] == 'q' && header[1] == 'o' && header[2] == 'i' && header[3] == 'f';
    }

    @Override
    public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
        return new QoiResourceDecoder(asset);
    }

}

ResourceFormat.register(new QoiResourceFormat());
```

A format registered later is asked first, so it can also take over a built-in one. See [Decoders](decoders.md) to write the decoder itself.

## Embedded libraries

Every JOID JAR embeds what these formats need: FFmpeg with its natives, JSVG and TwelveMonkeys ImageIO. JOID registers nothing in ImageIO. See [Installation](../getting-started/installation.md).

## See also

- [ResourceBuilder](resource-builder.md).
- [Decoders](decoders.md).
- [ResourceNode](../nodes/design/resource.md).
- [VideoPlayerNode](../nodes/design/video-player.md).