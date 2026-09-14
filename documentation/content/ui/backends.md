# Backends

JOID's core is engine-agnostic. Nodes, effects, the shader pipeline, fonts, resources and video never call OpenGL, Vulkan, GLFW or OpenAL directly — they go through four bridges registered in `BridgeHandler`. A backend is the set of classes that implements those bridges for one engine.

| Bridge | Responsibility |
|---|---|
| `IUIBridge` | Hosts UIs: open / close, hover rendering, input dispatch. See [Bridge](bridge.md). |
| `IWindowBridge` | Window size, mouse position, mouse grab, keyboard state, clipboard. |
| `IRenderBridge` | Matrix stacks, render state, textures, framebuffers, shaders, draw calls. |
| `IAudioBridge` | Streaming audio sources used by the video player. |

`BridgeHandler.getWindow()`, `getRender()` and `getAudio()` throw an `IllegalStateException` with an explicit message when the matching bridge was never registered.

## Available backends

The `main` branch contains the neutral library. Each backend lives in its own branch, ships its own `build.gradle`, its shaders and a ready-to-run `DemoWindow` (`./gradlew runDemo`).

| Branch | Stack | Register | Shaders |
|---|---|---|---|
| `impl/lwjgl-2` | LWJGL 2.9.1 — OpenGL fixed pipeline, OpenAL | `LWJGL2Backend.register()` | GLSL 120 |
| `impl/lwjgl-3` | LWJGL 3.3.4 — GLFW, OpenGL 3.3 core, OpenAL | `LWJGL3Backend.register(window)` | GLSL 330 |
| `impl/vulkan` | LWJGL 3.3.4 — GLFW, Vulkan 1.3, shaderc, OpenAL | `VulkanBackend.register(window)` | Vulkan GLSL 450 |

### LWJGL 2

Register once the `Display` exists, then register your `IUIBridge`:

```java
Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
LWJGL2Backend.register();
BridgeHandler.register(myBridge);
JOID.inst().load();
```

The LWJGL 2 backend maps every call natively onto the fixed pipeline and restores the host state it changes, which makes it safe inside an existing LWJGL 2 host. It needs the `native/` folder on `java.library.path`.

### LWJGL 3

Create a GLFW window with an OpenGL 3.3 core context and a stencil buffer, make it current, then register:

```java
GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
GLFW.glfwMakeContextCurrent(window);
GL.createCapabilities();
LWJGL3Backend.register(window);
```

Natives are resolved from Maven for the current OS by the branch `build.gradle`.

### Vulkan

Vulkan owns the swapchain, so the host drives the frame explicitly:

```java
Configuration.STACK_SIZE.set(1024);
GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
VulkanBackend.register(window);

final VulkanRenderBridge render = (VulkanRenderBridge) BridgeHandler.getRender();
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

A backend implements `IWindowBridge`, `IAudioBridge` and `IRenderBridge`, and never modifies the `main` branch.

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

Shaders are backend assets loaded from `/assets/shaders/<name>/<name>.vsh` and `.fsh`, so each branch ships the language of its engine:

| Backend | Language | Conventions |
|---|---|---|
| LWJGL 2 | GLSL 120 | Fixed attributes (`gl_Vertex`, `gl_Color`, `gl_MultiTexCoord0`). |
| LWJGL 3 | GLSL 330 | Attributes at locations `0` position, `1` uv, `2` color, `3` normal. Uniforms `uProjectionMatrix` and `uModelViewMatrix`. Output `out vec4 fragColor`. |
| Vulkan | GLSL 450 | Same attributes. Vertex uniforms in a `std140` block at `binding = 0`, fragment uniforms at `binding = 1`, samplers from `binding = 2`. Varyings `vPosition`, `vTexCoord`, `vColor` at locations `0`, `1`, `2`. Output `layout(location = 0) out vec4 fragColor`. |

Samplers that are not set through a `SamplerUniform` receive the currently bound texture. The LWJGL 3 and Vulkan backends wrap the fragment `main` to apply the render state alpha test, which is why the output must be named `fragColor`.

## See also

- [Bridge](bridge.md) — hosting UIs and forwarding input.
- [Custom Shaders](../shaders/custom.md) — writing shaders for each backend.
