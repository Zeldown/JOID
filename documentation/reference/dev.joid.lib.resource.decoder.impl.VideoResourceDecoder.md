---
summary: Plays a video file with FFmpeg, its frames into a texture and its sound through the audio bridge.
---

[[VideoResourceFormat]] picks it for MP4, Matroska and AVI files.
The first decode waits for the FFmpeg natives ([[VideoNatives]]); a video that is not a plain file, inside a jar, a stream or a URL, is copied into the `video` area of the cache folder first.
Frames are decoded ahead on a thread of their own and shown at the time of the JOID clock; the sound plays when an audio bridge is registered, otherwise the video plays silent with one warning.
Get it from [[Resource#getPlayback]] and cast it to set the sound; each fork of the same file plays on its own.

Learn: [Media](/use/learn/describing-the-ui/media/).

## create(File)

Creates a decoder of the video `file`.

## create(Asset)

Creates a decoder of the video `asset`.

## loop

If `true`, the video starts again at the end.

## autoplay

If `true`, the video starts once its first frame is uploaded.

## volume

The volume of the sound, on 1; `0F` mutes it.

## audioGroup

The group of the audio source, which the audio bridge can use to set the volume of every sound of a group at once; `null` for none.

## referenceDistance

With a `location`, the distance from the listener under which the sound plays at full volume.

## maxDistance

With a `location`, the distance from the listener at which the sound fades out completely; in between, the volume falls as `(1 − t)²`, `t` going from 0 to 1 between the two distances.

## isSettled()

If `true`, the frame shown is the one the current time asks for, so that a capture of the screen is stable.

## isPaused()

If `true`, the video is paused.

## isPlaying()

If `true`, the video is running and not paused.

## getProgress()

Returns the frame shown on 1 of the frame count.

## getCurrentTime()

Returns the time of the frame shown, in seconds.

## getAsset()

Returns the video file.

## getDisplayedFrameIndex()

Returns the index of the frame shown.

## getVideoWidth()

Returns the width of the video, in pixels, `0` before the decode.

## getVideoHeight()

Returns the height of the video, in pixels, `0` before the decode.

## getTotalFrames()

Returns the number of frames of the video.

## getDuration()

Returns the length of the video, in seconds.

## getFrameRate()

Returns the frame rate of the video, 30 when the file does not give one.

## getLocation()

Returns the position of the sound, or `null` for a sound that does not depend on the listener.

## prepare(ResourceData)

Gives the data two placeholder textures until the first frame is uploaded.

## decode(ResourceData)

Waits for the natives, opens the video, reads its size, rate and length and decodes its first frame.
Throws the exception of the natives when they cannot be installed, `UncheckedIOException` when the video cannot be read or copied, and `IllegalStateException` when it cannot be decoded.

## upload(ResourceData)

Uploads the first frame and starts the playback when `autoplay` is `true` or `play()` was called before the end of the decode.

## clear(ResourceData)

Releases the decoder, the sound and the textures.

## fork()

Returns a decoder of the same file that plays on its own.

## play()

Starts the video from the start, or from the time of a `seek` made while stopped, or resumes it after `pause()`.
Called before the decode ends, it starts the video once the first frame is uploaded.

## stop()

Stops the video and its sound; the next `play()` starts from the start.

## pause()

Pauses the video and its sound on the current frame.

## seek(double)

Moves the video to `seconds` from the start; on a stopped video, it pauses there, and `play()` resumes from there.

## location(float,float,float)

Places the sound at `x`, `y`, `z`, so that its volume depends on the distance to the listener given by [[VideoAudioPlayer#audioListenerPosition]].

## release()

Stops the decoding thread and frees the FFmpeg decoder and the sound; the next `play()` opens the file again.
