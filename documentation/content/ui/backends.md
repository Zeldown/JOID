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

The repository is a multi-module Gradle build. `core` contains the neutral library, and each backend is a module under `impl/` with its own `build.gradle`, its shaders and a ready-to-run `DemoWindow` (`./gradlew :vulkan:runDemo`). LWJGL 3 and Vulkan share the `glfw` window module and the `openal` audio module.

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

A backend implements `IWindowBridge`, `IAudioBridge` and `IRenderBridge`, and never modifies the `core` module. Add it as a module under `impl/`, include it in `settings.gradle` and apply `gradle/backend.gradle` to package it with the core.

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

### Shaders

Shaders live in the `core` module and are written once in JOID GLSL (see [Custom Shaders](../shaders/custom.md)). The core parses each stage into a `ShaderSource` — varyings, uniforms, samplers, the built-ins it uses and its body — and passes both stages to `createShader(ShaderSource, ShaderSource, BlendState)`. The backend only generates the declarations of its language in front of the body:

| Backend | Language | Generated declarations |
|---|---|---|
| LWJGL 2 | GLSL 120 | `#define` of the built-ins onto `gl_Vertex`, `gl_MultiTexCoord0`, `gl_Color`, `gl_ProjectionMatrix`, `gl_ModelViewMatrix`, `gl_FragColor`… and of `texture` onto `texture2D`. Varyings become `varying`. |
| LWJGL 3 | GLSL 330 | Attributes at locations `0` position, `1` uv, `2` color, `3` normal, built-in uniforms, `in` / `out` varyings, `out vec4 fragColor`. |
| Vulkan | GLSL 450 | Same attributes, the uniforms of both stages in a single `std140` block at `binding = 0`, samplers from `binding = 1`, varying locations shared by both stages, `layout(location = 0) out vec4 fragColor`. |

Every generated header ends with a `#line` directive, so compilation errors point to the original file. Samplers that are not set through a `SamplerUniform` receive the currently bound texture. The LWJGL 3 and Vulkan backends draw without a bound shader through `/assets/shaders/fixed`, and wrap the fragment `main` to apply the render state alpha test.

### Tests

Each backend implements `ISnapshotBackend` in its `src/test` sources — create an offscreen surface, run a frame, capture its pixels, release it — and extends `SnapshotSuite`:

```java
public class SnapshotTest extends SnapshotSuite {

    @Override
    protected ISnapshotBackend createBackend() {
        return new SnapshotBackend();
    }

}
```

The suite registers a `ManualClockBridge`, checks that every core shader compiles, then plays the `static` and `interaction` scenarios of `core/src/test/resources/snapshot` on the demo UIs. Time only advances by frames of 16 ms, and each shot waits for the resources being loaded then for two identical consecutive frames, so every run renders the same pixels. Shots are compared to the references in `src/test/snapshots` with a tolerance of 8 per channel.

| Command | Result |
|---|---|
| `./gradlew test` | Shader unit tests and snapshot tests of every module. Rendered shots and differences are written to `build/snapshots`. |
| `./gradlew updateSnapshots` | Replaces the references after an intended visual change. |
| `./gradlew crossBackendTest` | Runs the tests, then compares the `lwjgl3` and `vulkan` shots to `lwjgl2`, tolerating up to 2000 pixels per shot for sub-pixel rasterization differences. Differences are written to `build/snapshots/cross`. |

Snapshot tests need a GPU. The pre-commit hook installed by `./gradlew installLocalGitHook` runs `check` and `crossBackendTest` whenever a commit touches the sources or the build.

## See also

- [Bridge](bridge.md) — hosting UIs and forwarding input.
- [Custom Shaders](../shaders/custom.md) — writing shaders for each backend.
