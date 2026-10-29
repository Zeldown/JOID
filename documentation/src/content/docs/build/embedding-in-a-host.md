---
title: "Embedding in a host"
description: "Run JOID inside an engine that owns the window, the loop and the OpenGL context: shared state, drawings of the host and a shared window."
genre: build
sidebar:
  order: 4
---

A host is an application that already draws its own frame: a game engine, an editor, a simulation.
It owns the window, the loop and the graphics context, and JOID draws its interface on top, inside the same frame.
This page covers what a backend for such a host does beyond [Writing a backend](/build/writing-a-backend/): share the OpenGL state, let the host draw inside JOID, and share the window and the input.
The user side, calling `frame()` from your own loop, is on [Embedding JOID](/use/learn/running/embedding/).

## Register once the context is current

The OpenGL render bridge reads the capabilities of the context when it is created, and throws an `IllegalStateException` when no context is current, once your binding returns `null` for the version ([base-opengl](/build/base-modules/opengl/#create-the-bridge)).
Register the backend on the thread of the context of the host, after the host made it current (on LWJGL 3, after `GL.createCapabilities()`).
Ask the host for a 24-bit depth buffer and an 8-bit stencil buffer: JOID clips its masks and scroll areas with the stencil.

This backend reuses the OpenGL and OpenAL modules on the context and the audio device of the host, with the window bridge of the engine:

```java file="com/example/backend/GlHostBackend.java" region="backend"
```

`binding` is the [[IGlBinding]] over the OpenGL functions of the host.
On LWJGL 3, depend on [base-lwjgl3-opengl](/build/base-modules/lwjgl3-opengl/) and pass `Lwjgl3GlBinding.inst()`; on LWJGL 2, copy the binding classes of the official backend ([[Lwjgl2GlBinding]], with its framebuffer bindings) into your backend, under their Apache 2.0 license; on another library, write the binding.
Do not depend on a final backend such as `joid-backend-lwjgl3` for its bindings: its jars embed the core, and yours would then carry two copies of it.

`ownContext(false)` tells the audio bridge that the host created the OpenAL context: it never creates nor destroys one ([base-openal](/build/base-modules/openal/)).
When the host draws on a thread of its own, add a [thread bridge](/build/bridges/thread/).

## Draw after the host frame

The host draws its scene, then asks JOID for one frame, then presents:

```java file="com/example/backend/HostLoop.java" region="loop"
```

`frame()` wraps the drawing of JOID in `beginFrame()` and `endFrame()` of the render bridge.
The OpenGL bridge draws into the framebuffer that the host has bound when `beginFrame()` runs: bind the target you want before `frame()`, the default framebuffer or a framebuffer of your own.

### The state journal

The OpenGL render bridge never calls OpenGL directly: it goes through a [[GlStateGuard]], whose [[JournalGlBinding]] notes in a [[GlStateJournal]] every piece of state that JOID changes.

1. `beginFrame()` enters the guard: the journal starts, and the guard puts the states that JOID relies on to known values (no scissor, no logic operation, no sRGB conversion, no polygon offset, depth function `LESS`, front faces counter-clockwise, pixel alignment 4, no pixel buffer bound, and on a compatibility profile no fog, lighting or alpha test).
2. The first time JOID changes a piece of state during the frame, the journal reads the value of the host and keeps it.
3. `endFrame()` exits the guard: the journal restores every value it kept, framebuffers and programs first, the active texture unit last.

Only what JOID touched is read and restored, so a frame costs a handful of queries, not a full dump of the state.
The state of the context after the frame is therefore the state before it, value for value: a host that keeps a cache of the OpenGL state on its side (bound textures and program, blending, depth, vertex arrays, viewport) finds that cache as right after the frame as it was before, and never needs to reset it.
The guard is re-entrant: nested `enter()` and `exit()` calls count their depth, and only the outermost pair starts and restores the journal.

### Let the host draw inside a frame

A node that shows something drawn by the host (a 3D preview, an item of the game) calls the host inside [[IRenderBridge#suspend]]:

```java file="com/example/backend/HostPreviewNode.java" region="draw"
```

On OpenGL, `suspend` restores the state of the host, runs the drawing, then starts the journal again and resets the pipeline.
A JOID call made inside the drawing of the host, such as a nested raster, opens and closes its own journal, so the host keeps its state.
The host draws with its own matrices, so the node gives it its rectangle in screen pixels from the bottom-left corner of the viewport, read from the [[PixelGrid]].

[[IRenderBridge#raster]] does the same into a JOID framebuffer at its pixel size, so that the effects of the node apply to the drawing of the host; the OpenGL bridge binds that framebuffer and its viewport around the drawing.
The user side is [[DrawUtils#RASTER]] ([Drawing shapes and text](/use/learn/going-further/drawing/#show-what-another-renderer-draws)).

### Import the matrices of a fixed-function host

A host on a compatibility profile can keep its interface matrices in the fixed-function stacks.
[[FixedMatrixImport]], with `FixedMatrixImport.create(bridge).apply()`, loads its projection and model-view into the matrix stacks of JOID, in place of the projection that `screen(width, height)` builds.
`create` throws an `IllegalStateException` on a core profile, which has no such matrices.

## Run inside a Vulkan host

A Vulkan host gives JOID its device, its command buffer and its target image instead of a context.
[Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host) gives that contract.

## Share the window

The window bridge reports the size of the surface of the host and its mouse, and returns `true` from `isMouseGrabbed()` while the host locks the cursor: JOID then leaves the cursor alone.
The UI bridges of a host (one that keeps the scene of the host behind the UIs, one that follows the screens that JOID opens) are on [Embedding JOID](/use/learn/running/embedding/), and their hooks on [UI bridge](/build/bridges/ui/).

## Forward the input through JOID first

The host asks JOID before it handles each event, and drops the events that JOID consumed ([Input forwarding](/build/input/)).
An overlay that lets the input through returns `false`, so the host keeps its controls under a transparent overlay.

## Check the guard

[[StateGuardContractSuite]] proves that a JOID frame leaves the context of the host as it found it.
Each test sets one unusual state in the host (one [[StateTrap]]: a scissor, a logic operation, a bound pixel buffer, client arrays, a sampler object, a read framebuffer…), runs a frame, and compares the result with a clean context, pixel for pixel.
It also checks that a raster which holds another raster gives its state back to the drawing inside it, and to the host after it.

Implement [[IStateGuardBackend]] in your tests to run it ([Testing a backend](/build/testing-a-backend/#the-suites)):

| Member | What it does |
|---|---|
| [[IStateGuardBackend#inject]] | Sets the state of one trap in the context, as a host would. |
| [[IStateGuardBackend#supports]] | Whether the context can set that trap; a trap it cannot set skips its test. |
| [[IStateGuardBackend#drawExternal]] | Draws like the host, with its own state. |
| [[IStateGuardBackend#fill]] | Fills a rectangle of the target with a color, outside JOID. |
| [[IStateGuardBackend#readState]] | Reads the state of the context as names and values, compared before and after the frame. |
