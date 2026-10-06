# Bridges

JOID never talks to a window, a graphics API or a sound device directly: it goes through bridges, small interfaces that a backend implements for one engine. `BridgeHandler` (`dev.joid.lib.bridge`) holds one registry per kind of bridge. Because your UIs only see these interfaces, the same UI code runs on every backend and every engine version. This page describes the registries and every bridge interface; [Backends](backends.md) lists the ready-made implementations.

## Registering the bridges

An application registers a backend (window, render and audio bridges), then its own UI bridge, then loads JOID:

```java
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.joid.impl.lwjgl3.Backend;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

GLFW.glfwInit();
GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);

final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
GLFW.glfwMakeContextCurrent(window);
GL.createCapabilities();

Backend.register(window);
BridgeHandler.UI.register(new AppUIBridge());
JOID.inst().load();
```

`AppUIBridge` is your [UI bridge](ui-bridge.md). See [Backends](backends.md) for the window setup of each engine.

| Registry | Type | Bridge | Registered by |
|---|---|---|---|
| `BridgeHandler.UI` | `UIBridgeRegistry` | [`IUIBridge`](#iuibridge) | you |
| `BridgeHandler.WINDOW` | `BridgeRegistry<IWindowBridge>` | [`IWindowBridge`](#iwindowbridge) | the backend |
| `BridgeHandler.RENDER` | `BridgeRegistry<IRenderBridge>` | [`IRenderBridge`](#irenderbridge) | the backend |
| `BridgeHandler.AUDIO` | `BridgeRegistry<IAudioBridge>` | [`IAudioBridge`](#iaudiobridge-and-iaudiosource) | the backend |
| `BridgeHandler.CLOCK` | `BridgeRegistry<IClockBridge>` | [`IClockBridge`](#iclockbridge) | JOID, with a `SystemClockBridge` |

## BridgeRegistry

A registry keeps several bridges of the same kind and answers with the one of highest priority.

| Method | Description |
|---|---|
| `register(T bridge)` | Adds the bridge. Registering a bridge that is already in the registry moves it to the end of its priority group. |
| `unregister(T bridge)` | Removes the bridge; the next one takes over. |
| `get()` | The bridge of highest priority. Throws an `IllegalStateException` when the registry is empty, for example `No render bridge registered, call BridgeHandler.RENDER.register before using JOID`. |
| `getBridge(Class<B> bridgeClass)` | The bridge of highest priority that is an instance of `bridgeClass`, as an `Optional`, empty when none is. |
| `find(Predicate<T> filter)` | The bridge of highest priority that matches `filter`, as an `Optional`, empty when none matches. |
| `static create(String name)` | A new, empty registry. `name` appears in the message of `get()`. |

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
BridgeHandler.CLOCK.getBridge(ManualClockBridge.class).ifPresent(clock -> clock.advance(1000L));
```

### Priority with getIndex

Every bridge implements `IBridge`, whose `getIndex()` returns `0` by default. A registry orders its bridges by index: the highest index wins, and among equal indexes the latest registered wins. Override `getIndex()` to keep a bridge in front of the ones registered after it:

```java
@Override
public int getIndex() {
    return 10;
}
```

`UIBridgeRegistry`, the type of `BridgeHandler.UI`, adds the routing of UIs:

| Method | Description |
|---|---|
| `get(UI ui)` | The bridge of highest priority whose `canHandle(ui)` returns `true`, as an `Optional`. |
| `get(Class<? extends UI> clazz)` | The bridge of highest priority whose `canHandle(clazz)` returns `true`, as an `Optional`. |

`JOID.open`, `JOID.close`, `JOID.isOpen` and `JOID.getUI` use them to find the bridge of a UI. See [Several UI bridges](ui-bridge.md#several-ui-bridges).

## IUIBridge

The UI bridge hosts the UIs: it opens and closes them, dispatches input to them and draws them. Extend `UIBridge`, which implements the dispatching and the drawing, and see [UI Bridge](ui-bridge.md) for the full contract.

| Method | Role |
|---|---|
| `open(UI)` / `close(UI)` | Called by `JOID.open` and `JOID.close`. |
| `add(UI)` / `remove(UI)` | Put a UI in the list, or take it out. |
| `isOnTop(UI)` / `isOpened(UI)` | Whether the UI receives hover, whether it is in the list. |
| `canHandle(UI)` / `canHandle(Class<? extends UI>)` | Routing between several UI bridges. |
| `getInstance()` / `getUiList()` | The bridge and its ordered list of UIs. |
| `getInterfaceScale(UI)` | Scale factor of the UI, `1` by default. |
| `drawHover(UI, List<String>, double, double)` | Draws a text tooltip. |

## IWindowBridge

The window bridge answers questions about the window. Every coordinate is in window pixels, with the origin at the top-left corner.

| Method | Description |
|---|---|
| `getWidth()` / `getHeight()` | Size of the drawable area in pixels. UIs are laid out on it. |
| `getMouseX()` / `getMouseY()` | Mouse position in pixels, in the same space as the size. Read at every frame: JOID needs no mouse-move event. |
| `isMouseGrabbed()` | `true` while the host captures the cursor (for example a first-person camera). A node being dragged stops its drag. |
| `isKeyDown(Key key)` | Whether a key is held. `Key.isDown()` and the modifier helpers of `UI` call it. |
| `getClipboard()` / `setClipboard(String text)` | Text clipboard, used by text fields. `getClipboard()` returns `""` when it holds no text. |

## IRenderBridge

The render bridge draws: matrix stacks, render state, textures, framebuffers, shaders and draw calls. JOID's drawing code and the [shader pipeline](../shaders/pipeline.md) call it; you call it in a [draw hook](../drawing/draw-utils.md) only for low-level work. Its contract is described in [Writing a Backend](writing-a-backend.md#the-render-contract).

| Group | Methods |
|---|---|
| Model-view matrix | `pushMatrix()`, `popMatrix()`, `loadIdentity()`, `translate(x, y, z)`, `scale(x, y, z)`, `rotate(angle, x, y, z)`, `quantize(motionX, motionY)` |
| Projection | `pushProjection()`, `popProjection()`, `ortho(left, right, bottom, top, near, far)` |
| State stack | `pushState()`, `popState()` |
| State | `color(r, g, b, a)`, `blend(BlendState)`, `depth(test, write)`, `cull(boolean)`, `lighting(boolean)`, `colorMask(boolean)`, `alphaTest(threshold)`, `lineWidth(width)`, `lineSmooth(boolean)`, `getLineWidth()`, `isLineSmooth()` |
| Stencil | `stencilTest(boolean)`, `stencilFunction(StencilFunction, reference, mask)`, `stencilOperation(fail, depthFail, pass)`, `clearStencil()` |
| Target | `viewport(x, y, width, height)`, `getViewportWidth()`, `getViewportHeight()`, `getPixelGrid()`, `clear(r, g, b, a)`, `frameBuffer(IFrameBuffer)` |
| Textures and shaders | `texture(ITexture, TextureFilter, TextureWrap)`, `resetTexture()`, `shader(IShader)`, `getShader()` |
| Drawing | `draw(DrawMode, VertexBuffer)` |
| Factories | `createTexture()`, `createFrameBuffer(width, height, TextureFilter)`, `createShader(ShaderSource vertex, ShaderSource fragment, BlendState)` |

`RenderBridge` (`dev.joid.lib.bridge.render`) is an abstract base that keeps the matrices and the state in Java for engines without a fixed pipeline.

## IAudioBridge and IAudioSource

The audio bridge creates streaming sources, used by the [audio track of videos](../resources/playback.md#audio).

| Method of `IAudioBridge` | Description |
|---|---|
| `createSource(int sampleRate, int channels)` | A new source playing interleaved signed 16-bit samples at `sampleRate` Hz. |

| Method of `IAudioSource` | Description |
|---|---|
| `queue(short[] samples)` | Appends a buffer of interleaved samples to the playback queue. |
| `play()` / `pause()` / `stop()` | Starts or resumes, pauses, stops the playback. |
| `clear()` | Stops the source and removes every queued buffer. |
| `gain(float gain)` | Linear volume, `0` for silence. |
| `isPlaying()` | Whether the source is playing. |
| `getQueuedBuffers()` / `getProcessedBuffers()` | Buffers queued, and buffers already played that can be reused. |
| `delete()` | Releases the source. |

## IClockBridge

The clock bridge gives JOID its time. Everything that moves reads it:

- tween animators, `Color.RAINBOW()` and `Color.LOADING()`;
- animated images, videos and the size changes of SVG rasters;
- scheduled tasks of a UI, the frame time and the FPS counter;
- `Node.wait`, the last click, key and update times of nodes, key repeat and the cursor blink of text fields.

| Method | Description |
|---|---|
| `nanoTime()` | Monotonic time in nanoseconds. |
| `currentTimeMillis()` | Time in milliseconds. |

`SystemClockBridge` is registered when `BridgeHandler` loads and reads `System.nanoTime()` and `System.currentTimeMillis()`.

### Controlling time with ManualClockBridge

`ManualClockBridge` (`dev.joid.lib.bridge.clock`) only moves when you move it, which makes tests and captures deterministic:

```java
final ManualClockBridge clock = ManualClockBridge.create(0L);
BridgeHandler.CLOCK.register(clock);

clock.advance(16L);
uiBridge.update();
uiBridge.draw();

BridgeHandler.CLOCK.unregister(clock);
```

| Method | Description |
|---|---|
| `static create(long time)` | A clock stopped at `time` milliseconds. |
| `advance(long milliseconds)` | Moves the clock forward. |
| `setTime(long time)` | Sets the time in milliseconds. |
| `currentTimeMillis()` | The time in milliseconds. |
| `nanoTime()` | The time multiplied by 1,000,000. |

Unregistering it gives the time back to the `SystemClockBridge`. The [testkit](testkit.md) drives its snapshots with a `ManualClockBridge`.

## See also

- [UI Bridge](ui-bridge.md) — implementing the UI bridge and feeding input.
- [Backends](backends.md) — the bridges of LWJGL 2, LWJGL 3 and Vulkan.
- [Writing a Backend](writing-a-backend.md) — implementing the render, window and audio bridges.
- [Quick Start](../getting-started/quick-start.md) — a complete first application.