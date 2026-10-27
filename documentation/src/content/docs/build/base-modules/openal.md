---
title: "base-openal"
description: "The OpenAL module: an audio bridge of streaming sources over a binding, with gains mapped by the host and an optional shared context."
genre: build
sidebar:
  order: 2
---

`joid-base-openal` implements the [audio bridge](/build/bridges/audio/) on OpenAL.
It calls OpenAL only through an [[IAlBinding]], so the same code runs on LWJGL 2, LWJGL 3 or the OpenAL of an engine, and the module depends on no LWJGL.

## Create the bridge

[[AlAudioBridge#create]] takes the binding of your library:

| Binding | Library |
|---|---|
| [[Lwjgl3AlBinding#inst]] | LWJGL 3 OpenAL, in [base-glfw](/build/base-modules/glfw/) |
| [[Lwjgl2AlBinding#inst]] | LWJGL 2 OpenAL, in the LWJGL 2 backend: copy it into your backend, as the GL bindings ([Embedding in a host](/build/embedding-in-a-host/#register-once-the-context-is-current)) |
| your [[IAlBinding]] | the OpenAL of your engine |

Three settings adapt it to a host:

| Setting | Default | Effect |
|---|---|---|
| [[AlAudioBridge#ownContext]] | `true` | With `true`, the bridge creates an OpenAL context on the default device when the first source needs one, and destroys it when the JVM exits. With `false`, the host owns the context, and the bridge never creates nor destroys one. |
| [[AlAudioBridge#gain]], with an [[AudioGain]] | the gain unchanged | Maps the gain and the audio group of each source to the volume sent to OpenAL. |
| [[AlAudioBridge#groupVolume]] | `1F` for every group | Multiplies the volume of every source of a group. |

The groups come from the application, which tags a video with any object; a host maps them to its own volume settings:

```java file="com/example/backend/EngineBackend.java" region="audio"
```

## Streaming sources

Each [[AlAudioSource]] streams through a queue of OpenAL buffers:

- `write(samples)` reuses a processed buffer or creates one, mixes more than two channels down to stereo, queues it, and plays again when the source ran dry while playing;
- `getBufferedSamples()` counts the queued samples that OpenAL has not played yet;
- `stop()` drops the queue.

A source follows the context: when no context is current, every call does nothing; when the context changed, the source creates its OpenAL objects again on the next call, so it follows a host that recreates its sound engine.
The gain is mapped again on each call and sent to OpenAL only when it changed.

## Write a binding

Each method of [[IAlBinding]] maps to one OpenAL call, except the context methods:

| Member | Contract |
|---|---|
| [[IAlBinding#createContext]], [[IAlBinding#destroyContext]] | Open and close a context on the default device, for `ownContext(true)`. |
| [[IAlBinding#getCurrentContext]] | An object proper to the current context, or `null` without one. |

The sources compare that object by identity (`==`): return the same instance while the context lives, and a new one when the context is created again, even when the native handle is the same.
A boxed handle, created anew at each call, makes every source create its objects again at each call.
