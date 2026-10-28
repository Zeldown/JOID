---
title: "Demos"
description: "The demo jar, the three jars prod, dev and demo, the modes of JOIDEnv, the demo launchers and the index of every demo."
genre: build
sidebar:
  order: 2
---

The demos are UIs that show every feature of JOID, one card per case.
They exist for backend authors: run them on a backend to see it draw everything, and read them to find a working example of any feature.
They ship only in the demo jars, never to an application.

## Three jars per module

The core and every backend of a release come in three variants, and `./gradlew build` in the template builds the same three for your backend.
The `-prod` and `-dev` jars are those of the applications ([Choose the dev or the prod jar](/use/get-started/installation/#choose-the-dev-or-the-prod-jar)); the `-demo` jar adds to the dev content the demo UIs with their fonts, models, textures and videos, and the demo windows, for backend authors only.

The variants differ by package: `dev/joid/demo/**`, `assets/demo/**` and the `demo` packages of the base modules and of the backends leave the dev jar; the dev tools, `assets/dev/**` and the `snapshot` packages also leave the prod jar.
To check a packaged jar, call [[JarCheck#run]] from a main of your own and run it on the jar with its libraries: it registers your backend, draws 60 frames of a UI, then exits with `0`, or with `1` when the modes do not match the variant, a stack trace is printed or the UI closed.

## Modes

The jar decides the modes; nothing in the code of an application turns them on.
[[JOIDEnv]] reads them once, from the classes present:

| Mode | On when the classpath holds | Read with |
|---|---|---|
| Dev | the class of the inspector, [[DevNode]] (dev and demo jars) | [[JOIDEnv#isDevMode]] |
| Demo | `DemoFont` (demo jar) | [[JOIDEnv#isDemoMode]] |

When it loads, JOID loads the dev font in both modes and the demo fonts in demo mode.
A test can turn the dev mode off or on with [[JOIDEnv#devMode]] ([Testing a backend](/build/testing-a-backend/#force-the-dev-mode-in-a-test)); the demo mode follows the jar alone.

## Launch the demos

Each official backend has a launcher in its demo jar: run its main class with the libraries of the backend on the classpath, and `-XstartOnFirstThread` on macOS for the GLFW ones.

| Backend | Main class of the demo jar | Window |
|---|---|---|
| LWJGL 3 | `dev.joid.backend.lwjgl3.demo.DemoWindow` | GLFW, OpenGL 3.3 core requested |
| Vulkan | `dev.joid.backend.vulkan.demo.DemoWindow` | GLFW without client API, through a [[WindowVulkanHost]] |
| LWJGL 2 | `dev.joid.backend.lwjgl2.demo.DemoWindow` | `Display`, 24-bit depth, 8-bit stencil, key repeats on |
| Your backend | `./gradlew runDemo` in the template | the `DemoWindow` of `src/demo/java` |

Each official launcher opens a 1920×1080 resizable window titled `JOID - Demo (<backend>)`, registers itself as the UI bridge, then registers the backend, opens the menu and runs the loop.
Registered first, the launcher is the only UI bridge: `registerDefaults()` finds the UI registry filled and adds no [[WindowUIBridge]], so the window shows the dark grey window background of the demos.
The `DemoWindow` of the template opens no window: it registers itself the same way, then calls `frame()` until you give it the loop of your engine ([Run it](/build/writing-a-backend/#run-it)).

The launchers are UI bridges: `DemoUIBridge`, a [[WindowUIBridge]] with a dark grey window background and the dev font for its tooltips, opens the menu in `start()` through its `openMenu()` hook.
The GLFW launchers share `GlfwDemoWindow` of base-glfw, whose subclass names the backend, sets the window hints, registers the backend and presents each frame.

In the demos:

- the menu shows one card per demo, and a click opens it; Escape always comes back to the menu, which itself never closes;
- Ctrl+K opens a popup from the menu, and from that popup again;
- the shortcuts of the dev mode work as in any application ([Dev tools](/use/get-started/dev-tools/)).

## Index

| Demo | Shows |
|---|---|
| `UIDemoChoice` | The menu. |
| `UIDemoSimple` | Rectangles, borders, hover colors, circles, progress bars. |
| `UIDemoOption` | The options of a UI and of popups: scale, interface scale, zoom, anchors, backdrop, transitions, tasks, forced opening. |
| `UIDemoOverlay` | An overlay, turned on and off from its card of the menu. |
| `UIDemoLayout` | Flex and grid layouts. |
| `UIDemoOverflow` | Scrolling and scrollbars. |
| `UIDemoText` | Alignment, truncation, modifiers. |
| `UIDemoFont` | Fonts, weights and glyphs. |
| `UIDemoMarkup` | Text markup and text effects. |
| `UIDemoResource` | Images, SVG, URLs, a failed resource, crops, mipmaps, custom formats and locators. |
| `UIDemoPlayer` | Video and animated images. |
| `UIDemoAudio` | The sound of videos: volume and positional sound. |
| `UIDemoEvent` | Mouse and key events, lifecycle listeners, tasks and shortcuts, tooltips. |
| `UIDemoEventFlow` | The order of the events: priorities, consumption, listeners of the UI, the printed key against the physical position. |
| `UIDemoCursor` | Cursors and click-through. |
| `UIDemoDraggable` | Dragging and snapping. |
| `UIDemoReorderable` | Reorderable lists. |
| `UIDemoTextField` | Text, integer and multiline fields. |
| `UIDemoControl` | Checkbox, toggle, switch, and a control of your own. |
| `UIDemoSlider` | Double, integer and string sliders. |
| `UIDemoSelector` | Selectors. |
| `UIDemoSignal` | Signals and collections of signals. |
| `UIDemoWatch` | Watches, waits and silent writes. |
| `UIDemoStore`, `UIDemoOtherStore` | Stores of every scope and remembered fields, shared between two screens. |
| `UIDemoEffect` | Rounded corners and the other effects. |
| `UIDemoAnimation` | Node animations: yoyo, delay, start on click. |
| `UIDemoTween` | Tweens, easings and timelines. |
| `UIDemoRender` | The drawing API, the matrix stack, blending, framebuffers, shader pipelines and custom shaders. |
| `UIDemoShader` | Gradients, masks, custom effects and effects under a zoom. |
| `UIDemoChart` | Charts, radar included. |
| `UIDemoModel` | The 3D model viewer. |

The snapshot scenarios of the testkit open these demos, so every visible feature is checked on every backend ([Testing a backend](/build/testing-a-backend/)).
