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

Calls `alGenSources`, then places the source at the listener with `AL_SOURCE_RELATIVE` and a position of zero.

## deleteSource(int)

Calls `alDeleteSources`.

## genBuffer()

Calls `alGenBuffers`.

## deleteBuffer(int)

Calls `alDeleteBuffers`.

## bufferData(int,int,short[],int)

Calls `alBufferData` through a direct buffer that it reuses, with `AL_FORMAT_MONO16` or `AL_FORMAT_STEREO16`.

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
