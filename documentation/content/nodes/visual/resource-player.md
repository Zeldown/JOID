# ResourcePlayerNode

`ResourcePlayerNode` (`dev.joid.lib.ui.node.impl.design.resource`) plays a video or an animated image (GIF, APNG, animated WebP) with playback controls, looping, volume, positional audio and play, pause, stop, end and progress callbacks. Use [`ResourceNode`](resource.md) for still images.

## Creating a ResourcePlayerNode

```java
final ResourcePlayerNode player = ResourcePlayerNode
    .create(430, 10, 640, 360)
    .resource(Resource.of(MyUI.class.getResourceAsStream("/videos/intro.mp4")))
    .loop(true)
    .attach(this);

this.keybind(() -> {
    if (player.isPlaying()) {
        player.pause();
    } else {
        player.resume();
    }
}, Key.SPACE);
```

![A looping placeholder video that freezes for a moment, then plays on](../../images/player-pause.gif "Space pauses the looping video, a second press resumes it (a short placeholder video stands in for intro.mp4).")

`keybind` and `Key` (`dev.joid.lib.utils.key`) are described in [The UI Class](../../ui/ui-class.md) and [Mouse and Keyboard](../../interactions/mouse-and-keyboard.md).

The resource is any [`Resource`](../../resources/resources.md) whose decoder provides an `IResourcePlayback`: videos and animated images (see [Supported Formats](../../resources/formats.md)). A still image is displayed, but the playback controls do nothing on it.

## Starting the playback

The playback starts the first time the node draws its loaded resource with a non-zero size. At that moment the node:

1. applies its volume and audio position to a video;
2. stops the playback, seeks to `0`, and applies `loop` and `autoplay`;
3. calls `play()` when `autoplay` is `true`.

`resource(...)` with another resource releases the previous video and starts the new resource the same way on its next draw. With `autoplay(false)`, the playback stays stopped until you call `play()`.

### Size and placeholder

- A node created with `create(x, y)` (0×0) takes the size of the resource's frame in pixels, used as UI units, on the first frame where the resource is loaded; it is drawn from the next frame on.
- Like `ResourceNode`, a node with a single `0` dimension derives it from the aspect ratio of the frame: `create(x, y, 640, 0)` gets a height of 360 for a 16:9 video.
- `stretch(StretchType)` fits the frame into the node with `ResourceNode.StretchType` (`STRETCH` by default, `CONTAIN`, `COVER`; see [ResourceNode](resource.md#fitting-with-stretchtype)). The frame is drawn without tint.
- While there is no resource, while it loads, or when it has no size, the node draws a pulsing grey rectangle (`Color.LOADING()`).

## Controlling the playback

| Method | Description |
| --- | --- |
| `play()` | Starts the playback from the beginning when it is stopped or ended, and resumes it where it was when it is paused. |
| `pause()` | Pauses the playback and fires `onPause` right away. |
| `resume()` | Resumes a paused playback. |
| `stop()` | Stops the playback and fires `onStop` right away; the current frame stays displayed. |
| `seek(double seconds)` / `seekTo(double seconds)` | Moves the playback to a time in seconds. `seekTo` is an alias of `seek`. |
| `restart()` | Stops, seeks to `0` and plays, then fires `onPlay` right away, even when the playback was already playing. It is the only control that brings a running or paused playback back to the beginning. |

These methods return the node (`ResourcePlayerNode`) and do nothing when the node has no resource with a playback.

### Reading the state

| Method | Description |
| --- | --- |
| `isPlaying()` | `true` while the playback runs and is not paused. `false` without playback. |
| `isPaused()` | `true` while paused. `false` without playback. |
| `getDuration()` | Duration in seconds, `0` without playback. |
| `getProgress()` | Position as a fraction of the duration, from `0` to `1`; `0` without playback. |
| `getPlayback()` | `IResourcePlayback` (`dev.joid.lib.resource.dto.playback`) of the current resource, `null` without one. |
| `getVideo()` | `VideoResourceDecoder` (`dev.joid.lib.resource.dto.decoder.impl`) of the current resource, `null` unless it is a video. |

The current time in seconds is given by `onProgress`, or read with `getPlayback().getCurrentTime()` once `getPlayback()` is not `null`. The full playback contract (`IResourcePlayback`, video decoding, looping and seeking semantics) is described in [Playback, Video and Audio](../../resources/playback.md).

> NOTE: The playback belongs to the resource's decoder. Resources created from the same source (for example the same URL through `Resource.of`) can share one decoder; two players on such resources play, pause and seek together.

## Loop, autoplay and volume

| Method | Default | Description |
| --- | --- | --- |
| `loop(boolean loop)` | `false` | Loops the playback. Applies to the current playback right away and again when it starts. |
| `autoplay(boolean autoplay)` | `true` | Plays as soon as the playback starts. Read when the playback starts. |
| `volume(float volume)` | `1F` | Volume of a video's audio, `1F` = 100 %. Applies to the current video right away and again when it starts. Animated images have no audio. |

## Positional audio

A video's audio can fade with the distance to a listener, for UIs placed in a 3D world.

| Method | Default | Description |
| --- | --- | --- |
| `location(float x, float y, float z)` | none | Position of the audio source. Without a location, the audio is not attenuated. |
| `referenceDistance(float distance)` | decoder default (`5F`) | Distance up to which the volume is full. |
| `maxDistance(float distance)` | decoder default (`50F`) | Distance from which the audio is silent. |

Between the two distances the volume fades quadratically. The listener position comes from `VideoAudioPlayer.setAudioListener(AudioListener)` (`dev.joid.lib.video`); without a listener, the audio is not attenuated.

```java
VideoAudioPlayer.setAudioListener(() -> new Vector3f(0F, 1.6F, 0F));

ResourcePlayerNode
    .create(0, 0, 640, 360)
    .resource(Resource.of(MyUI.class.getResourceAsStream("/videos/screen.mp4")))
    .location(10F, 2F, -4F)
    .referenceDistance(3F)
    .maxDistance(30F)
    .attach(this);
```

These settings apply to the current video right away and again when the playback starts. They have no effect on animated images.

## Callbacks

```java
final ProgressNode bar = ProgressNode.create(0, 370, 640, 6).color(Color.DARKGRAY, Color.WHITE).attach(this);

ResourcePlayerNode
    .create(0, 0, 640, 360)
    .resource(Resource.of(MyUI.class.getResourceAsStream("/videos/intro.mp4")))
    .onPlay(player -> System.out.println("playing"))
    .onProgress((player, progress, currentTime) -> bar.progress((float) progress))
    .onStop(player -> System.out.println("stopped"))
    .onEnd(player -> System.out.println("finished"))
    .attach(this);
```

![A placeholder video playing above a thin white bar that fills as it plays](../../images/player-progress.gif "onProgress drives the ProgressNode under the video.")

| Method | Lambda | Fired when |
| --- | --- | --- |
| `onPlay(NodeResourcePlayerPlayCallback<T>)` | `(node) -> ...` | The playback was not playing on the previous drawn frame and plays on the current one: when it starts, and after `play()` or `resume()` from a stopped or paused state. `restart()` fires it immediately, inside the call. |
| `onPause(NodeResourcePlayerPauseCallback<T>)` | `(node) -> ...` | `pause()` is called on the node and the resource has a playback. It fires immediately, inside the `pause()` call. |
| `onStop(NodeResourcePlayerStopCallback<T>)` | `(node) -> ...` | The playback stops: `stop()` is called on the node and the resource has a playback (it fires immediately, inside the `stop()` call), or the resource reaches its end, right after `onEnd`. `restart()` does not fire it. |
| `onEnd(NodeResourcePlayerEndCallback<T>)` | `(node) -> ...` | The resource reaches its end while `loop` is `false`. `stop()` does not fire it, and it never fires while looping. |
| `onProgress(NodeResourcePlayerProgressCallback<T>)` | `(node, progress, currentTime) -> ...` | On each frame while playing, when the progress changed. `progress` goes from `0` to `1`, `currentTime` is in seconds. |

- `onPlay`, `onEnd`, `onProgress` and the `onStop` of the end of the resource are detected while the node draws: a node that is not drawn (hidden, or outside a closed UI) does not fire them. A playback stopped through `getPlayback()` instead of the node's `stop()` is seen the same way, as an end.
- The callback interfaces live in `dev.joid.lib.ui.node.impl.design.resource.callback`. Each has an `apply(...)` method for the lambda and `pre(...)`/`post(...)` phases taking an `InternalContext`; the lambda runs in the POST phase. See [Callbacks](../../interactions/callbacks.md).
- You can register several callbacks of the same kind; they run in registration order.
- The callback ids are the constants `ResourcePlayerNode.CALLBACK_PLAY`, `CALLBACK_PAUSE`, `CALLBACK_STOP`, `CALLBACK_END` and `CALLBACK_PROGRESS`, usable with `hasCallback(int)`.

## Releasing the video

The node releases its video decoder (decoding thread, audio source) when it is detached: when its UI closes or is rebuilt, or when its parent's children are cleared with `clearChildren()`. Attached again, the node starts its resource from the beginning on its next draw, as a new node does (playing it when `autoplay` is on). `resource(...)` also releases the previous video before switching. See the node lifecycle in [Node Fundamentals](../node-fundamentals.md).

## Reference

### Factories

| Method | Description |
| --- | --- |
| `ResourcePlayerNode.create(double x, double y)` | Creates a player sized by its resource. |
| `ResourcePlayerNode.create(double x, double y, double width, double height)` | Creates a player with a given box; a `0` dimension follows the resource's aspect ratio. |

### Properties

| Method | Default | Description |
| --- | --- | --- |
| `resource(Resource resource)` | `null` | Media to play. Releases the previous video. |
| `stretch(StretchType stretchType)` | `StretchType.STRETCH` | How the frame fills the box. |
| `autoplay(boolean)` | `true` | Plays when the playback starts. |
| `loop(boolean)` | `false` | Loops the playback. |
| `volume(float)` | `1F` | Video audio volume. |
| `location(float, float, float)` | none | Position of the video's audio source. |
| `referenceDistance(float)` | none | Full-volume distance. |
| `maxDistance(float)` | none | Silent distance. |

These setters return the node itself, typed by the generic return of the fluent API.

### Getters

| Method | Description |
| --- | --- |
| `getResource()` | The current `Resource`, or `null`. |
| `getStretchType()` | The current `StretchType`. |
| `isAutoplay()`, `isLoop()`, `getVolume()` | The configured values. |
| `getLocation()` | The audio position as a `Vector3f`, or `null`. |
| `getReferenceDistance()`, `getMaxDistance()` | The configured distances as `Float`, or `null` when not set on the node. |
| `isResourceStarted()` | `true` once the playback of the current resource has started. |
| `isWasPlaying()`, `getLastProgress()` | Playing state and progress seen on the previous frame, used to fire the callbacks. |
| `isPlaying()`, `isPaused()`, `getDuration()`, `getProgress()`, `getPlayback()`, `getVideo()` | See [Reading the state](#reading-the-state). |

## See also

- [ResourceNode](resource.md)
- [Playback, Video and Audio](../../resources/playback.md)
- [Supported Formats](../../resources/formats.md)
- [Resources](../../resources/resources.md)
- [ProgressNode](progress.md)
- [Callbacks](../../interactions/callbacks.md)