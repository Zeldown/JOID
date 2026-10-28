---
summary: Plays an animated image, GIF, APNG, animated WebP or a format of yours, by uploading the frame of the current time.
---

The formats pick it with the [[IResourceAnimationReader]] of the file; give it your own reader for another animated format.
The frames are decoded once, in memory, and shared by every fork of the same file; each fork has its own playback.
The time follows the JOID clock, so the animation keeps its pace whatever the frame rate.

Learn: [Images and media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/).

## create(Asset,IResourceAnimationReader)

Creates a decoder that reads `asset` with `reader`.

## loop

If `true`, the animation plays forever; if `false`, once.
Without a call, the loop count stored in the file applies.

Default: the loop count stored in the file

## autoplay

If `true`, the animation starts once it is uploaded.

## isPlaying()

If `true`, the animation is running and not paused.

## getDuration()

Returns the length of one play, in seconds, `0D` before the decode.

## getProgress()

Returns the time within the current play, on 1 of the duration.

## getCurrentTime()

Returns the time within the current play, in seconds.

## getAsset()

Returns the animated file.

## isPaused()

If `true`, the animation is paused.

## prepare(ResourceData)

Gives the data a transparent placeholder texture until the first frame is uploaded.

## decode(ResourceData)

Reads every frame, once for the file and its forks, and sets the size and the first frame of the data.
Throws `UncheckedIOException` when the file cannot be read.

## upload(ResourceData)

Uploads the first frame and starts the playback when `autoplay` is `true`.

## clear(ResourceData)

Stops the playback and drops the frames.

## fork()

Returns a decoder that shares the frames of this file and plays on its own.

## play()

Starts the animation, or resumes it after `pause()`; after `stop()`, it starts from the start, or from the time of a `seek` made while stopped.

## stop()

Stops the animation on its current frame; the next `play()` starts from the start unless a `seek` comes first.

## pause()

Pauses the animation on its current frame.

## seek(double)

Moves to `seconds` from the start, clamped to the length of one play; while stopped, the next `play()` starts there.
