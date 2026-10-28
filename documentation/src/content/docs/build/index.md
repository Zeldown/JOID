---
title: "Build on JOID"
description: "Documentation for backend authors: architecture, bridges, backends, base modules, pipeline, tests and demos."
genre: build
sidebar:
  order: 0
---

This documentation is for backend authors.
You run JOID on a graphics, window or audio stack that JOID does not ship, or inside an engine that owns the window and the loop.

It assumes that you know how to build an interface with JOID.
If you do not, start with [Use JOID](/use/): every notion of the user API (UIs, nodes, signals, effects) lives there, and the pages here link to it instead of teaching it again.

## Where to start

| You want to | Read |
|---|---|
| Understand how the modules depend on each other | [Architecture and layers](/build/architecture/) |
| Know what JOID asks from a backend, bridge by bridge | [The bridges](/build/bridges/) |
| Run JOID on your own engine | [Writing a backend](/build/writing-a-backend/), then [Testing a backend](/build/testing-a-backend/) |
| Forward the mouse and the keyboard of your window | [Input forwarding](/build/input/) |
| Draw JOID inside an engine that owns the window and the loop | [Embedding in a host](/build/embedding-in-a-host/) |
| Reuse the GLFW, OpenAL or OpenGL code of the official backends | [Base modules](/build/base-modules/) |
| Know what reaches your render bridge | [Render pipeline](/build/pipeline/), [Shader translation](/build/shader-translation/), [Framebuffers and depth](/build/framebuffers-and-depth/) |
| Let [[Resource#of]] take the images and textures of your engine | [Resource resolvers](/build/resource-resolvers/) |
| See how an official backend is put together | [LWJGL 3](/build/backends/lwjgl3/), [Vulkan](/build/backends/vulkan/), [LWJGL 2](/build/backends/lwjgl2/) |
| See every feature of JOID running on a backend | [Demos](/build/demos/) |

The [reference](/reference/) lists every public type of JOID, member by member, the bridges and the testkit included.

## What you need

- JDK 8: JOID compiles to Java 8 bytecode, and the Gradle 3.5 wrapper of the backend template runs on it.
- The JOID jars of the release you target, listed in the `libs/README.md` of the template.
- The library of your engine, and a machine where it can open a hidden surface, because the tests render real frames.
