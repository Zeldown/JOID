---
summary: A source of OpenAL that streams the samples of a video through a queue of buffers.
---

Every call does nothing while no OpenAL context is current; when the context changed, the source creates its OpenAL objects again.

Learn: [Streaming sources](/build/base-modules/openal/#streaming-sources).

## AlAudioSource(AlAudioBridge,int,int)

Creates a source of the bridge for that sample rate and channel count; [[AlAudioBridge#createSource]] calls it.

## delete()

Stops and deletes the OpenAL source and its buffers when their context is still current, then forgets them.

## play()

Plays the source.

## stop()

Stops the source and drops its queued buffers, which it keeps for the next writes.

## pause()

Pauses the source.

## gain(float)

Sets the gain of the source, `1F` for 100 %, before the mapping of the bridge.

## group(Object)

Sets the audio group of the source, or `null`, and applies its volume.

## write(short[])

Mixes the samples down to stereo when they have more than two channels, fills a processed or a new buffer with them and queues it; plays again when the source ran dry while playing.

## getBufferedSamples()

Returns the number of queued samples that OpenAL has not played yet, or `0` without a context.

## sync()

Follows the current context and sends the mapped gain to OpenAL when it changed; returns `false` when no context is current.

## isPlaying()

If `true`, the source was asked to play and was not paused or stopped since.
