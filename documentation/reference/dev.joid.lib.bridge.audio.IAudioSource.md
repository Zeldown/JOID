---
summary: One stream of 16-bit samples that the video player fills ahead of the clock and plays.
---

The video player calls every method on the render thread.

Learn: [Audio bridge](/build/bridges/audio/#required).

## delete()

Stops the source and releases what the audio API allocated for it.

## play()

Starts the source, or resumes it after `pause()`.

## stop()

Stops the source and drops the samples that it has not played.

## pause()

Pauses the source and keeps its queued samples for `play()`.

## gain(float)

Sets the volume, `1F` for 100 %; `0F` mutes the source while its samples keep flowing.

## group(Object)

Receives the audio group that the application gave the video, or `null` for none; the source maps it to a volume of the host.

## write(short[])

Appends interleaved samples to the queue of the source.
A playing source whose queue ran dry plays again.

## isPlaying()

If `true`, the source is playing.

## getBufferedSamples()

Returns the number of interleaved samples written and not played yet, counted on the channels that the source queues (stereo after a downmix).
