---
title: "Architecture and layers"
description: "The layers of JOID: the agnostic core, the base modules shared by several backends, and the thin final backends."
genre: build
sidebar:
  order: 1
---

JOID is split into three layers, so that one interface runs unchanged on OpenGL, Vulkan or the renderer of an engine.
A module depends only on the layers above it, never on a sibling of its own layer, with one exception: `base-glfw` depends on `base-openal`, because it holds the LWJGL 3 binding of OpenAL.

<figure>

![Three bands of boxes. At the top, joid-core: UIs, nodes, signals, the bridge contracts and the generic half of every backend. In the middle, three base modules, each with an arrow up to the core: base-glfw (window, input, OpenAL binding), with an arrow to base-openal (audio bridge over IAlBinding), and base-opengl (render bridge over IGlBinding). Between the middle and the bottom, a note reads: each backend uses the base modules named in its box. At the bottom, four backends: backend-lwjgl2 (openal, opengl), backend-lwjgl3 (glfw, openal, opengl), backend-vulkan (glfw, openal) and, highlighted in blue, your backend with the modules it reuses. A line under them reads: a backend depends on the core and on the base modules it names.](/captures/build-layers.webp)

<figcaption>
A backend picks the base modules it needs and adds only what is specific to its library.
Nothing below the core is visible to the code of a UI.
</figcaption>
</figure>

## The core knows no technology

`joid-core` holds everything that does not depend on a graphics, window or audio library:

- the UI engine: UIs, nodes, layout, signals, effects, animation, text and resources;
- the contracts that a backend implements, in `dev.joid.lib.bridge`: one interface per [bridge](/build/bridges/), and the registries of [[BridgeHandler]];
- the generic half of every backend: [[RenderBridge]] keeps the matrices, the render state and the frame in Java, [[Texture]] computes the mipmap chain, [[Shader]] parses and translates the shaders, and [[Backend]] registers the bridges in a fixed order.

The core never calls OpenGL, Vulkan, GLFW or OpenAL.
It reaches them only through the bridges.

## The base modules share one library between backends

A base module holds the code that several backends would otherwise copy, for one library:

| Module | Library | Gives |
|---|---|---|
| [base-glfw](/build/base-modules/glfw/) | GLFW and OpenAL of LWJGL 3 | the window bridge, the input forwarder, the key table, the OpenAL binding |
| [base-openal](/build/base-modules/openal/) | OpenAL, through [[IAlBinding]] | the audio bridge and its streaming sources |
| [base-opengl](/build/base-modules/opengl/) | OpenGL 2.1 and later, through [[IGlBinding]] | the render bridge, the state guard, textures, framebuffers, shaders and mipmaps |

`base-openal` and `base-opengl` call their library only through these binding interfaces, so they declare no LWJGL dependency: the binding you give them decides which library they call.

## The final backends stay thin

A final backend picks its base modules, adds what is specific to its library, and registers everything through a subclass of [[Backend]]:

| Backend | Base modules | Its own code |
|---|---|---|
| [LWJGL 2](/build/backends/lwjgl2/) | openal, opengl | the `Display` window bridge, the polled input, the natives, the native cursors, the LWJGL 2 bindings |
| [LWJGL 3](/build/backends/lwjgl3/) | glfw, openal, opengl | the LWJGL 3 OpenGL binding and the context hints |
| [Vulkan](/build/backends/vulkan/) | glfw, openal | the whole renderer, and the hosts that own the device and the frame |

Each jar of a backend embeds the core and its base modules, with Gson, Guava, Commons IO, Commons Lang, JSVG, TwelveMonkeys and ASM relocated under `dev.joid.shaded`; JavaCV, JavaCPP and FFmpeg keep their own packages, and so does JNA in the LWJGL 2 jars.
An application declares one JOID jar, next to the libraries of its backend (LWJGL, for the official ones).

## The tools sit beside the layers

| Module | Role |
|---|---|
| `tool-msdf` | Generates the MSDF atlases of the fonts; the core embeds it. |
| `tool-testkit` | Renders scenarios on a hidden surface and runs the contract suites that every backend passes ([Testing a backend](/build/testing-a-backend/)). |

The `snapshot` packages of `base-glfw`, `base-opengl` and of each final backend hold the hidden surfaces of the tests; those of `base-opengl` and of the backends use the testkit, and only the dev and demo jars carry them.

## Feed the core, never the interface

The code of a UI never adapts to its backend.
Rules that hold for every backend, such as the mipmap chain, the vertex layout or the depth conversion, already live in the core, so a backend only translates them for its library.

When a host has data of its own (its textures, its asset paths, its key bindings, its text objects), the backend feeds a registry of the core and the UI keeps calling the core API:

| Registry | Turns | Page |
|---|---|---|
| [[ResourceResolver]] | an object of the host (a sprite, a texture handle) into a resource | [Resource resolvers](/build/resource-resolvers/) |
| [[AssetLocator]] | a path of the host into a stream | [Custom formats](/use/learn/going-further/custom-formats/) |
| [[KeyResolver]] | a key binding of the host into keys | [Keyboard and shortcuts](/use/learn/interactivity/keyboard/) |
| [[TextConverter]] | a text object of the host into a string | [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/) |
