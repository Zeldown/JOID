---
title: "Base modules"
description: "The modules a backend reuses for GLFW, OpenAL and OpenGL, and how to pick them."
genre: build
sidebar:
  order: 0
---

A base module holds the code of one library that several backends share ([Architecture and layers](/build/architecture/#the-base-modules-share-one-library-between-backends)).
Reuse one whenever your engine runs on its library: the official backends run on them, and they pass the contract suites of the testkit.

| Module | Use it when your engine | You write |
|---|---|---|
| [base-glfw](/build/base-modules/glfw/) | opens its window with GLFW through LWJGL 3 | nothing: pass the window handle |
| [base-openal](/build/base-modules/openal/) | plays sound with OpenAL | an [[IAlBinding]], unless it is LWJGL 3, whose binding base-glfw holds |
| [base-opengl](/build/base-modules/opengl/) | gives you an OpenGL 2.1 or later context | an [[IGlBinding]] and its [[IGlFrameBufferBinding]]s; the module holds none |

Each module is published as its own jar (`joid-base-glfw`, `joid-base-openal`, `joid-base-opengl`), without the hidden and demo windows that only the jars of the official backends carry.
A backend embeds the modules it uses into its jars; in the template, put them into the `embed` configuration ([Start from the template](/build/writing-a-backend/#start-from-the-template)).
`base-glfw` depends on `base-openal`: embed both.
