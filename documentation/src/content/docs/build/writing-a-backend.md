---
title: "Writing a backend"
description: "Write a backend step by step from the template: extend Backend, implement the bridges, then check it against the LWJGL 3 rendering."
genre: build
sidebar:
  order: 1
---

A backend runs JOID on a stack that JOID does not ship: another graphics API, the renderer of a game engine, another window library.
It creates the bridges for that stack and registers them through a subclass of [[Backend]], without any change to the core.
This page builds one, then checks it against the official LWJGL 3 rendering.

Before you start, read [Architecture and layers](/build/architecture/) and [The bridges](/build/bridges/).

## Choose what you write

| Your engine | Write | Model |
|---|---|---|
| Runs on GLFW, OpenAL or OpenGL | A [[Backend]] subclass that reuses the [base modules](/build/base-modules/); on OpenGL, an [[IGlBinding]] over the OpenGL functions of your engine. | [LWJGL 3 backend](/build/backends/lwjgl3/) |
| Has its own renderer (Vulkan, a modern engine) | A [[RenderBridge]] subclass with its textures, framebuffers and shaders, plus the bridges you cannot reuse. | [Vulkan backend](/build/backends/vulkan/) |
| Owns the window and the loop, and lets you draw inside its frame | One of the above, plus the rules of [Embedding in a host](/build/embedding-in-a-host/). | [LWJGL 2 backend](/build/backends/lwjgl2/) for an input queue that the loop drains |

The examples of this page target an imaginary engine: `Engine` and `EngineDevice` stand for its API, one method for one call of that API.

## Start from the template

Each release publishes `joid-backend-template-<version>.zip`, a Gradle project that compiles against the JOID jars only.

1. Put the jars listed in `libs/README.md` into `libs/`: the `prod`, `dev` and `demo` jars of the core, the testkit, and the `demo` jar of the LWJGL 3 backend, which renders the images your backend is compared with.
2. Rename the package `com.example.joid.backend` in `src/main/java`, `src/test/java` and `src/demo/java`.
   In `build.gradle`, rename `group`, `archivesBaseName`, `backendPackage` and the `main` class of the `runDemo` task; in `settings.gradle`, rename `rootProject.name`.
3. Add the libraries of your engine to the `compile` dependencies.
   Put the base modules you reuse into the `embed` configuration, so that they go inside your jars; `joid-base-glfw` needs `joid-base-openal` next to it.

**Check**: `./gradlew compileJava` succeeds.
The bridges of the template throw an `UnsupportedOperationException` that names the method to implement, so `register()` fails until you have implemented them.

## Extend Backend

[[Backend]] is the base of every backend: the three official ones are small `final` classes over it.
Your subclass creates its bridges in hooks:

```java file="com/example/backend/EngineBackend.java" region="backend"
```

[[Backend#register]] calls the hooks in this order, between its version check and the loading of JOID ([What register() does](/use/learn/running/backends/#what-register-does) gives the whole sequence from the side of an application):

| Hook | What it gives | Default |
|---|---|---|
| `createAudioBridge()` | The audio bridge; not called when the application set `audio(false)`. | `null`: no sound. |
| `createWindowBridge()` | The window bridge. | Required. |
| `createRenderBridge()` | The render bridge. | Required. |
| `createThreadBridge()` | The thread bridge; `null` keeps the bridge already registered. | A [[FrameThreadBridge]] on the thread that calls `register()`. |
| `registerDefaults()` | What goes into the shared registries. | A [[WindowUIBridge]] through `registerDefault`, registered only when the application registered no UI bridge before. |
| `attachInput()` | The connection from the input of the window to JOID. | Nothing. |

Override `registerDefaults()` with a call to `super.registerDefaults()` first, so that the default UI bridge stays.
`register()` runs once: a second call throws an `IllegalStateException`.

The build of the template writes the version of JOID it compiles against into `backend.properties`, in the package that `backendPackage` names: keep that package equal to the package of your [[Backend]] subclass.
`register()` reads that file and throws an `IllegalStateException` when the loaded JOID has another major version; without the file, as when you run from your IDE before any build, it checks nothing.

The application creates the backend, configures it, and calls `register()` once the engine is ready to draw, then [[Backend#frame]] in its loop.
The setters of [[Backend]] return the type of their target, so an application calls the setters of your subclass first and the common ones after:

```java fragment
final EngineBackend backend = EngineBackend.create(engine).audio(false).register();
```

## Implement the window bridge

The window bridge reports the size, the mouse, the held keys and the clipboard.
Its contract and the bridge of this engine are on [Window bridge](/build/bridges/window/).
On GLFW, return a [[GlfwWindowBridge]] of [base-glfw](/build/base-modules/glfw/).

## Implement the render bridge

Extend [[RenderBridge]]: it keeps the matrices and the state, and your subclass turns them into the calls of your API.

```java file="com/example/backend/EngineRenderBridge.java" region="bridge"
```

`drawPrimitive` applies the whole state at each draw: the core changed it since the previous draw without telling you.
For the same reason, `suspend` runs the drawing of a host as is: the next draw applies the state of JOID again.
`raster` targets the framebuffer at the given size and requires an open frame, like every draw.
[Render bridge](/build/bridges/render/) lists every rule a draw respects, and [Render pipeline](/build/pipeline/) explains what reaches it.

### Textures

Extend [[Texture]]: it keeps the size, the levels and the deletion, and calls four hooks.
Pixels arrive as ARGB `int`s, row by row from the top; `forEachStep` walks the mip levels, so that every backend fills the same levels ([Mipmaps](/build/pipeline/#mipmaps)).

```java file="com/example/backend/EngineTexture.java" region="texture"
```

### Framebuffers

Extend [[FrameBufferHandle]]: its constructor takes the color texture, and `deleteHandle()` releases the rest.
A framebuffer has a depth attachment, and no stencil that the core relies on ([Framebuffers and depth](/build/framebuffers-and-depth/)).

```java file="com/example/backend/EngineFrameBuffer.java" region="framebuffer"
```

### Shaders

Extend [[Shader]] and give its constructor a [[GlslShaderTranslator]] for the GLSL dialect of your API and its uniform layout:

```java file="com/example/backend/EngineShader.java" region="translator"
```

The constructor of [[Shader]] builds the uniform block and the samplers and translates both stages.
It compiles nothing: `compileProgram` runs at the first call of `isActive()`, so the fields of your subclass are ready by then.
Implement the three hooks, and send the uniform block at each draw when `pack()` reports a change:

```java file="com/example/backend/EngineShader.java" region="program"
```

[Shader translation](/build/shader-translation/) explains the dialects, the uniform layouts and what the translator adds.

## Add audio and a render thread

Both are optional.
Without audio, delete the `createAudioBridge()` override of the template: the default returns `null`.

- Audio: return an [[IAudioBridge]] from `createAudioBridge()` ([Audio bridge](/build/bridges/audio/)), or an [[AlAudioBridge]] on OpenAL ([base-openal](/build/base-modules/openal/)).
- Render thread: return an [[IThreadBridge]] from `createThreadBridge()` when your engine draws on a thread that does not call `register()`: the default bridge warns once when the frames run on another thread ([Thread bridge](/build/bridges/thread/)).

## Register the defaults

`registerDefaults()` fills the registries that several parties share.
The example registers a resource resolver, so that [[Resource#of]] accepts the sprites of the engine ([Resource resolvers](/build/resource-resolvers/)).
Nothing else belongs to the backend: an application or a plugin adds its markups, locators or other bridges itself, before or after `register()`.

## Forward the input

`attachInput()` connects the events of your window to [[BridgeHandler#UI]].
[Input forwarding](/build/input/) gives the contract and the forwarder of this engine.
When the events belong to the loop of the application (a queue drained each frame), leave `attachInput()` empty and give the application a forwarder to call, as the LWJGL 2 backend does.

## Run it

With every hook implemented, an application runs your backend like an official one.
The `DemoWindow` of the template, in `src/demo/java`, registers itself as the UI bridge from [[JOID#onReady]], registers `ExampleBackend`, opens the menu of the demos and calls `frame()` in an endless loop.
Give it the constructor of your backend and the loop of your engine: poll the events, call `frame()`, present.

**Check**: `./gradlew runDemo` opens the JOID demos on your engine ([Demos](/build/demos/)).
On macOS, an engine on GLFW needs `jvmArgs '-XstartOnFirstThread'` on `runDemo` and on the test tasks; an engine on AWT or Swing must not have it.

## Give the testkit a surface

The tests render real frames on a hidden surface, through the `ExampleSnapshotBackend` of the template in `src/test/java`.
Implement its methods as [Testing a backend](/build/testing-a-backend/#give-the-testkit-a-surface) shows for this engine; `RenderBridgeContractTest`, `BitmapFontContractTest` and `SnapshotTest` of the template already use it.

## Check it against LWJGL 3

The testkit compares your rendering with the official one, image by image.
Run these steps in order; each one assumes that the previous one is green.

1. `./gradlew test` runs the contract suites in a few seconds, then `SnapshotTest`, which renders every scenario; the first run fails until the references of your GPU exist.
2. `./gradlew updateSnapshots` records the references of your GPU.
3. `./gradlew crossBackendTest` runs the tests and renders the same scenarios with the LWJGL 3 backend, then compares your shots with those, one level per channel at most.
4. Open the `report.html` that a failure prints, fix, and run `crossBackendTest` again.
5. `./gradlew build` produces your `prod`, `dev` and `demo` jars; `./gradlew testDemoJar testDevJar testProdJar` runs the tests against them.

When a shot differs, check these first:

| Symptom | Usual cause |
|---|---|
| The whole image, or the content of a framebuffer, is upside down | The viewport is not flipped on the screen, or a framebuffer is not stored bottom-up ([Orientation](/build/framebuffers-and-depth/#orientation)). |
| Shapes vanish behind others, or 3D models look inside out | The depth range is 0 to 1 and the projection was not converted with [[DepthRange]], or the depth is not cleared to 1. |
| Masks and scroll areas do not clip | The screen has no 8-bit stencil buffer, or `clearStencilBuffer` clears nothing. |
| Text and images are blocky when drawn smaller | The mip levels are not filled with `forEachStep`, or the sampler ignores `isMipmapFiltered()`. |
| Colors are off by a few levels | The surface or the texture format is sRGB where JOID expects linear `UNORM`. |

The suites, the references and the reports are detailed on [Testing a backend](/build/testing-a-backend/).
