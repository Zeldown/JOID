---
title: "Images and media"
description: "Show images, animated images and videos from files, the web or your own textures."
genre: learn
sidebar:
  order: 13
---

Icons, photos, animated images, video trailers: everything JOID shows from a file is a resource.
This page shows how to load one, draw it, play it when it moves, and which formats JOID reads.

**You will learn**
- How to show an image from the jar, the disk or the web
- How to fit, tint and crop an image
- How to know when an image is loaded, and share settings between images
- How to play animated images and videos, with their sound, or a texture of your application
- Which formats JOID reads

## Show an image

A [[Resource]] is something JOID can draw, made by [[Resource#of]], and a [[ResourceNode]] draws it:

```java file="com/example/describing/media/GalleryUI.java" region="image"
```

```java file="com/example/describing/media/GalleryUI.java" region="natural"
```

<figure>

![On a dark card, two rows of light grey placeholder images framed in grey with a cross. First row: the image at its natural size, a smaller copy with the same proportions, and a copy tinted darker. Second row: the image stretched into a square, contained in a square between two dark grey bands, and covering a square, then the top-left quarter of the image enlarged.](/captures/describing-media-gallery.webp)

<figcaption>
One image, many ways to draw it: natural size, proportional size, tint, the three fits and a crop.
</figcaption>
</figure>

A `ResourceNode` sizes itself from the image:

- Without a size, or with `0` for both, it takes the size of the image, one design pixel per image pixel.
- With only a width (the height at `0`), such as `create(400, 40, 160, 0)`, it computes the height that keeps the proportions of the image; with only a height (the width at `0`), the width.
- With both, it fills that box, as [Fit, tint and crop](#fit-tint-and-crop) shows.

An image has a fixed number of pixels, while the canvas scales with the window ([The design canvas](/use/learn/describing-the-ui/design-canvas/)).
Give a source as large as the largest window shows it: JOID scales an image down smoothly, but scaling it up blurs it.

## Load from the jar, the disk or the web

`Resource.of(...)` takes the usual sources:

```java file="com/example/describing/media/MediaSources.java" region="sources"
```

| Source | What JOID reads |
|---|---|
| `String` without a scheme | A file of the classpath, such as `images/placeholder.png` in your jar, or else a file of the disk at that path. |
| `String` with a scheme | A URL (`https:`, `file:`, `jar:`). JOID gives up after 10 s without a connection and 30 s without data. |
| `File` | A file of the disk. |
| `InputStream` | The content of the stream, read once. |
| `BufferedImage` | An image that your code builds. |

Two resources of the same path or URL decode it once and share the result.
A texture of your own renderer is a source too: [Show a texture of your application](#show-a-texture-of-your-application).
To read from a source of your own, such as an archive, write an asset locator ([Custom formats](/use/learn/going-further/custom-formats/)).

## Fit, tint and crop

[[ResourceNode#stretch]] decides how an image fills a box of other proportions, with a value of [[ResourceNode.StretchType]]:

```java file="com/example/describing/media/GalleryUI.java" region="stretch"
```

| Value | Result |
|---|---|
| `STRETCH` | The default: the image fills the box and loses its proportions. |
| `CONTAIN` | The whole image, as large as the box allows, centered; the rest of the box stays empty. |
| `COVER` | The image fills the box and keeps its proportions; what goes beyond the box is cut. |

<figure>

![Three dark grey boxes labelled STRETCH, CONTAIN and COVER, each holding the same light grey placeholder image, change shape in a loop, from wide to square, to tall, then back to square and wide: the first image deforms with its box, the second stays whole with empty bands beside it, the third fills its box and loses its edges](/captures/describing-media-stretch.webp)

<figcaption>
Only `STRETCH` deforms the image; `CONTAIN` and `COVER` keep its proportions whatever the shape of the box.
</figcaption>
</figure>

[[ResourceNode#color]] tints the image: JOID multiplies each pixel by the [[Color]], so white, the default, keeps the image as it is.
[[ResourceNode#hoveredColor]] gives the tint under the mouse, and [[ResourceNode#hoveredResource]] another image, with a fade ([Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/)):

```java file="com/example/describing/media/GalleryUI.java" region="tint"
```

[[Resource#textureCoords]] crops the image to a region, in image pixels, for a sprite sheet or an atlas:

```java file="com/example/describing/media/GalleryUI.java" region="crop"
```

The settings of a `Resource` belong to that `Resource` object: `corner` crops, while `image`, made from the same file, does not.
[[Resource#nearest]] keeps the hard edges of pixel art when JOID scales the image, and [[Resource#linear]], the default, smooths them.
An image drawn smaller than its size gets mipmaps, smaller copies that keep it smooth; [[Resource#mipmap]] forces them on or off.
[[ResourceNode#linear]] chooses the sampling for one node only, whatever the resource says.

## Know when an image is loaded

A resource loads the first time a node draws it, in the background.
Until then, the node draws a placeholder in its place ([Rebuilding the tree](/use/learn/state/rebuilding/) explains this skeleton).
[[Resource#onLoad]] and [[Resource#onError]] tell your code how the load ends:

```java file="com/example/describing/media/LoadingUI.java" region="events"
```

Without a file at `photos/beach.png` in the working directory, the status reads `Missing photo`: `onError` ran.
Both run on the render thread; added after the resource reached that state, they run at once.
[[Resource#isLoaded]] and [[Resource#isFailed]] read the state at any time.
A resource that cannot be read takes 64 × 64 design pixels in a node without a size; it draws a magenta and black checkerboard with the dev jar, and nothing with the prod jar.

<figure>

![A dark card with three images in a row: on the left, a magenta and black checkerboard above the status Missing photo, since photos/beach.png does not exist; in the middle and on the right, the grey placeholder image, drawn by the blocking and the builder examples further down](/captures/describing-media-loading.webp)

<figcaption>
With the dev jar, the photo that cannot be read shows as a checkerboard, and `onError` wrote the status.
</figcaption>
</figure>

[[Resource#blocking]] loads the image on the render thread, the first time it is drawn, so that it is on screen from its first frame:

```java file="com/example/describing/media/LoadingUI.java" region="blocking"
```

:::caution
That first frame waits for the whole decoding.
Keep `blocking()` for small images that must never be missing, and preload a large one.
:::

[[Resource#generate]] starts the decoding in the background without waiting for a node to draw the resource, for example in the constructor of the UI, before it opens:

```java file="com/example/describing/media/PreloadUI.java" region="preload"
```

`onLoad` tells when that decoding ends, and the first node that draws the resource then shows it at once.

## Give a set of images the same settings

`Resource.of(...)` starts every resource with linear sampling.
Images that share other settings, such as the sprites of a pixel-art game, come from one [[ResourceBuilder]]:

```java file="com/example/describing/media/LoadingUI.java" region="builder"
```

[[ResourceBuilder#create]] starts with the settings of `Resource.of(...)`; `nearest()`, `linear()`, `mipmap(...)` and `textureCoords(...)` each return a builder with that setting.
Each resource starts with a copy of the settings of its builder, and its own setters change that resource only; [[Resource#reset]] gives it back the settings of its builder.
Resources of every builder share their decoded content, kept in memory 5 minutes after its last use.
[[ResourceBuilder#cache]] gives a builder a cache of its own, such as a [[MemoryResourceCache]], or `null` to make each resource decode its own copy.

## Play animated images and videos

A [[ResourcePlayerNode]] plays a resource that moves: an animated image, or a video with its sound.

```java file="com/example/describing/media/AnimationUI.java" region="animation"
```

<figure>

![On a dark card, a light grey animated image where a grey square slides from left to right, then starts again from the left](/captures/describing-media-animation.webp)

<figcaption>
The player starts the GIF on its own and loops it.
</figcaption>
</figure>

A player sizes itself and fits its image like a `ResourceNode`, with `stretch(...)`.
It starts on its own ([[ResourcePlayerNode#autoplay]]), plays once ([[ResourcePlayerNode#loop]]) and at full volume ([[ResourcePlayerNode#volume]], on 1).
Here a video waits for the user, at half volume, and closes its UI when it ends:

```java file="com/example/describing/media/TrailerUI.java" region="video"
```

```java file="com/example/describing/media/TrailerUI.java" region="controls"
```

- [[ResourcePlayerNode#play]], [[ResourcePlayerNode#pause]], [[ResourcePlayerNode#toggle]], [[ResourcePlayerNode#stop]] and [[ResourcePlayerNode#restart]] control the playback, and [[ResourcePlayerNode#seek]] jumps to a time in seconds from the start, paused if the player was stopped.
- Called before the player first draws, these calls wait for it to start.
  With `autoplay(false)`, the player waits paused on its first frame: `isPaused()` is `true`.
- [[ResourcePlayerNode#isPlaying]], [[ResourcePlayerNode#isPaused]], [[ResourcePlayerNode#getDuration]] (seconds) and [[ResourcePlayerNode#getProgress]] (on 1) read the playback.
- [[ResourcePlayerNode#onPlay]], [[ResourcePlayerNode#onPause]], [[ResourcePlayerNode#onStop]] and [[ResourcePlayerNode#onEnd]] follow it; `onEnd` never fires on a loop.
  [[ResourcePlayerNode#onProgress]] receives a [[NodeProgressEvent]], with the progress on 1 and the current time in seconds.

The guide [Show a video](/use/guides/show-a-video/#what-you-will-build) shows a video playing under these controls, with a progress bar and a pause button.
Each player has its own playback, created when it is attached: two players of one resource play independently.
A player that leaves the tree stops and releases its playback; attached again, it starts from the beginning ([Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/)).

An animated image drawn by a `ResourceNode` plays too, as many times as the file asks, and every node that draws that resource shows the same frame.
[[Resource#getPlayback]] gives its [[IResourcePlayback]], with the same controls, or `null` for a still image.

## Control the sound of a video

The sound of a video goes through the audio bridge of the backend, at the volume of its player.
[[ResourcePlayerNode#audioGroup]] tags it, so that one setting changes every sound of a group.
With JOID's backends, [[AlAudioBridge#groupVolume]] sets the volume of a group, on 1:

```java fragment
ResourcePlayerNode.create(0, 0, 640, 360).resource(intro).audioGroup("music").attach(this);
BridgeHandler.AUDIO.getBridge(AlAudioBridge.class).groupVolume("music", 0.4F);
```

For a video placed in a 3D scene of your application, [[ResourcePlayerNode#location]] places the sound at a [[Point]] of that scene.
It plays at full volume up to [[ResourcePlayerNode#referenceDistance]] from the listener (5 by default), then fades out until [[ResourcePlayerNode#maxDistance]] (50 by default).
The listener stands at the origin until [[VideoAudioPlayer#audioListenerPosition]] gives it an [[AudioListenerPosition]], which JOID asks for the position of the listener every frame.

## Play videos on every machine

JOID decodes videos with FFmpeg, whose native libraries depend on the system.
When they are not on the classpath, the first video downloads those of the running system from Maven Central, checks their SHA-256 and keeps them in the [cache folder](/use/learn/running/files/) for 30 days.
Videos play on Windows (x64), macOS (Intel and Apple silicon) and Linux (x64).

[[VideoNatives#install]] starts that download early and returns a `CompletableFuture`, for example behind a loading screen.
[[VideoNatives#repository]] takes the URL of another Maven repository, such as a mirror of your company.
When the natives cannot be found, downloaded or checked, the video fails with a [[VideoNativesException]] that says why, and `onError` receives it.
[Play videos without network access](/use/get-started/installation/#play-videos-without-network-access) shows how to put the natives on the classpath instead.

## Show a texture of your application

An application that renders with its own OpenGL or Vulkan code can show the result in a node.
Wrap the texture in a borrowed texture of the backend, then give it to `Resource.of(...)`:

```java file="com/example/describing/media/MinimapPanel.java" region="texture"
```

| Backend | Borrowed texture | It takes |
|---|---|---|
| LWJGL 2, LWJGL 3 | [[GlBorrowedTexture]] | The name of an OpenGL texture, or an `IntSupplier` that returns it |
| Vulkan | [[VulkanBorrowedTexture]] | A [[VulkanImage]], or a `Supplier` that returns one |

JOID draws the texture where it is: it never copies it, never writes into it and never deletes it.
It calls a supplier each time it draws, so the node follows your renderer when it replaces its texture, after a resize for example.

The size of the resource is the size of the texture, read at each draw.
A texture that does not exist when the resource first loads makes the resource fail.
A Vulkan image must be on the device of the backend, in the `SHADER_READ_ONLY_OPTIMAL` layout whenever JOID draws it.

## Supported formats

JOID recognizes a file by its first bytes, never by its name:

| Kind | Formats | Node |
|---|---|---|
| Still images | PNG, JPEG, BMP, WebP | `ResourceNode` |
| Vector images | SVG, drawn again at the size it is shown | `ResourceNode` |
| Animated images | GIF, animated PNG (APNG), animated WebP | `ResourcePlayerNode` |
| Videos, with their sound | MP4 and MOV, WebM and MKV, AVI | `ResourcePlayerNode` |

Sound plays only as the track of a video: an audio file alone is not a resource.
HEIF and AVIF images are recognized but not decoded: the resource fails with an error that says so.
To read another format, write your own decoder ([Custom formats](/use/learn/going-further/custom-formats/)).
To draw a resource in your own drawing code, see [Draw an image](/use/learn/going-further/drawing-resources-and-transforms/#draw-an-image).

## Recap

- A **`Resource`** comes from **`Resource.of(...)`**: a classpath path, a file, a URL, a stream, a `BufferedImage` or a **borrowed texture**.
- A **`ResourceNode`** draws it at its **natural size**, or keeps its proportions when you give one side.
- **`stretch`** fits it (`STRETCH`, `CONTAIN`, `COVER`), **`color`** tints it, **`textureCoords`** crops it.
- A resource **loads when first drawn**, in the background unless **`blocking()`**; **`onLoad`** and **`onError`** run on the render thread.
- A **`ResourceBuilder`** gives its settings to every resource it creates.
- A **`ResourcePlayerNode`** plays animated images and videos with **`play`**, **`pause`**, **`toggle`**, **`seek`**, **`loop`** and **`volume`**, and has its own playback.
- Videos need the **FFmpeg natives**, downloaded on the first video when they are not on the classpath.
- JOID reads formats **by their content**; the table of supported formats is the full list.

## Try it

**A logo that lights up.**
Show the placeholder image in grey, turning to its own colors under the mouse.

<details>
<summary>Solution</summary>

`color` tints the image and `hoveredColor` gives the tint under the mouse; white keeps the image as it is.

```java fragment
ResourceNode.create(100, 100).resource(logo).color(GalleryUI.MUTED).hoveredColor(Color.WHITE).attach(this);
```

</details>

**A looping video behind the menu.**
Play a silent video in a loop behind a whole menu, filling the 1920 × 1080 canvas without distortion.

<details>
<summary>Solution</summary>

`COVER` keeps the proportions and cuts what goes beyond the canvas; a volume of `0F` silences the video.

```java file="com/example/describing/media/MenuBackgroundUI.java" region="background"
```

Attach it first, so that the nodes of the menu draw above it.

</details>
