# Backends

JOID's core is engine-agnostic. Nodes, effects, the shader pipeline, fonts, resources and video never call OpenGL, Vulkan, GLFW or OpenAL directly — they go through the bridges registered in `BridgeHandler`. A backend is the set of classes that implements those bridges for one engine.

| Bridge | Responsibility |
|---|---|
| `IUIBridge` | Hosts UIs: open / close, hover rendering, input dispatch. See [Bridge](bridge.md). |
| `IWindowBridge` | Window size, mouse position, mouse grab, keyboard state, clipboard. |
| `IRenderBridge` | Matrix stacks, render state, textures, framebuffers, shaders, draw calls. |
| `IAudioBridge` | Streaming audio sources used by the video player. |
| `IClockBridge` | Time of animations, scheduled tasks, text cursors, double clicks and `Node.wait`. `SystemClockBridge` is registered by default, `ManualClockBridge` controls time by hand. |

Every bridge implements `IBridge`, and `BridgeHandler` exposes one `BridgeRegistry` per bridge type: `UI`, `WINDOW`, `RENDER`, `AUDIO` and `CLOCK`. A registry keeps every registered bridge ordered by `getIndex()` — `0` by default, the latest registration wins on ties.

| Method | Result |
|---|---|
| `register(bridge)` | Adds the bridge to the registry. |
| `get()` | The bridge with the highest priority. Throws an `IllegalStateException` with an explicit message when none was registered. |
| `find(filter)` | The highest-priority bridge matching the predicate, or `null`. |
| `getBridge(MyBridge.class)` | The highest-priority bridge of that class, or `null`. |

`BridgeHandler.UI` is a `UIBridgeRegistry`: it adds `get(ui)` and `get(MyUI.class)`, which return the bridge able to handle the `UI`.

## Available backends

The repository is a multi-module Gradle build. `core` contains the neutral library, and each backend is a module under `impl/` with its own `build.gradle`, its shaders and a ready-to-run `DemoWindow` (`./gradlew :vulkan:runDemo`). LWJGL 3 and Vulkan share the `glfw` window module and the `openal` audio module. The `testkit` module contains the snapshot test framework shared by the backends.

| Module | Stack | Register | Generated shaders |
|---|---|---|---|
| `lwjgl2` | LWJGL 2.9.1 — OpenGL fixed pipeline, OpenAL | `Backend.register()` | GLSL 120 |
| `lwjgl3` | LWJGL 3.3.4 — GLFW, OpenGL 3.3 core, OpenAL | `Backend.register(window)` | GLSL 330 |
| `vulkan` | LWJGL 3.3.4 — GLFW, Vulkan 1.3, shaderc, OpenAL | `Backend.register(window)` | Vulkan GLSL 450 |

Implementation classes are named by role — `Backend`, `RenderBridge`, `Shader`, `Texture`… — and their package, `be.zeldown.joid.impl.<module>`, tells which engine they belong to.

### LWJGL 2

Register once the `Display` exists, then register your `IUIBridge`:

```java
import be.zeldown.joid.impl.lwjgl2.Backend;

Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
Backend.register();
BridgeHandler.UI.register(myBridge);
JOID.inst().load();
```

The LWJGL 2 backend maps every call natively onto the fixed pipeline and restores the host state it changes, which makes it safe inside an existing LWJGL 2 host. It needs the `native/` folder of the `lwjgl2` module on `java.library.path`.

### LWJGL 3

Create a GLFW window with an OpenGL 3.3 core context and a stencil buffer, make it current, then register:

```java
import be.zeldown.joid.impl.lwjgl3.Backend;

GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
GLFW.glfwMakeContextCurrent(window);
GL.createCapabilities();
Backend.register(window);
```

Natives are resolved from Maven for the current OS by the module `build.gradle`.

### Vulkan

Vulkan owns the swapchain, so the host drives the frame explicitly:

```java
import be.zeldown.joid.impl.vulkan.Backend;
import be.zeldown.joid.impl.vulkan.render.RenderBridge;

Configuration.STACK_SIZE.set(1024);
GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
Backend.register(window);

final RenderBridge render = (RenderBridge) BridgeHandler.RENDER.get();
while (!GLFW.glfwWindowShouldClose(window)) {
    GLFW.glfwPollEvents();
    bridge.update();
    render.beginFrame();
    bridge.draw();
    render.endFrame();
    render.present();
}
```

`Configuration.STACK_SIZE` must be raised before the first LWJGL call: Vulkan instance creation enumerates every layer and extension on LWJGL's thread-local stack. A Vulkan 1.3 device is required; smooth and wide lines are enabled when the driver supports them.

## Input conventions

- Mouse coordinates are in pixels, origin at the **top-left** corner of the window.
- Keys are the engine-neutral `Key` enum. `Key.LEFT_CONTROL.isDown()` queries the window bridge; `keyPressed(char, Key, InternalContext)` receives the key that produced the event.
- The character and the key are delivered together. GLFW backends merge the key and char callbacks before dispatching.

## Writing a backend

A backend implements `IWindowBridge`, `IAudioBridge` and `IRenderBridge`, and never modifies the `core` module. Add it as a module under `impl/`, include it in `settings.gradle` and apply `gradle/backend.gradle` to package it with the core. It can also live in its own repository and depend only on the JOID jars, as described in *Backend in its own repository* below.

### Render bridge

Two approaches are supported:

- **Native** — implement `IRenderBridge` directly and forward every call to a stateful API (the LWJGL 2 backend).
- **Emulated** — extend `RenderBridge`. Matrices and state are tracked in Java (`getModelView()`, `getProjection()`, `getState()`), and your implementation only provides `clear`, `clearStencil`, `draw`, `createTexture`, `createFrameBuffer` and `createShader`, applying the current state when they run (the LWJGL 3 and Vulkan backends).

The contract every backend follows:

- `draw(DrawMode, VertexBuffer)` receives `TRIANGLES` or `LINES` from the `Tessellator`. Each vertex is 32 bytes in native order: position `3×float` at offset 0, texture coordinates `2×float` at 12, color `RGBA8` at 20, normal `3×int8` at 24. `isTexture()`, `isColor()` and `isNormal()` tell which attributes are present — use the current color when colors are absent.
- Projection matrices follow OpenGL conventions (clip-space depth in `[-1, 1]`, Y up, viewport origin at the bottom-left). Backends with other conventions convert them.
- `resetTexture()` binds an opaque white texture, so shaders can always sample.
- `ITexture.upload` receives `ARGB` integers. `ITexture.delete()` may be called more than once.
- Framebuffers only have a color attachment — no depth or stencil.
- `pushState()` / `popState()` restore everything set through the bridge, including the bound framebuffer, the viewport and the current shader.
- `lighting(true)` means an ambient term of `0.6` plus a directional light along the view axis, applied per vertex with flat shading.
- Smooth lines are drawn by the core: the `Tessellator` expands each segment of a line drawn with `lineSmooth(true)` into a quad in screen space, and the `line` shader computes the OpenGL antialiased coverage, so backends only draw triangles. `isLineSmooth()`, `getLineWidth()`, `getViewportWidth()` and `getViewportHeight()` expose the state it needs.
- `getPixelGrid()` places the window pixels at the current transform: the number of pixels covered by one unit, and where each unit lands on the window. `RenderBridge` computes it from its matrices; a native bridge reads its projection and model-view matrices and returns `PixelGrid.of(projection, modelView, viewportWidth, viewportHeight)`. Effects size their textures from it and align them on the window pixels, and images snap their corners to it.

### Shaders

Shaders live in the `core` module and are written once in JOID GLSL (see [Custom Shaders](../shaders/custom.md)). The core parses each stage into a `ShaderSource` — varyings, uniforms, samplers, the built-ins it uses and its body — and passes both stages to `createShader(ShaderSource, ShaderSource, BlendState)`. The backend only generates the declarations of its language in front of the body:

| Backend | Language | Generated declarations |
|---|---|---|
| LWJGL 2 | GLSL 120 | `#define` of the built-ins onto `gl_Vertex`, `gl_MultiTexCoord0`, `gl_Color`, `gl_ProjectionMatrix`, `gl_ModelViewMatrix`, `gl_FragColor`… and of `texture` onto `texture2D`. Varyings become `varying`. |
| LWJGL 3 | GLSL 330 | Attributes at locations `0` position, `1` uv, `2` color, `3` normal, built-in uniforms, `in` / `out` varyings, `out vec4 fragColor`. |
| Vulkan | GLSL 450 | Same attributes, the uniforms of both stages in a single `std140` block at `binding = 0`, samplers from `binding = 1`, varying locations shared by both stages, `layout(location = 0) out vec4 fragColor`. |

Every generated header ends with a `#line` directive, so compilation errors point to the original file. Samplers that are not set through a `SamplerUniform` receive the currently bound texture. The LWJGL 3 and Vulkan backends draw without a bound shader through `/assets/shaders/fixed`, and wrap the fragment `main` to apply the render state alpha test.

### Tests

Snapshot tests live in the `testkit` module. A backend implements `ISnapshotBackend` — create an offscreen surface of the requested size, run a frame, capture a region of its pixels, release it and name the renderer — and extends `SnapshotSuite` and `RenderBridgeContractSuite` in its tests:

```java
public class SnapshotTest extends SnapshotSuite {

    @Override
    protected ISnapshotBackend createBackend() {
        return new SnapshotBackend();
    }

}
```

`RenderBridgeContractTest` extends `RenderBridgeContractSuite` the same way. The contract suite checks the render bridge without reference images, in a few seconds: every core shader compiles, the current color and vertex colors are drawn, `ARGB` textures show their first texel at the top-left, `resetTexture()` binds an opaque white texture, framebuffers keep what is drawn into them, `popState()` restores the framebuffer, shader, viewport and line state, a texture can be deleted twice and the OpenGL projection puts the origin at the top-left of the capture.

The snapshot suite registers a `ManualClockBridge`, then plays each scenario of `testkit/src/main/resources/snapshot` — `static`, `interaction`, `transition`, `popup`, `window`, `dev` and `video`. Every scenario starts from the same state: no UI, the clock at the same instant, a 1920×1080 window, dev mode off, no key held and no mask. Time only advances with `wait` and `moveto`, by frames of 16 ms, and lerps and the fps counter follow the frame time measured on the clock, so every run renders the same pixels. Each shot waits for the resources being loaded, for every video to display the frame matching the clock, then for two identical consecutive frames. Videos and GIFs follow the clock, so a paused clock freezes them, audio is muted, and URL resources are downloaded once into `.snapshots/cache` so later runs work offline.

References belong to each machine and graphics card: they are stored in `.snapshots/<module>/<renderer>/`, which git ignores. A shot without reference is recorded on the first run; afterwards every shot must match its reference pixel for pixel. References of shots removed from the scenarios are deleted after the run.

The suites read their folders from system properties, with defaults that work from any IDE or build tool: `joid.snapshot.references` (`.snapshots/references`), `joid.snapshot.output` (`build/snapshots/renders`), `joid.snapshot.cache` (`.snapshots/cache`) and `joid.snapshot.update`. The JOID build sets them for each module. In the JOID backends, `SnapshotBackend` lives in the `snapshot` package of the main sources, so their dev jar can render a baseline, and their prod jar leaves it out.

| Command | Result |
|---|---|
| `./gradlew test` | Shader unit tests and snapshot tests of every module. Renders and an interactive `report.html` are written to `build/snapshots/<module>`. |
| `./gradlew updateSnapshots` | Replaces the references after an intended visual change. `./gradlew :vulkan:updateSnapshots` updates a single module. |
| `./gradlew crossBackendTest` | Runs the tests, then compares the `lwjgl3` and `vulkan` shots to `lwjgl2` with a tolerance of one level per channel, which absorbs the antialiasing rounding of each driver. The comparison is shown in `build/snapshots/cross/report.html`. |

Snapshot tests need a GPU. The hooks installed by `./gradlew installLocalGitHook` run `scripts/run-tests`: the pre-commit hook tests the staged changes and the pre-push hook the pushed commits, after stashing everything else. Only the modules touched by the changes are tested — `core`, `testkit` and the build test every backend, `glfw` and `openal` test LWJGL 3 and Vulkan — and the cross comparison reuses the last renders of the other backends.

#### Report

Each `report.html` lists the shots with their status and their number of different pixels, and shows the reference and the render of the selected shot in eight modes: side by side, swipe, onion skin, blink, amplified difference, highlighted pixels, reference and render. The wheel zooms around the cursor down to single pixels with a pixel grid, dragging pans, and hovering a pixel shows its coordinates, both colors and the delta of each channel. `Next difference` groups the different pixels into regions and zooms on each one, so even a single pixel is found. The report opens straight from the disk, without a server. When a test fails, Gradle prints the link of the report at the end of the build.

#### Scenarios

A scenario is a text file with one command per line; `#` starts a comment.

| Command | Effect |
|---|---|
| `ui <class>` | Closes every UI, opens the UI and moves the mouse out of the window. |
| `open <class>` | Opens the UI through `JOID.open`, with its transitions and popups. |
| `wait <ms>` | Advances the clock frame by frame. |
| `move <x> <y>` | Moves the mouse. |
| `moveto <x> <y> <ms>` | Moves the mouse progressively, dragging while a button is pressed. |
| `press <button>` / `release` | Presses or releases a `ClickType`. |
| `scroll <value>` | Scrolls, `120` per notch. |
| `type <text>` | Types the text, holding `LEFT_SHIFT` for uppercase letters and shifted symbols. |
| `key <KEY>[+<KEY>...]` | Holds every key of the combination and sends the last one, for example `key LEFT_CONTROL+K`. |
| `down <KEY>` / `up <KEY>` | Holds or releases a key for the following commands. |
| `resize <width> <height>` | Resizes the window, up to 1920×1080. |
| `zoom <level>` | Sets the zoom level of the opened UIs. |
| `dev <true\|false>` | Enables or disables dev mode for the UIs opened afterwards. |
| `mask <x> <y> <width> <height>` | Fills the rectangle in the following shots, to exclude content that cannot be deterministic such as memory usage. |
| `unmask` | Removes the masks. |
| `shot <name>` | Captures the window as `<name>.png`. |

To add a capture, add its commands to a scenario — or add a scenario file and its `matches…Snapshots` test to `SnapshotSuite` — then run `./gradlew test`: the new shots are recorded as references.

`UIDemoVideo` masks its statistics overlay, which shows the memory usage and the decoder queues.

## Backend in its own repository

A backend does not have to live in the JOID repository: each release publishes the jars needed to develop, test and package one elsewhere.

| Artifact | Contents |
|---|---|
| `joid-core-X.Y.Z-dev.jar` | The core and the demo assets, to compile, test and run the demo. |
| `joid-core-X.Y.Z-prod.jar` | The same core without `assets/demo`, embedded in the prod jar of the backend. |
| `joid-testkit-X.Y.Z.jar` | `SnapshotSuite`, `RenderBridgeContractSuite`, the scenarios, the report, `SnapshotBaseline` and `SnapshotComparison`. It needs JUnit 4. |
| `joid-glfw-X.Y.Z.jar`, `joid-openal-X.Y.Z.jar` | The GLFW window and OpenAL audio bridges, for engines built on them. |
| `joid-<backend>-X.Y.Z-dev.jar` | The official backends, with their `SnapshotBackend` to render a baseline. |
| `joid-backend-template-X.Y.Z.zip` | A Gradle project to start from. |

The template compiles against the jars of its `libs/` folder and declares the libraries of the core — Guava, Gson, commons-lang3, commons-compress, commons-io and vecmath, plus JavaCV and FFmpeg for video — since no JOID jar embeds a third-party library. It compiles as is, with bridges that throw `UnsupportedOperationException` until they are implemented:

| Command | Result |
|---|---|
| `./gradlew test` | The contract and snapshot suites, with references in `.snapshots/references`. |
| `./gradlew renderBaseline` | Renders the scenarios with the `SnapshotBackend` of `joid-lwjgl3-X.Y.Z-dev.jar`, in its own JVM. |
| `./gradlew crossBackendTest` | Compares the shots of the backend to that baseline within one level per channel. |
| `./gradlew build` | A dev jar and a prod jar that embed the matching core. |
| `./gradlew testDevJar testProdJar` | Runs the tests against the packaged jars. |
| `./gradlew runDemo` | Launches the demo window. |

`SnapshotBaseline <backend class> <output directory>` renders every scenario with an `ISnapshotBackend`, and `SnapshotComparison <report directory> <reference directory> <candidate directory>...` compares directories of shots, so any build tool can run them.

The `Backend` class of the template calls `JOID.checkVersion(version)` before registering the bridges: it prints a warning and returns `false` when the loaded JOID has another major version than the one the backend targets. A backend relies on `be.zeldown.joid.lib.bridge` and its subpackages — bridges, render state, shader sources and uniforms, textures, framebuffers and vertices —, on `be.zeldown.joid.internal.JOID` to load JOID and check its version, and on `be.zeldown.joid.demo` for its demo window.

## See also

- [Bridge](bridge.md) — hosting UIs and forwarding input.
- [Custom Shaders](../shaders/custom.md) — writing shaders for each backend.
