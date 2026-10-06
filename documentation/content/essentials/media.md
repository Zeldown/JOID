# Images and Media

JOID displays images, SVG, animated images and videos with the same two pieces: a `Resource`, which points at the content and decodes it, and a node that draws it, `ResourceNode` for pictures and `ResourcePlayerNode` for videos and controllable animations. This page shows how to load content, fit it into a node, and play media.

## Loading content with Resource.of

`Resource.of(...)` (`dev.joid.lib.resource`) creates a resource from a file, a URL or a stream:

```java
final Resource banner = Resource.of(new File("images/banner.png"));
final Resource avatar = Resource.of("https://placehold.co/400x400.png");
final Resource icon = Resource.of(SettingsUI.class.getResourceAsStream("/assets/icons/play.svg"));
```

- `Resource.of` returns at once. The pixels are decoded in the background the first time the resource is drawn, and the node shows a pulsing placeholder until they are ready.
- The format (PNG, JPEG, SVG, GIF, APNG, WebP, MP4, WebM...) is detected from the content itself, never from the file name.
- Resources loaded from the same source share their decoded data, so loading the same file twice costs nothing.

> WARNING: A `String` is always a URL. For a file on disk, pass `new File(path)`; for a file inside your jar, pass `getResourceAsStream(path)`.

## Displaying an image with ResourceNode

`ResourceNode` (`dev.joid.lib.ui.node.impl.design.resource`) draws a resource:

```java
ResourceNode.create(10, 10).resource(banner).attach(this);
ResourceNode.create(10, 200, 100, 100).resource(avatar).attach(this);
ResourceNode.create(120, 200, 0, 48).resource(icon).attach(this);
```

![A wide placeholder banner above a square placeholder and a small one](../images/ess-media-sizes.png "The three sizing rules, with placeholder images standing in for banner.png and play.svg (a 600 × 150 PNG and a 96 × 96 SVG).")

| Size given | Result |
| --- | --- |
| None, `create(x, y)` | The node takes the image's size in pixels, as canvas units. |
| One dimension, the other `0` | The missing one follows the image's proportions. |
| Both | The image is fitted into the box (see below). |

## Fitting with StretchType

When the box and the image have different proportions, `stretch(...)` chooses how the image fills the box. `StretchType` is nested in `ResourceNode`:

```java
ResourceNode
.create(10, 320, 300, 200)
.resource(Resource.of("https://placehold.co/400x800.png"))
.stretch(StretchType.COVER)
.attach(this);
```

![A tall placeholder image filling a wide box, cropped at the top and bottom](../images/ess-media-cover.png "COVER fills the 300 × 200 box with the 400 × 800 image and crops what does not fit.")

| `StretchType` | Behavior |
| --- | --- |
| `STRETCH` (default) | Fills the box exactly; the image is distorted when the proportions differ. |
| `CONTAIN` | The whole image fits inside the box, centered; the rest of the box stays empty. |
| `COVER` | The image covers the whole box, centered; the parts outside the box are cropped. |

`COVER` and a [`CircleNodeEffect`](styling.md#effects) make a round avatar from any picture.

## Tint and hover

`color(...)` tints the image: the default `Color.WHITE` keeps the original pixels, and a lower alpha fades it. A second resource fades in while the mouse is over the node:

```java
ResourceNode.create(400, 10, 64, 64).resource(icon).color(Color.WHITE.copyAlpha(0.5F)).attach(this);

ResourceNode
.create(480, 10, 48, 48)
.resource(Resource.of("https://placehold.co/48x48.png"), Resource.of("https://placehold.co/48x48/orange/white.png"))
.attach(this);
```

![A half-transparent icon next to a gray placeholder that turns orange under the cursor](../images/ess-media-hover.gif "Left, the icon tinted with alpha 0.5F; right, the hovered resource fading in (an SVG placeholder stands in for play.svg, 2× scale).")

## Options of a resource

Settings that belong to the content itself chain right after `Resource.of`:

```java
final Resource tile = Resource.of(new File("sprites/sheet.png")).nearest().textureCoords(0, 0, 32, 32);
ResourceNode.create(10, 600, 64, 64).resource(tile).attach(this);
```

![The first 32 x 32 sprite of a sheet, a violet square frame, drawn at 64 x 64 with sharp pixels](../images/ess-media-sprite.png "Only the top-left 32 × 32 region of a four-sprite sheet is drawn, scaled with nearest filtering (2× scale).")

- `nearest()` keeps hard pixel edges when the image is scaled, for pixel art; `linear()` (the default) smooths it.
- `textureCoords(u, v, width, height)` draws only a region of the image, in source pixels: one sprite of a sheet.

## Animated images

GIF, APNG and animated WebP play by themselves in a `ResourceNode`, in a loop or not as their file says. Nothing else is needed:

```java
ResourceNode.create(600, 10, 64, 64).resource(Resource.of(new File("images/spinner.gif"))).attach(this);
```

![An animated placeholder image playing by itself](../images/ess-media-gif.gif "An animated image plays in a plain ResourceNode (an APNG placeholder stands in for spinner.gif, 2× scale).")

## Videos with ResourcePlayerNode

`ResourcePlayerNode` plays videos (MP4, WebM, Matroska, AVI, with their audio) and animated images, and gives you the controls:

```java
final ResourcePlayerNode player = ResourcePlayerNode
.create(430, 10, 640, 360)
.resource(Resource.of(new File("videos/intro.mp4")))
.loop(true)
.volume(0.5F)
.attach(this);

this.keybind(() -> {
    if (player.isPlaying()) {
        player.pause();
    } else {
        player.resume();
    }
}, Key.SPACE);
```

| Method | Description |
| --- | --- |
| `play()`, `pause()`, `resume()`, `stop()`, `restart()` | Playback controls. |
| `seek(seconds)` | Moves to a time. |
| `loop(boolean)` | Loops the playback. Default `false`. |
| `autoplay(boolean)` | Starts playing as soon as the content is ready. Default `true`. |
| `volume(float)` | Audio volume, `1F` = 100 %. Default `1F`. |
| `isPlaying()`, `getProgress()`, `getDuration()` | Playback state; the progress is a fraction from `0` to `1`. |

Callbacks tell you what happens: `onPlay`, `onPause`, `onStop`, `onEnd` and `onProgress((node, progress, currentTime) -> ...)`. A progress bar under the video:

```java
RectNode.create(430, 375, 640, 6).color(Color.DARKGRAY).attach(this);
final RectNode bar = RectNode.create(430, 375, 0, 6).color(Color.WHITE).attach(this);

player.onProgress((node, progress, currentTime) -> bar.width(640 * progress));
```

![A looping placeholder video with a white bar under it that fills as it plays](../images/ess-media-player.gif "The bar follows onProgress (a short WebM placeholder stands in for intro.mp4, 0.5× scale).")

> NOTE: Resources from the same source share their playback: two players showing the same file play, pause and seek together.

## Going further

- [Resources](../resources/resources.md): every input, options, builders, caching, load callbacks, releasing.
- [Supported Formats](../resources/formats.md): what each format supports and how it is detected.
- [Playback, Video and Audio](../resources/playback.md): playback control from code, video audio, positional audio.
- [ResourceNode](../nodes/visual/resource.md) and [ResourcePlayerNode](../nodes/visual/resource-player.md): the nodes in detail.
- [Assets](../resources/assets.md) and [Custom Formats and Decoders](../resources/custom-formats.md): loading from your own sources and formats.

Next: [Animation](animation.md).