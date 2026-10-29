---
title: "base-lwjgl3-opengl"
description: "The OpenGL binding of LWJGL 3 and its framebuffer bindings, for the render bridge of base-opengl."
genre: build
sidebar:
  order: 6
---

`joid-base-lwjgl3-opengl` holds [[Lwjgl3GlBinding]], the [[IGlBinding]] of the OpenGL library of LWJGL 3, with its two framebuffer bindings, [[Lwjgl3CoreGlFrameBufferBinding]] and [[Lwjgl3ExtGlFrameBufferBinding]].
Give `Lwjgl3GlBinding.inst()` to the render bridge of [base-opengl](/build/base-modules/opengl/#create-the-bridge) whenever your engine draws with the OpenGL of LWJGL 3: the LWJGL 3 backend does, and so does a host that owns the context ([Embedding in a host](/build/embedding-in-a-host/#register-once-the-context-is-current)).

The binding calls OpenGL on the context that is current on the calling thread, and returns `null` for the version when none is current.
It depends on base-opengl and on the `lwjgl` and `lwjgl-opengl` jars of LWJGL 3, never on their natives, nor on GLFW: the context may come from any window library.
