---
title: "LWJGL 3 backend"
description: "What the LWJGL 3 backend is made of: the GLFW, OpenAL and OpenGL modules, its context requests, and the OpenGL profiles it is tested on."
genre: build
sidebar:
  order: 2
---

`joid-backend-lwjgl3` runs JOID on LWJGL 3 with a GLFW window and an OpenGL context.
It is the reference backend: the other backends compare their renders with its own ([Testing a backend](/build/testing-a-backend/#compare-with-lwjgl-3)).
How an application installs and starts it is on [Choosing a backend](/use/learn/running/backends/).

## What it is made of

| Bridge | Class | From |
|---|---|---|
| Render | [[GlRenderBridge]] over [[Lwjgl3GlBinding]] | [base-opengl](/build/base-modules/opengl/) |
| Audio | [[AlAudioBridge]] over [[Lwjgl3AlBinding]] | [base-openal](/build/base-modules/openal/), [base-lwjgl3-glfw](/build/base-modules/lwjgl3-glfw/) |
| Window | [[GlfwWindowBridge]] | base-lwjgl3-glfw |
| Input | [[GlfwInputForwarder]], attached in `attachInput()` | base-lwjgl3-glfw |

Its own code is small: [[Lwjgl3Backend]], the LWJGL 3 OpenGL bindings and [[GlContextRequest]].

## What differs

### The OpenGL binding is replaceable

[[Lwjgl3Backend#binding]] takes another [[IGlBinding]] than `Lwjgl3GlBinding.inst()`.
The snapshot backend uses it to hide extensions from the render bridge ([[ProfileGlBinding]]); a host can use it to route the OpenGL calls through its own layer.

### Context requests

[[GlContextRequest]] sets the GLFW hints of the context before the window exists; [Start a backend](/use/learn/running/backends/#start-a-backend) gives its three requests.
On macOS, `CORE_33` also asks for a forward-compatible context, which macOS requires.

## OpenGL profiles

The render bridge adapts to the context: dialect, framebuffer family, vertex arrays, mipmap copies ([base-opengl](/build/base-modules/opengl/)).
[[Lwjgl3SnapshotBackend]] renders on the [[SnapshotProfile]] named by the system property `joid.snapshot.profile`, so that a test run checks one path:

| Profile | Context |
|---|---|
| `DEFAULT` | OpenGL 3.3 core requested, whatever version the driver gives. |
| `GL_20` | OpenGL 2.0 with GLSL 1.10, which JOID must refuse with a clear message. |
| `GL_21` | OpenGL 2.1. |
| `GL_21_EXT` | OpenGL 2.1 without the ARB framebuffer and vertex array extensions: the EXT framebuffer family. |
| `GL_21_EXT_NO_BLIT` | The same without framebuffer blits: mipmaps are drawn. |
| `GL_30` | OpenGL 3.0. |
| `GL_32_FORWARD_COMPATIBLE` | OpenGL 3.2 core, forward-compatible. |
| `GL_33` | OpenGL 3.3 core. |
| `GL_45_COMPATIBILITY` | OpenGL 4.5 compatibility. |

Every profile but `DEFAULT` checks the version and the profile that the driver gives; a driver that cannot give them fails the test with a message that suggests `MESA_GL_VERSION_OVERRIDE` and `MESA_GLSL_VERSION_OVERRIDE`.

## Tests

`Lwjgl3SnapshotBackend` opens a hidden GLFW window with the request of the profile, turns vertical sync off and registers the backend with `audio(false)`.
[Testing a backend](/build/testing-a-backend/#the-suites) runs the contract suites and the snapshot suite on your backend.
