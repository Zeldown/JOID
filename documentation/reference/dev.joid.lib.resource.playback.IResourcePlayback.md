---
summary: The playback controls of an animated image or a video, to play, pause, seek and loop it.
---

Get them from [[Resource#getPlayback]], or from a resource player node.
Times are in seconds; call the controls on the render thread.

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## play()

Starts the playback from the start, or from the time of a `seek` made while stopped, or resumes it after `pause()`.

## stop()

Stops the playback; the next `play()` starts from the start, unless a `seek` comes first.

## pause()

Pauses the playback at the current time.

## seek(double)

Moves the playback to `seconds` from the start; while stopped, the playback pauses on that frame, and the next `play()` starts there.

## loop(boolean)

If `true`, the playback starts again at the end.

## autoplay(boolean)

If `true`, the playback starts by itself once the resource is loaded.

## isLoop()

If `true`, the playback loops.

## isPaused()

If `true`, the playback is paused.

## isPlaying()

If `true`, the playback is running.

## isAutoplay()

If `true`, the playback starts by itself once loaded.

## getDuration()

Returns the length of the media, in seconds.

## getProgress()

Returns the current time on 1 of the duration.

## getCurrentTime()

Returns the current time, in seconds from the start.

## restart()

Stops, seeks to the start and plays.
