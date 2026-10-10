# Bridges and Backends

JOID never calls a windowing, graphics or audio API itself: it goes through small interfaces called bridges. A backend implements the window, render and audio bridges for one engine, and you register it once before loading JOID. Your UI code never mentions it, so switching backends changes only your startup code.

```java
public final class App {

	public static void main(final String[] args) {
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GlContextRequest.CORE_33.apply();
		final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();

		Backend.register(window);
		final AppUIBridge bridge = new AppUIBridge();
		BridgeHandler.UI.register(bridge);
		JOID.inst().load();

		GlfwInputForwarder.create(bridge).attach(window);
		GLFW.glfwSetFramebufferSizeCallback(window, (handle, width, height) -> {
			if (width > 0 && height > 0) {
				bridge.resize(width, height);
			}
		});
		bridge.resize(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());

		JOID.open(new UIMainMenu());
		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
			bridge.frame();
			GLFW.glfwSwapBuffers(window);
		}

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

}
```

`Backend` is `dev.joid.backend.lwjgl3.Backend`, `AppUIBridge` is the bridge of [Embedding JOID in an Application](ui-bridge.md), and `UIMainMenu` is one of your UIs.

![LWJGL 2 and LWJGL 3 share the OpenGL renderer, LWJGL 3 and Vulkan share the GLFW window bridge, all three share the OpenAL audio bridge](../images/diagram-backends.png "Three backends over shared modules")

## Pick a backend

JOID ships three backends. They render the same pixels, and any UI runs unchanged on each.

| Backend | Renderer | Window and input | Register with |
|---|---|---|---|
| LWJGL 3 | OpenGL 2.0 to 4.6, compatibility or core | GLFW | `dev.joid.backend.lwjgl3.Backend.register(window)` |
| Vulkan | Vulkan 1.3 | GLFW | `dev.joid.backend.vulkan.Backend.register(window)` |
| LWJGL 2 | OpenGL 2.0 to 4.6, compatibility or core | `Display`, `Mouse`, `Keyboard` | `dev.joid.backend.lwjgl2.Backend.register()` |

Each `Backend.register` checks the JOID version, then registers the audio, window and render bridges. The jars and their dependencies are listed in [Installation](../getting-started/installation.md).

## The registries of BridgeHandler

`BridgeHandler` (`dev.joid.lib.bridge`) holds one registry per kind of bridge. `register(bridge)` adds a bridge and `get()` returns the one in front: the highest `getIndex()` (`0` by default), then the latest registered.

| Registry | Bridge | Registered by |
|---|---|---|
| `BridgeHandler.UI` | `IUIBridge`: open UIs, input, frames | you |
| `BridgeHandler.WINDOW` | `IWindowBridge`: size and mouse in pixels, keys, clipboard, cursor | the backend |
| `BridgeHandler.RENDER` | `IRenderBridge`: matrices, render state, textures, shaders, draws | the backend |
| `BridgeHandler.AUDIO` | `IAudioBridge`: audio sources for video sound | the backend |
| `BridgeHandler.CLOCK` | `IClockBridge`: the time of animations and media | JOID (`SystemClockBridge`) |
| `BridgeHandler.THREAD` | `IThreadBridge`: the render thread | JOID (`DirectThreadBridge`) |
| `BridgeHandler.SIGNAL_REPLAY` | `ISignalReplayRemapper`: renamed classes for reactive setters | JOID (identity) |

![Your UIs and the JOID core call the registries of BridgeHandler, filled by your UI bridge, the backend and JOID](../images/diagram-bridges.png "Everything JOID needs from the outside goes through a registry")

`getBridge(Class)` and `find(Predicate)` return a specific bridge, or `null`.

## LWJGL 3

Create the window with an OpenGL context, make it current and call `GL.createCapabilities()` before `Backend.register(window)`, as in the first example. `GlContextRequest` (`dev.joid.backend.lwjgl3`) sets the window hints: `CORE_33`, `CORE_32_FORWARD_COMPATIBLE` or `COMPATIBILITY`, each with a 24-bit depth and an 8-bit stencil buffer. On macOS, start the JVM with `-XstartOnFirstThread`.

## Vulkan

The Vulkan backend owns its device and swapchain. Raise the LWJGL stack size first, create the window without a client API, and show each frame with `present()`:

```java
Configuration.STACK_SIZE.set(1024);
if (!GLFW.glfwInit()) {
	throw new IllegalStateException("Unable to initialize GLFW");
}

GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
Backend.register(window);
final VulkanRenderBridge render = (VulkanRenderBridge) BridgeHandler.RENDER.get();

while (!GLFW.glfwWindowShouldClose(window)) {
	GLFW.glfwPollEvents();
	bridge.frame();
	render.present();
}
```

The UI bridge, the input and the resize are set up as with LWJGL 3. Every clear and draw happens inside the frame, between `beginFrame()` and `endFrame()`, which `frame()` calls for you.

## LWJGL 2

Install the natives before creating the `Display`, ask for a stencil buffer, then register the backend. `Lwjgl2InputForwarder` reads the mouse and keyboard queues once per frame:

```java
Natives.install();
Display.setDisplayMode(new DisplayMode(1920, 1080));
Display.setResizable(true);
Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
Backend.register();

final AppUIBridge bridge = new AppUIBridge();
BridgeHandler.UI.register(bridge);
JOID.inst().load();

final Lwjgl2InputForwarder input = Lwjgl2InputForwarder.create(bridge);
bridge.resize(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
while (!Display.isCloseRequested()) {
	input.poll();
	bridge.frame();
	Display.update();
	if (Display.wasResized()) {
		bridge.resize(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
	}
}
Display.destroy();
```

Size the screen from the window bridge, not from `Display.getWidth()`: the window bridge counts framebuffer pixels on high-density screens.

## Embedding JOID in a host

When a host such as a game owns the window and the loop, draw JOID after the host drew its frame, from the thread of the graphics context. Wrap the JOID frame in `beginFrame()` and `endFrame()`: the OpenGL backends then give the host its OpenGL state back.

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
render.beginFrame();
try {
	render.screen(width, height);
	this.bridge.draw();
} finally {
	render.endFrame();
}
```

![The host draws its frame, then beginFrame, screen, the UI draw and endFrame in a finally block](../images/diagram-backend-host.png "The host finds its state back after the JOID frame")

When the host draws inside a JOID frame, such as an item of a game shown in a UI, wrap that drawing in `suspend(...)`: the host gets its own state during the call.

```java
BridgeHandler.RENDER.get().suspend(() -> this.items.drawItem(stack, x, y));
```

A host that draws its interface with fixed-function matrices loads them into JOID with `FixedMatrixImport.create(bridge).apply()` (`dev.joid.base.opengl.render.state`) instead of `screen(width, height)`.

## Registering bridges one by one

`Backend.register` is a shortcut. With another windowing system, register your own window bridge next to the render and audio bridges of the shared modules:

```java
BridgeHandler.AUDIO.register(AlAudioBridge.create(Lwjgl3AlBinding.inst()));
BridgeHandler.WINDOW.register(new HostWindowBridge());
BridgeHandler.RENDER.register(GlRenderBridge.create(Lwjgl3GlBinding.inst()));
```

## A render thread with IThreadBridge

When your engine renders on a thread other than the one that opens UIs, register an `IThreadBridge`. `JOID.open`, `JOID.close` and every callback JOID hands to your code then run on the render thread:

```java
public final class GameThreadBridge implements IThreadBridge {

	@Override
	public boolean isRenderThread() {
		return Game.inst().isRenderThread();
	}

	@Override
	public void execute(final @NonNull Runnable runnable) {
		Game.inst().runOnRenderThread(runnable);
	}

}
```

```java
BridgeHandler.THREAD.register(new GameThreadBridge());
```

## Reference

| Method | Description |
|---|---|
| `Backend.register(...)` | Check the version, register the audio, window and render bridges. LWJGL 3 also takes `(long window, IGlBinding binding)`. |
| `Natives.install()` | Install the LWJGL 2 natives, before `Display.create()`. |
| `VulkanRenderBridge.present()` | Show the last frame on the window. |
| `BridgeRegistry.register(T)` / `unregister(T)` | Add a bridge at its sorted position, or remove it. |
| `BridgeRegistry.get()` | The bridge in front; throws an `IllegalStateException` when the registry is empty. |
| `BridgeRegistry.getBridge(Class)` / `find(Predicate)` | The first matching bridge, or `null`. |
| `IRenderBridge.beginFrame()` / `endFrame()` | Wrap a JOID frame drawn inside a host loop. |
| `IRenderBridge.screen(int width, int height)` | Draw to the window, with a viewport and a projection in pixels. |
| `IRenderBridge.suspend(Runnable)` | Run a host drawing inside a JOID frame. |
| `ManualClockBridge.create(long)` / `advance(long)` | A clock that moves only when you advance it, for tests. |

## Good to know

- Register the backend once the graphics context exists: the render bridge creates GPU objects as soon as it is built.
- Without an 8-bit stencil buffer, the masks of your UIs do not clip.
- The demo windows of the backends are only in the `-dev` jars: never reference them from application code.

## See also

- [Embedding JOID in an Application](ui-bridge.md)
- [Writing a Backend](writing-a-backend.md)
- [Testkit](testkit.md)
- [Installation](../getting-started/installation.md)
- [Frame Loop and Dev Tools](../concepts/frame-loop.md)