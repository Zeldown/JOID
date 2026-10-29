---
title: "Base modules"
description: "The modules a backend reuses for GLFW, OpenAL and OpenGL, one per library, and how to pick them."
genre: build
sidebar:
  order: 0
---

A base module holds the code of one library that several backends share ([Architecture and layers](/build/architecture/#the-base-modules-share-one-library-between-backends)).
Reuse one whenever your engine runs on its library: the official backends run on them, and they pass the contract suites of the testkit.

There are two kinds of base modules.
An API module holds the logic of an API over a binding interface, and depends on no library:

| Module | Use it when your engine | You give it |
|---|---|---|
| [base-openal](/build/base-modules/openal/) | plays sound with OpenAL | an [[IAlBinding]] |
| [base-opengl](/build/base-modules/opengl/) | gives you an OpenGL 2.1 or later context | an [[IGlBinding]] and its [[IGlFrameBufferBinding]]s |

A library module follows the jars of one library: it binds that library to an API module, or turns its window and its input into bridges.

| Module | Use it when your engine | You write |
|---|---|---|
| [base-lwjgl3-glfw](/build/base-modules/lwjgl3-glfw/) | opens its window with GLFW through LWJGL 3 | nothing: pass the window handle |
| [base-lwjgl3-openal](/build/base-modules/lwjgl3-openal/) | plays sound with the OpenAL of LWJGL 3 | nothing: give its binding to base-openal |
| [base-lwjgl3-opengl](/build/base-modules/lwjgl3-opengl/) | draws with the OpenGL of LWJGL 3 | nothing: give its binding to base-opengl |

Pick the modules of the libraries your engine already uses, and nothing else: a Vulkan engine takes no OpenGL module, an engine without sound no OpenAL module.
A library module depends on the API module it binds: embed both.

Each module is published as its own jar (`joid-base-<module>`), without its `snapshot` and `demo` packages (the hidden window and the demo window of base-lwjgl3-glfw, the snapshot capture of base-opengl), which only the dev and demo jars of the official backends carry.
No module carries the natives of its library: the application, or the final backend, adds the natives of its platform.
A backend embeds the modules it uses into its jars; in the template, put them into the `embed` configuration ([Start from the template](/build/writing-a-backend/#start-from-the-template)).