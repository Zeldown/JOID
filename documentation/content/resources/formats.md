# Supported Formats

JOID decodes still images, SVG, animated GIF, APNG and WebP, and videos with their audio. It recognizes a format from the first bytes of the content, so file names and extensions never matter. This page lists what each format supports and how the detection works.

## Formats at a glance

| Format | Recognized by | Decoder | Playback |
|---|---|---|---|
| JPEG, BMP, WBMP and any format of an installed ImageIO plugin | fallback when no other format matches | `RasterResourceDecoder` | no |
| PNG | the PNG signature, without an `acTL` chunk | `RasterResourceDecoder` | no |
| APNG | the PNG signature with an `acTL` chunk before the image data | `AnimatedResourceDecoder` | yes |
| GIF (87a and 89a) | `GIF87a` or `GIF89a` | `AnimatedResourceDecoder`, even with a single frame | yes |
| WebP, still (lossy, lossless, with alpha) | `RIFF....WEBP` | `RasterResourceDecoder` with the embedded TwelveMonkeys WebP reader | no |
| WebP, animated | `RIFF....WEBP` with a `VP8X` chunk whose animation flag is set | `AnimatedResourceDecoder` | yes |
| SVG | text starting with `<svg`, after an optional BOM, XML declaration, comments and `<!DOCTYPE>` | `VectorResourceDecoder` | no |
| MP4, M4V, MOV, 3GP and other ISO media files | an `ftyp` box at byte 4 | `VideoResourceDecoder` | yes, with audio |
| Matroska, WebM | the EBML signature `1A 45 DF A3` | `VideoResourceDecoder` | yes, with audio |
| AVI | `RIFF....AVI ` | `VideoResourceDecoder` | yes, with audio |

Decoders are in `dev.joid.lib.resource.dto.decoder.impl`. Formats with playback expose an [`IResourcePlayback`](playback.md).

## How a format is detected

`ResourceFormat.decoder(asset)` (`dev.joid.lib.resource.dto.format`) reads the first 512 bytes of the asset without consuming it and asks each registered `IResourceFormat` in turn:

1. formats you registered, the latest first;
2. SVG;
3. WebP;
4. video (ISO media, Matroska and WebM, AVI);
5. PNG and APNG: when the first 512 bytes do not reach the `acTL` chunk or the image data, the first 64 KiB are read to decide;
6. GIF;
7. otherwise `RasterResourceDecoder`, which reads the content with `javax.imageio.ImageIO`.

Inputs that are already decoded (`BufferedImage`, `ITexture`) skip the detection: [resolvers](custom-formats.md#resolvers-for-in-memory-inputs) handle them first.

To support a format JOID does not recognize, or to route a container to an existing decoder, [register an `IResourceFormat`](custom-formats.md#adding-a-format-with-iresourceformat).

> NOTE: Every file with an `ftyp` box goes to the video decoder, including HEIF and AVIF still images.

## Still images

`RasterResourceDecoder` decodes the whole image into ARGB pixels and uploads it as one texture.

- Still PNG files are read with ImageIO.
- With the fallback, any format that an ImageIO reader registered in the JVM can read is supported. On Java 8 that is JPEG, BMP and WBMP (PNG and GIF have their own formats); adding an ImageIO plugin to your classpath (for example a TIFF reader) adds its formats.
- A content that no reader understands fails with `Failed to decode image, ImageIO returned null for <id>`.
- Still WebP uses the TwelveMonkeys WebP reader embedded and relocated in the JOID jars, independently of the ImageIO plugins of your classpath.

## Animated images: GIF, APNG and WebP

`AnimatedResourceDecoder` reads every frame at decoding time and composes them on a canvas of the animation size, following the frame offsets, blend and disposal operations of the file. Each composed frame is kept in memory as a full-size ARGB array (`width × height × 4` bytes per frame) for as long as the resource lives; the GPU only holds one texture, updated when the displayed frame changes.

| Aspect | Behavior |
|---|---|
| Frame duration | As stored in the file; a frame shorter than 10 ms lasts 100 ms, as in web browsers. |
| Loop count | GIF: the `NETSCAPE2.0` extension (0 loops forever, `n` plays `n + 1` times), one play without it. APNG: `num_plays` of `acTL` (0 loops forever). WebP: the loop count of `ANIM` (0 loops forever). `loop(boolean)` overrides it. |
| Start | Plays as soon as it is uploaded, unless `autoplay(false)`. |
| Timing | Follows the [clock bridge](../integration/bridges.md#iclockbridge): a paused `ManualClockBridge` freezes the animation. |

See [Playback, Video and Audio](playback.md) for the controls.

## SVG

`VectorResourceDecoder` parses the document with JSVG (embedded and relocated in the JOID jars) and renders it with antialiasing.

- The intrinsic size, returned by `getWidth()` and `getHeight()`, is the size of the document rounded up, at least 1×1 pixel.
- The first raster is rendered at the intrinsic size. Each draw then tells the decoder the size the SVG covers in window pixels, and the decoder renders a raster at that size, so the SVG stays sharp at any zoom or interface scale.
- A raster is at most 4096 pixels on its longest side.
- The decoder keeps the 8 most recently used rasters and shows one again at once when its size comes back.
- An asynchronous resource renders its rasters on a background thread named `ResourceVector/<n>`, one at a time, and keeps drawing the previous raster meanwhile. While the size keeps changing (less than 200 ms since the last change), it keeps the current raster as long as it is at least as large as needed and less than about 1.56 times wider, and otherwise renders a raster 25 % larger than needed, so that an animated size needs fewer renders. Once the size rests, it renders the exact size.
- A blocking resource renders the requested size at once.
- SVG textures are never mipmapped.

An SVG drawn with [texture coordinates](resources.md#sprites-with-texturecoords) keeps its intrinsic raster.

## Video

`VideoResourceDecoder` decodes videos with FFmpeg 6.0 through JavaCV 1.5.9. Both, with the FFmpeg natives for Windows x86-64, Linux x86-64, macOS x86-64 and macOS arm64, are part of the JOID jars (see [Installation](../getting-started/installation.md)). Video playback works on those four platforms.

| Aspect | Behavior |
|---|---|
| Containers | MP4 and other ISO media files, Matroska and WebM, AVI. Other containers (MPEG-TS, FLV, Ogg...) are not recognized; route them to `VideoResourceDecoder` with [a format of your own](custom-formats.md#adding-a-format-with-iresourceformat). |
| Codecs | The video codecs of the embedded FFmpeg build, such as H.264, HEVC, VP8 and VP9. |
| Source | The asset is first copied into a temporary file (`joid-video-*.mp4`, deleted when the JVM exits), then read from it. |
| Size | `getWidth()` and `getHeight()` are the size of the video. |
| Frame rate | Read from the file, 30 frames per second when the file does not tell. |
| Streaming | A decoding thread named `joid-video-decode` keeps up to 5 frames ahead; two textures alternate on the GPU. |
| Timing | Frames are shown when the [clock bridge](../integration/bridges.md#iclockbridge) reaches their time. |
| Audio | The audio track plays through the [audio bridge](../integration/bridges.md#iaudiobridge-and-iaudiosource). See [Playback, Video and Audio](playback.md#audio). |
| Errors | A file FFmpeg cannot open prints `Failed to decode video: ...` to `System.err`; the resource then has no picture. |

### Transparent videos

A WebM file whose VP8 or VP9 track is flagged with an alpha channel (`alpha_mode` set to 1) is decoded with libvpx, which keeps the alpha channel: the transparent parts of the video are transparent on screen.

## Transparency of every format

Decoders give the fully transparent pixels of an image, an animation frame or an SVG raster the color of the nearest pixel that is not fully transparent, keeping them fully transparent. Linear interpolation then never blends a dark halo into the edges of a transparent image.

## See also

- [Resources](resources.md) — loading, options and lifecycle.
- [Playback, Video and Audio](playback.md) — controlling animations and videos.
- [Custom Formats and Decoders](custom-formats.md) — adding a format or a decoder.
- [ResourcePlayerNode](../nodes/visual/resource-player.md) — the node that plays videos.