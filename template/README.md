# JOID backend template

A starting point to run [JOID](https://github.com/Zeldown/JOID) @JOID_VERSION@ on an engine of your choice, in your own repository, with only the JOID jars.

A backend extends `Backend`, creates three bridges — `IRenderBridge`, `IWindowBridge` and `IAudioBridge` — and never modifies the JOID core. The template compiles as is: `ExampleBackend` already registers the three bridges, whose methods throw `UnsupportedOperationException` until you implement them.

## Setup

1. Put the JOID jars listed in [`libs/README.md`](libs/README.md) in `libs/`.
2. Rename the `com.example.joid.backend` package in `src/main/java`, `src/test/java` and `src/demo/java`, `group`, `archivesBaseName`, `backendPackage` and the `main` class of `runDemo` in `build.gradle`, and `rootProject.name` in `settings.gradle`.
3. Add the libraries of your engine to the `compile` dependencies. When your engine runs on GLFW or OpenAL, add `joid-base-lwjgl3-glfw` and `joid-base-openal` to the `embed` configuration instead of writing those bridges (on OpenGL, add `joid-base-opengl` too and implement its bindings instead of a render bridge):

```groovy
dependencies {
    embed files("libs/joid-base-lwjgl3-glfw-${joidVersion}.jar", "libs/joid-base-openal-${joidVersion}.jar")
}
```

The JOID core needs no declaration: its jars embed its libraries (Gson, Guava, Commons IO, Commons Lang, JSVG, TwelveMonkeys and ASM relocated under `dev.joid.shaded`, JavaCV, JavaCPP and FFmpeg with its natives).

## Implementing the backend

| Class | Role |
|---|---|
| `ExampleBackend` | Extends the core `Backend`: its `register()` checks the JOID version (an `IllegalStateException` when the loaded JOID has another major version than the one the build writes into `backend.properties` in the `backendPackage` of `build.gradle`, a newer minor version is accepted), then registers the bridges your hooks create (`createAudioBridge()`, `createWindowBridge()`, `createRenderBridge()`, and `createThreadBridge()`, `new FrameThreadBridge()` by default, which runs the tasks sent from other threads at the start of `frame()`), calls `registerDefaults()` (a `WindowUIBridge` by default; override it to add your resource resolvers, asset locators, key resolvers...) and `attachInput()` (override it to forward the input of your engine to `BridgeHandler.UI`), then loads JOID and runs the `JOID.onReady` callbacks. The application calls `ExampleBackend.create().register()`, then `frame()` in its loop. |
| `render/ExampleRenderBridge` | Extends the core `RenderBridge`, which tracks matrices and state in Java. Implement `clearColorBuffer`, `clearDepthBuffer`, `clearStencilBuffer`, `drawPrimitive`, `createTexture`, `createFrameBuffer` and `createShader`, applying the current state when they run. |
| `window/ExampleWindowBridge` | Window size, mouse, keyboard and clipboard. |
| `audio/ExampleAudioBridge` | Streaming audio sources used by the video player. |
| `demo/DemoWindow` | Opens the JOID demo UIs on your engine. It lives in `src/demo/java`, so only the demo jar contains it. |
| `ExampleSnapshotBackend` (tests) | Creates an offscreen surface, runs a frame, captures its pixels and names the renderer. |

The render bridge contract — vertex layout, projection conventions, textures, framebuffers, state stack, shaders — is described in [Writing a backend](https://documentation.joid.dev/build/writing-a-backend/).

## Tasks

| Command | Result |
|---|---|
| `./gradlew test` | Runs `RenderBridgeContractTest` and `BitmapFontContractTest`, which check the render bridge contract in a few seconds, and `SnapshotTest`, which renders every JOID scenario and compares each shot pixel for pixel to the references of the GPU in `.snapshots/references`; a shot without a reference for this GPU fails the test. |
| `./gradlew updateSnapshots` | Records the references of the GPU, the first time and after an intended visual change. |
| `./gradlew renderBaseline` | Renders the same scenarios with the official LWJGL 3 backend into `build/snapshots/lwjgl3`. |
| `./gradlew crossBackendTest` | Runs the tests and the baseline, then compares your shots to the LWJGL 3 ones within one level per channel. |
| `./gradlew build` | Builds three jars that embed the JOID core of the same variant, with the libraries its jars contain (Gson, Guava, Commons IO, Commons Lang, JavaCV, JavaCPP, FFmpeg, JSVG, TwelveMonkeys, ASM), and the `embed` jars: `joid-backend-example-1.0.0-prod.jar` to ship, `joid-backend-example-1.0.0-dev.jar` with the JOID dev tools (inspector, hot reload, warnings) for the developers who use your backend, and `joid-backend-example-1.0.0-demo.jar` with the dev tools, the JOID demos and `DemoWindow`, for you only. JOID turns its dev and demo modes on by itself when their classes are in the jar. |
| `./gradlew testDemoJar testDevJar testProdJar` | Runs the tests against the packaged jars instead of the classes: every test for the demo jar, the contract and unit tests for the dev and prod jars. |
| `./gradlew runDemo` | Launches `DemoWindow`. |

Every run writes an interactive `report.html` next to the renders, and its link is printed at the end of the build when a test fails.

On macOS, `renderBaseline` starts its JVM with `-XstartOnFirstThread`, which GLFW needs. When your engine runs on GLFW too, add `jvmArgs '-XstartOnFirstThread'` to `runDemo` and to the test tasks; leave it out for an AWT or Swing engine, which it would block.

## License

JOID is licensed under the [Apache License 2.0](https://github.com/Zeldown/JOID/blob/main/LICENSE), and so is this template. Your backend stays yours: license it as you want, as long as you keep the JOID notices in what you redistribute.
