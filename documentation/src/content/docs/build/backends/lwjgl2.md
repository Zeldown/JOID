---
title: "LWJGL 2 backend"
description: "What the LWJGL 2 backend is made of: the OpenGL, OpenAL and LWJGL 2 modules, bundled natives and JNA, and input polled by the loop."
genre: build
sidebar:
  order: 1
---

`joid-backend-lwjgl2` runs JOID on LWJGL 2.9.1, for applications built on its `Display`.
How an application installs and starts it is on [Choosing a backend](/use/learn/running/backends/).

## What it is made of

| Bridge | Class | From |
|---|---|---|
| Render | [[GlRenderBridge]] over [[Lwjgl2GlBinding]] | [base-opengl](/build/base-modules/opengl/), [base-lwjgl2](/build/base-modules/lwjgl2/) |
| Audio | [[AlAudioBridge]] over [[Lwjgl2AlBinding]] | [base-openal](/build/base-modules/openal/), [base-lwjgl2](/build/base-modules/lwjgl2/) |
| Window | [[Lwjgl2WindowBridge]] | base-lwjgl2 |
| Input | [[Lwjgl2InputForwarder]], called by the loop | base-lwjgl2 |

Its own code is [[Lwjgl2Backend]], which registers them, and [[Lwjgl2Natives]]; `create()` takes no window, because LWJGL 2 has a single `Display`.
The backend keeps the default thread bridge and the default UI bridge of [[Backend]].

## What differs

### Natives

The jar bundles the LWJGL 2 natives for Windows, Linux and macOS.
[[Lwjgl2Natives#install]] installs them; whoever creates the `Display` calls it before `Display.create()`, and the backend never calls it itself:

1. When `org.lwjgl.librarypath` is set, or the natives are already on `java.library.path`, it does nothing.
2. Otherwise it extracts them into the cache folder of JOID ([Where JOID stores files](/use/learn/running/files/)), under `lwjgl`, and sets `org.lwjgl.librarypath`.

An extraction that fails throws an `IllegalStateException` that names the folder, and the next call tries again.

### Input belongs to the loop

LWJGL 2 delivers input through queues that the application loop drains, so the backend attaches nothing in `attachInput()`.
The application calls [[Lwjgl2InputForwarder#poll]] once per frame, before `frame()` ([Input forwarder](/build/base-modules/lwjgl2/#input-forwarder)).

### JNA

The jar embeds JNA 5.17, in its own packages, for the [native cursors](/build/base-modules/lwjgl2/#cursors) of base-lwjgl2.

### Context

The `Display` and the OpenGL context that the backend needs are on [Check the prerequisites](/use/learn/running/backends/#check-the-prerequisites).

## Tests

[[Lwjgl2SnapshotBackend]] renders into a `Display` on Linux and into a `Pbuffer` elsewhere, both with a 24-bit depth and an 8-bit stencil.
[Testing a backend](/build/testing-a-backend/#the-suites) runs the contract suites and the snapshot suite on your backend.
