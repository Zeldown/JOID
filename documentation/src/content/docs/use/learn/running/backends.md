---
title: "Choosing a backend"
description: "Pick a backend, check what it needs, start it in your program and style its window bridge."
genre: learn
sidebar:
  order: 1
---

A backend runs JOID on a graphics and window library.
Your UIs never mention it: switching backends changes your startup code and nothing else.

**You will learn**
- Which backend fits your application, and what it needs from the machine
- How to start a backend and run its frames
- What `register()` does, and which settings come before it
- How to run code once JOID is ready
- How to style the window bridge: the window background and the tooltips

## Pick the backend that fits your application

JOID has three backends.
They draw the same pixels for the same UI, so the choice depends on your application, never on your UIs.

| | LWJGL 3 | Vulkan | LWJGL 2 |
|---|---|---|---|
| Class | [[Lwjgl3Backend]] | [[VulkanBackend]] | [[Lwjgl2Backend]] |
| Window and input | GLFW (LWJGL 3.3.4) | GLFW (LWJGL 3.3.4) | `Display`, `Mouse` and `Keyboard` (LWJGL 2.9.1) |
| Input | attached by the backend | attached by the backend | read by [[Lwjgl2InputForwarder]] in your loop |
| Show a frame | `glfwSwapBuffers` | `endFrame()` of the Vulkan host | `Display.update()` |

Start with **LWJGL 3**: it is the reference backend, the one whose pixels the other two are compared against.
Choose **Vulkan** when your application already renders with Vulkan.
Choose **LWJGL 2** when your application already runs on LWJGL 2.

## Check the prerequisites

| Requirement | LWJGL 3 | Vulkan | LWJGL 2 |
|---|---|---|---|
| Java | 8 or later | 8 or later | 8 or later |
| Graphics | an OpenGL 2.1 or later context, core or compatibility profile, with GLSL 1.20 and framebuffer objects | a Vulkan 1.3 device able to present to the window | an OpenGL 2.1 context with GLSL 1.20 and framebuffer objects |
| Buffers | 24-bit depth, 8-bit stencil | created by the Vulkan host | 24-bit depth, 8-bit stencil |
| macOS | start the JVM with `-XstartOnFirstThread` | start the JVM with `-XstartOnFirstThread` | nothing to add |

The stencil buffer clips the masks of your UIs: without it, masked content draws outside its mask.
Framebuffer objects come with OpenGL 3.0, or with the `GL_ARB_framebuffer_object` or `GL_EXT_framebuffer_object` extension of an older context.
On a machine below the minimum, `register()` throws `JOID needs OpenGL 2.1 with GLSL 1.20, this context offers …` or `JOID needs framebuffer objects (…), this context offers …`; with Vulkan, creating the host throws `No Vulkan 1.3 device able to present to the window was found`.

[Installation](/use/get-started/installation/) lists the jars and the libraries to declare for each backend.

## Start a backend

JOID never creates a window or a loop: your application owns both.
Create the window and its graphics context, then hand the window to the backend:

```java file="com/example/running/RunningApp.java" region="window"
```

[[GlContextRequest]] sets the GLFW hints of the context before the window exists: `CORE_33` asks for an OpenGL 3.3 core profile, with the 24-bit depth and 8-bit stencil buffers that JOID needs.
`COMPATIBILITY` asks for the default context of the driver instead, for an application that also draws with the fixed pipeline of OpenGL, and `CORE_32_FORWARD_COMPATIBLE` for a driver that offers only a 3.2 forward-compatible core profile.
`GL.createCapabilities()` belongs to LWJGL: it loads the OpenGL functions of the current context, and JOID calls none before it.

```java file="com/example/running/RunningApp.java" region="register"
```

`create(window)` builds the backend; `register()` connects it to JOID.
Call `register()` once the graphics context exists and is current, on the thread of your loop: the render bridge creates GPU objects at once, and that thread becomes the [render thread](/use/learn/running/threads/).

Then open a UI and call `frame()` once per frame of your loop:

```java file="com/example/running/RunningApp.java" region="loop"
```

[[Backend#frame]] draws one frame of every open UI.
It follows the size of the window by itself, so you need no resize callback.
[Embedding JOID](/use/learn/running/embedding/) shows how to draw other things in the same loop.

<figure>

![A dark grey Settings button on a dark card, in the middle of the window](/captures/running-backends-start.webp)

<figcaption>The program above, with `RunningStartUI` open: the window shows the UI from the first frame.</figcaption>
</figure>

### Close the UIs before the program ends

`closeAll()` of the window bridge, after the loop, closes every UI that the bridge holds: their close listeners and the `onDetach` listeners of their nodes run, their [stores and remembered fields](/use/learn/state/stores-and-properties/) reach the disk.
Call it on each UI bridge of your own as well, once the loop is over.

A program that ends with UIs still open closes none of them.
When the JVM exits normally, a shutdown hook of JOID still saves the stores and the remembered fields, but no close listener runs; a process that is killed or crashes saves nothing.

### Start the other backends

The program stays the same; only the window, the backend and the way you show a frame change.

**Vulkan.** Raise the stack size of LWJGL before anything else, create the window without an OpenGL context, and give the backend a [[WindowVulkanHost]], which owns the device and the swapchain of the window:

```java file="com/example/running/VulkanApp.java" title="VulkanApp.java"
```

- Each frame of the loop draws between `beginFrame()` and `endFrame()` of the host, so the first frame begins right after `register()`.
- `endFrame()` shows the image on the window, and `beginFrame()` starts the next one at once.
- After the loop, `destroy()` releases the backend, then the host.

**LWJGL 2.** Call [[Lwjgl2Natives#install]] before the `Display` exists: it extracts the LWJGL 2 natives into the [cache folder](/use/learn/running/files/), unless `org.lwjgl.librarypath` or `java.library.path` already provides them.
Register the backend once the `Display` exists:

```java file="com/example/running/Lwjgl2App.java" title="Lwjgl2App.java"
```

LWJGL 2 has no input callbacks, so [[Lwjgl2InputForwarder#poll]] reads the input at the start of each frame.

## What register() does

`register()` runs these steps in this order:

1. It throws `IllegalStateException` when the backend is already registered: a backend registers once.
2. It checks that the backend was built for the same major version of JOID, and throws `The backend was built for JOID … but JOID … is loaded: update the backend` when it was not.
3. It applies the [data and cache folders](/use/learn/running/files/#move-the-folders) set with `dataDir(...)` and `cacheDir(...)`.
4. It registers the audio bridge of the backend, unless you called `audio(false)`, then its window and render bridges.
5. It registers a thread bridge tied to the calling thread, which becomes the render thread: work sent from other threads [runs there at the start of the next frame](/use/learn/running/threads/#bring-a-result-back-to-the-render-thread).
6. It adds the window bridge, unless you registered a UI bridge of your own before ([Embedding JOID](/use/learn/running/embedding/#draw-joid-over-your-own-scene)).
7. It attaches the input to the window (LWJGL 3 and Vulkan).
8. It loads JOID: it prints a banner with the version, the dev mode and the two folders (`[JOID] … loaded in dev mode, data in …, cache in …`), cleans the [cache folder](/use/learn/running/files/#keep-files-of-your-own-in-the-cache) on a background thread, and installs the shutdown hook that saves the stores.
9. It runs the code given to [[JOID#onReady]].

The settings of a backend come between `create(...)` and `register()`, all optional:

| Setting | Effect |
|---|---|
| [[Backend#audio]] | `false` registers no audio bridge, for tests and machines without an audio device. A video with a sound track still plays its picture. |
| [[Backend#dataDir]], [[Backend#cacheDir]] | Move the folders where JOID writes, see [Where JOID stores files](/use/learn/running/files/#move-the-folders). |

```java fragment
final Lwjgl3Backend backend = Lwjgl3Backend.create(window).audio(false).register();
```

## Run code once JOID is ready

Code that runs before the backend is registered cannot open a UI: no bridge is there to receive it.
A library, a plugin or a module that does not control the startup order gives its setup to [[JOID#onReady]] instead:

```java file="com/example/running/RunningLibrary.java" region="ready"
```

Before `register()`, JOID keeps that code and runs it at the end of `register()`, on the render thread.
Once the backend is registered, the code runs at once on the render thread, or at the start of the next frame when you call `onReady` from another thread.
`JOID.inst().isReady()` tells you which case you are in.

Code given before `register()` that throws prints `[JOID] A JOID.onReady callback failed (…)`, and the next `onReady` code still runs.
Once JOID is ready, the exception reaches the caller on the render thread, and code sent from another thread prints `[JOID] A task sent to the render thread from another thread failed (…)`.

## Style the window bridge

When you register no UI bridge of your own, the backend registers a [[WindowUIBridge]].
Every frame, it clears the window with its background color, draws the open UIs and draws the tooltip of the hovered node.

Get it from the UI registry once the backend is registered, and set what you need:

```java file="com/example/running/RunningApp.java" region="bridge"
```

| Setting | What it changes |
|---|---|
| [[WindowUIBridge#background]] | The color that clears the window before every frame, behind every UI. |
| [[WindowUIBridge#hoverInfo]] | The font, size and color of the tooltip text. |
| [[WindowUIBridge#hoverColor]] | The fill of the tooltip box. |
| [[WindowUIBridge#hoverBorderColor]] | The 1-pixel border of the tooltip box. |

<figure>

![The pointer moves onto the Settings button and rests there; a dark tooltip with the light text Opens the settings appears below it to the right](/captures/running-backends-tooltip.webp)

<figcaption>The tooltip of the window bridge with its default colors.</figcaption>
</figure>

:::caution
JOID ships no font for your application, so the window bridge has no tooltip style until you give it one.
With the dev jar, it prints `[JOID] WindowUIBridge draws no tooltip, set its hoverInfo(TextInfo) to draw them` once; with the prod jar, tooltips stay invisible.
:::

`getBridge(WindowUIBridge.class)` returns the highest bridge of that class or of a subclass, and `null` when you registered a UI bridge of another kind before the backend.
[Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/) shows how a node declares its tooltip.

## Write a backend of your own

To run JOID on another library or engine, implement its bridges: [Writing a backend](/build/writing-a-backend/) walks through it.

## Recap

- A **backend** runs JOID on LWJGL 3, Vulkan or LWJGL 2; your UIs are the same on all three.
- Start with **LWJGL 3**; pick Vulkan or LWJGL 2 to match the library your application already uses.
- Create the window and its context, then `create(...)`, the optional settings, and **`register()`** once, on the thread of your loop.
- Call **`frame()`** once per frame of your loop; it follows the window size by itself.
- After the loop, **`closeAll()`** closes the open UIs, so that their close listeners run and their stores reach the disk.
- **`JOID.onReady`** runs setup code once the backend is registered, whatever the startup order.
- The **window bridge** clears the window with `background`, and draws tooltips once you set **`hoverInfo`**.

## Try it

**A light window.**
Give the window the background `#DDDDDD` and tooltips with dark grey text on a white box.

<details>
<summary>Solution</summary>

```java fragment
BridgeHandler.UI
.getBridge(WindowUIBridge.class)
.background(Color.decode("#DDDDDD"))
.hoverColor(Color.WHITE)
.hoverInfo(TextInfo.create(font, 20F).color(new Color(85, 85, 85)));
```

</details>

**Open from a library.**
Move the `JOID.open(...)` call of the program above into a library class that does not know when the backend starts.

<details>
<summary>Solution</summary>

Wrap it in `JOID.onReady(() -> JOID.open(new RunningStartUI()));`.
The code runs at the end of `register()`, or at once if the backend is already registered.

</details>
