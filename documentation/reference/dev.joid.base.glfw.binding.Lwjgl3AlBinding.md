---
summary: The OpenAL binding of LWJGL 3, given to `AlAudioBridge` by the LWJGL 3 and Vulkan backends.
---

It lives in base-glfw because base-openal depends on no LWJGL version.

Learn: [OpenAL of LWJGL 3](/build/base-modules/glfw/#openal-of-lwjgl-3), [base-openal](/build/base-modules/openal/).

## inst()

Returns the only instance.

## createContext()

Opens the default device with `alcOpenDevice`, creates a context on it and makes it current.
Throws an `IllegalStateException` when no output device opens, or when the device refuses a context.

## destroyContext()

Releases the current context when it is its own, destroys it and closes the device; does nothing when it created none.

## getCurrentContext()

Returns the OpenAL capabilities of the current context, or `null` when no context is current.

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
