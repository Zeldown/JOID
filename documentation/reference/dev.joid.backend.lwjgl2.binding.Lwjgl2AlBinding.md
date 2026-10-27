---
summary: The OpenAL binding of LWJGL 2, given to `AlAudioBridge` by the LWJGL 2 backend.
---

Learn: [LWJGL 2 backend](/build/backends/lwjgl2/), [base-openal](/build/base-modules/openal/).

## inst()

Returns the only instance.

## createContext()

Creates the OpenAL context of LWJGL 2 with `AL.create()`.
Throws an `IllegalStateException` with the reason of LWJGL when no output device can be opened.

## destroyContext()

Destroys the OpenAL context of LWJGL 2 when it exists.

## getCurrentContext()

Returns the current context of OpenAL, or `null` while LWJGL 2 has no OpenAL context.

## genSource()

Creates a source placed at the listener, so that no position attenuates or pans it, and returns its name.

## deleteSource(int)

Deletes the source.

## genBuffer()

Creates a buffer and returns its name.

## deleteBuffer(int)

Deletes the buffer.

## bufferData(int,int,short[],int)

Copies the samples into a direct buffer that it reuses, then fills the OpenAL buffer, mono for one channel and stereo for two.

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
