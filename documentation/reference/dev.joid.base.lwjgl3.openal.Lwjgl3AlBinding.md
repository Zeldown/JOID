---
summary: The OpenAL binding of LWJGL 3, given to `AlAudioBridge` by the LWJGL 3 and Vulkan backends.
---

Learn: [base-lwjgl3-openal](/build/base-modules/lwjgl3-openal/), [base-openal](/build/base-modules/openal/).

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

Calls `alGenSources`, then places the source at the listener with `AL_SOURCE_RELATIVE` and a position of zero.

## deleteSource(int)

Calls `alDeleteSources`.

## genBuffer()

Calls `alGenBuffers`.

## deleteBuffer(int)

Calls `alDeleteBuffers`.

## bufferData(int,int,short[],int)

Calls `alBufferData` with `AL_FORMAT_MONO16` or `AL_FORMAT_STEREO16`.

## play(int)

Calls `alSourcePlay`.

## stop(int)

Calls `alSourceStop`.

## pause(int)

Calls `alSourcePause`.

## gain(int,float)

Calls `alSourcef` with `AL_GAIN`.

## queueBuffer(int,int)

Calls `alSourceQueueBuffers`.

## unqueueBuffer(int)

Calls `alSourceUnqueueBuffers`.

## isPlaying(int)

Calls `alGetSourcei` with `AL_SOURCE_STATE`.

## getQueuedBuffers(int)

Calls `alGetSourcei` with `AL_BUFFERS_QUEUED`.

## getProcessedBuffers(int)

Calls `alGetSourcei` with `AL_BUFFERS_PROCESSED`.
