# ResourcePlayerNode

Full-featured video player. Plays MP4, MOV, WebM, MKV, AVI through FFmpeg, and GIF and APNG animations through the same playback controls. Audio streamed through the audio bridge, synced to video.

## Create

```java
final Resource video = Resource.of(getClass().getResourceAsStream("/intro.mp4"));

ResourcePlayerNode.create(0, 0, 854, 480)
    .resource(video)
    .loop(true)
    .volume(1F)
    .autoplay(true)
    .attach(parent);
```

> NOTE: nothing is read at this point. The stream becomes an [asset](../../resources/assets.md), and the copy FFmpeg needs is written on the resource worker when the decoder runs — an unreadable source surfaces there, not here.

## Playback control

```java
node.play();
node.pause();
node.resume();
node.stop();
node.seek(double seconds);
node.seekTo(double seconds);   // alias
node.restart();                 // stop + seek(0) + play
```

Query state:

```java
node.isPlaying();
node.isPaused();
node.getDuration();             // seconds
node.getProgress();             // 0.0 → 1.0, the start of the frame shown: just under 1.0 at the end
node.getPlayback();             // Optional<IResourcePlayback>, for videos and animations
node.getVideo();                // Optional<VideoResourceDecoder>, for videos only
node.getVideo().map(VideoResourceDecoder::getFrameRate).orElse(0D);
```

## Configuration

```java
node.loop(boolean);
node.volume(float);              // 0.0 → 1.0
node.autoplay(boolean);          // start immediately on resource load
node.stretch(StretchType);       // STRETCH (default) or CONTAIN
node.resource(Resource);         // change video — releases previous decoder
```

## Callbacks

```java
node.onPlay(player -> { /* started */ });
node.onPause(player -> { /* paused */ });
node.onEnd(player -> { /* playback finished (only when loop=false) */ });
node.onProgress((player, progress, currentTime) -> {
    // Fires on each frame advance
});
```

## 3D spatial audio

Video audio can fade with listener distance. Useful for world-UIs:

```java
node.location(float x, float y, float z);          // source position
node.referenceDistance(float);                      // full volume up to this distance
node.maxDistance(float);                            // muted beyond this

// Listener position is set globally:
VideoAudioPlayer.setAudioListener(() -> new Vector3f(listenerX, listenerY, listenerZ));
```

The `AudioListener` is a functional interface whose `getListenerPosition()` returns a `Vector3f` — you provide your own listener position logic (player location, camera position, etc.). If not set, the fade is skipped and volume is flat.

## Example — fullscreen toggle

```java
final ResourcePlayerNode player = ResourcePlayerNode.create(100, 100, 640, 360)
    .resource(video)
    .loop(true)
    .attach(this);

this.keybind(() -> {
    if (player.getWidth() == 640) {
        player.position(0, 0).size(1920, 1080);
    } else {
        player.position(100, 100).size(640, 360);
    }
}, Key.F);
```

## Example — progress scrubber

```java
final ResourcePlayerNode player = ResourcePlayerNode.create(0, 0, 854, 480)
    .resource(video)
    .attach(this);

ProgressNode.create(0, 490, 854, 8)
    .color(Color.decode("#374151"), Color.decode("#3b82f6"))
    .progress(() -> (float) player.getProgress())
    .onClick((bar, mx, my, ct) -> {
        final double t = (mx - bar.getAbsoluteX()) / bar.getWidth() * player.getDuration();
        player.seekTo(t);
    })
    .effect(RoundedNodeEffect.create(4F))
    .attach(this);
```

## Lifecycle & cleanup

`ResourcePlayerNode` overrides `detach()` to release its decoder automatically — no leak when the UI closes or the node is removed via `clearChildren()`.

Changing the resource with `.resource(newResource)` releases the previous decoder's grabber, audio player, and thread before switching.

## Supported formats

Videos — MP4, MOV, WebM, MKV, AVI — play through FFmpeg, with their audio. GIF, APNG and animated WebP play through the same controls and callbacks, without audio. A VP8 or VP9 WebM with an alpha channel keeps its transparency. See [Formats](../../resources/formats.md).

## Best practices

- **Swap through `.resource(newResource)`.** The decoder holds a thread, temp file, and audio source: `.resource(...)` releases the previous one, and so does removing the node.
- **Set `autoplay(false)` for user-triggered playback.** Otherwise the video starts as soon as the resource decodes.
- **Prefer `.location(x, y, z) + setAudioListener` for world audio.** Setting volume manually each frame is less efficient.
- **Don't keep a reference to a disposed decoder.** Call `getPlayback()` or `getVideo()` each time — they are empty once the resource is gone.

## See also

- [Decoders](../../resources/decoders.md) — `VideoResourceDecoder` internals.
- [ResourceBuilder](../../resources/resource-builder.md) — video auto-detection from streams.
