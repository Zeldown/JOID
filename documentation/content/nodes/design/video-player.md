# VideoPlayerNode

Full-featured video player. Plays MP4, MOV, WebM, MKV, AVI, GIF, APNG — anything FFmpeg can decode. Audio streamed through OpenAL, synced to video.

## Create

```java
try {
    final Resource video = Resource.of(getClass().getResourceAsStream("/intro.mp4"));

    VideoPlayerNode.create(0, 0, 854, 480)
        .resource(video)
        .loop(true)
        .volume(1F)
        .autoplay(true)
        .attach(parent);
} catch (IOException e) {
    e.printStackTrace();
}
```

> NOTE: `Resource.of(InputStream)` throws `IOException` for video streams — wrap in try/catch or propagate.

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
node.getProgress();             // 0.0 → 1.0
node.getDecoder().getCurrentVideoTime();  // seconds
node.getDecoder().getFrameRate();
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

The `AudioListener` is a `Supplier<Vector3f>` — you provide your own listener position logic (player location, camera position, etc.). If not set, the fade is skipped and volume is flat.

## Example — fullscreen toggle

```java
final VideoPlayerNode player = VideoPlayerNode.create(100, 100, 640, 360)
    .resource(video)
    .loop(true)
    .attach(this);

this.keybind(() -> {
    if (player.getWidth() == 640) {
        player.position(0, 0).size(1920, 1080);
    } else {
        player.position(100, 100).size(640, 360);
    }
}, Keyboard.KEY_F);
```

## Example — progress scrubber

```java
final VideoPlayerNode player = VideoPlayerNode.create(0, 0, 854, 480)
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

`VideoPlayerNode` overrides `detach()` to release its decoder automatically — no leak when the UI closes or the node is removed via `clearChildren()`.

Changing the resource with `.resource(newResource)` releases the previous decoder's grabber, audio player, and thread before switching.

## Supported formats

Detected via magic bytes (header), not file extension:

- **MP4 / MOV** — `ftyp` box
- **WebM / MKV** — EBML header
- **AVI** — `RIFF` + `AVI`
- **GIF** — `GIF87a` / `GIF89a`
- **APNG** — PNG with `acTL` chunk (detected by FFmpeg)

For GIF/APNG, looping is enabled by default (`isLoopByDefault`).

## Best practices

- **Use `release()` before swapping many times.** The decoder holds a thread, temp file, and OpenAL source — cleaning up matters for long-running apps.
- **Set `autoplay(false)` for user-triggered playback.** Otherwise the video starts as soon as the resource decodes.
- **Prefer `.location(x, y, z) + setAudioListener` for world audio.** Setting volume manually each frame is less efficient.
- **Don't keep a reference to a disposed decoder.** Call `getDecoder()` each time — it returns `null` after release.

## See also

- [Decoders](../../resources/decoders.md) — `VideoResourceDecoder` internals.
- [ResourceBuilder](../../resources/resource-builder.md) — video auto-detection from streams.
