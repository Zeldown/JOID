# ResourcePlayerNode

`ResourcePlayerNode` plays a video or an animated image with playback controls, looping, volume and callbacks. Use [ResourceNode](resource.md) for still images.

```java
final ResourcePlayerNode player = ResourcePlayerNode
		.create(430, 10, 640, 360)
		.resource(Resource.of(new File("videos/intro.mp4")))
		.loop(true)
		.attach(this);

super.keybind(() -> {
	if (player.isPlaying()) {
		player.pause();
	} else {
		player.resume();
	}
}, Key.SPACE);
```

![A looping placeholder video that freezes for a moment, then plays on](../../images/player-pause.gif "Space pauses the looping video, a second press resumes it.")

The playback starts the first time the node draws its loaded resource, and plays at once while `autoplay` is `true` (the default). Sizing, the loading placeholder and `stretch(StretchType)` work as on a [ResourceNode](resource.md): `create(x, y, 640, 0)` gets a height of 360 for a 16:9 video.

## Controlling the playback

| Method | Description |
|---|---|
| `play()` | Starts from the beginning when stopped or ended; resumes when paused. |
| `pause()`, `resume()` | Pauses and resumes. |
| `stop()` | Stops; the current frame stays displayed. |
| `seek(double seconds)` | Moves to a time in seconds. |
| `restart()` | Goes back to the beginning and plays, even while playing or paused. |
| `isPlaying()`, `isPaused()` | Playback state; `false` without playback. |
| `getDuration()`, `getProgress()` | Duration in seconds, position from `0` to `1`. |

With `autoplay(false)`, the playback stays stopped until you call `play()`. `resource(...)` with another resource releases the previous video and starts the new one.

## Events with onProgress and onEnd

The player fires callbacks while it draws. Here `onProgress` drives a [ProgressNode](progress.md) under the video:

```java
private final BooleanSignal finished = BooleanSignal.of(false);

@Override
public void init() {
	final ProgressNode bar = ProgressNode.create(0, 370, 640, 6).background(Color.DARKGRAY).foreground(Color.WHITE).attach(this);

	ResourcePlayerNode
	.create(0, 0, 640, 360)
	.resource(Resource.of(new File("videos/intro.mp4")))
	.onProgress((player, progress, currentTime) -> bar.progress((float) progress))
	.onEnd(player -> this.finished.set(true))
	.attach(this);
}
```

![A placeholder video playing above a thin white bar that fills as it plays](../../images/player-progress.gif "onProgress drives the ProgressNode under the video.")

| Callback | Lambda | Fired when |
|---|---|---|
| `onPlay` | `player -> ...` | The playback starts or resumes, and on `restart()`. |
| `onPause` | `player -> ...` | `pause()` pauses a playback. |
| `onStop` | `player -> ...` | `stop()` is called, or the resource reaches its end (after `onEnd`). |
| `onEnd` | `player -> ...` | The resource reaches its end while `loop` is `false`. |
| `onProgress` | `(player, progress, currentTime) -> ...` | Each drawn frame where the position changed; `progress` from `0` to `1`, `currentTime` in seconds. |

## Positional audio

For UIs placed in a 3D world, the audio fades from `referenceDistance` to `maxDistance` around the listener of `VideoAudioPlayer.setAudioListenerPosition(...)`.

```java
VideoAudioPlayer.setAudioListenerPosition(() -> new Vector3f(0F, 1.6F, 0F));

ResourcePlayerNode
.create(0, 0, 640, 360)
.resource(Resource.of(new File("videos/screen.mp4")))
.location(new Vector3f(10F, 2F, -4F))
.referenceDistance(3F)
.maxDistance(30F)
.attach(this);
```

## Reference

Every setter has a value overload and a `Supplier` overload.

| Method | Default | Description |
|---|---|---|
| `create(x, y)`, `create(x, y, width, height)` | | Player sized by its resource, or with a box (`0` follows the aspect ratio). |
| `resource(Resource)` | `null` | Media to play. |
| `stretch(StretchType)` | `STRETCH` | `STRETCH`, `CONTAIN` or `COVER`. |
| `autoplay(boolean)` | `true` | Plays as soon as the playback starts. |
| `loop(boolean)` | `false` | Loops the playback. |
| `volume(float)` | `1F` | Volume of the video's audio (`1F` = 100 %). |
| `audioGroup(Object)` | `null` | Audio group of the engine, such as a sound category of a game. |
| `location(Vector3f)` | none | Position of the audio source; none means no attenuation. |
| `referenceDistance(float)`, `maxDistance(float)` | `5F`, `50F` | Full-volume and silent distances. |

## Good to know

- Resources from the same source (the same URL through `Resource.of`) can share a decoder: two players on them play, pause and seek together.
- The node releases its video when it is detached (its UI closes or is rebuilt); attached again, it starts over.
- Callbacks other than `onPause` and a direct `stop()` fire while the node draws: a hidden player does not fire them.

## See also

- Next: [ModelNode](model.md)
- [ResourceNode](resource.md)
- [Images and Media](../../concepts/media.md)
- [Input](../../concepts/input.md) for `keybind`