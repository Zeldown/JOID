---
summary: The OpenAL calls that `AlAudioBridge` needs, implemented once per LWJGL version.
---

A backend gives a binding to [[AlAudioBridge#create]], so that base-openal depends on no LWJGL version.
Every method runs on the render thread.

Learn: [Write a binding](/build/base-modules/openal/#write-a-binding).

## createContext()

Opens the default audio device and makes an OpenAL context current on it; throws an `IllegalStateException` when the device or the context cannot be created.

## destroyContext()

Destroys the context that `createContext()` created and closes its device; does nothing when there is none.

## getCurrentContext()

Returns an object that identifies the current OpenAL context, or `null` when none is current.
A source compares it between calls to notice a context that the host replaced.

## genSource()

Creates a source placed at the listener, so that no position attenuates or pans it, and returns its name.

## deleteSource(int)

Deletes the source.

## genBuffer()

Creates a buffer and returns its name.

## deleteBuffer(int)

Deletes the buffer.

## bufferData(int,int,short[],int)

Fills the buffer with interleaved 16-bit samples, mono for one channel and stereo for two, at that sample rate in hertz.

## play(int)

Plays the source.

## stop(int)

Stops the source; its queued buffers become processed.

## pause(int)

Pauses the source.

## gain(int,float)

Sets the gain of the source, `1F` for 100 %.

## queueBuffer(int,int)

Appends the buffer to the queue of the source.

## unqueueBuffer(int)

Removes the oldest buffer from the queue of the source and returns its name.

## isPlaying(int)

If `true`, the source is in the playing state.

## getQueuedBuffers(int)

Returns the number of buffers in the queue of the source, played or not.

## getProcessedBuffers(int)

Returns the number of queued buffers that the source has finished playing.
