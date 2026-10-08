# Backends

[Bridges and Backends](../concepts/bridges.md) listed the official backends and the [Quick Start](../getting-started/quick-start.md) registered the LWJGL 3 one. This page covers each of them in detail. A backend implements the window, render and audio [bridges](bridges.md) for one engine. JOID ships three: LWJGL 2, LWJGL 3 and Vulkan. Pick the one that matches your host, register it before loading JOID, and register your own [UI bridge](ui-bridge.md) next to it.

## A first window with LWJGL 3

Create a GLFW window with an OpenGL 3.3 core context and an 8-bit stencil buffer, make its context current, then register the backend with the window handle:

```java
public final class App {

	public static void main(final String[] args) {
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, Platform.get() == Platform.MACOSX ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, 24);
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);

		final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();

		Backend.register(window);
		final AppUIBridge bridge = new AppUIBridge();
		BridgeHandler.UI.register(bridge);
		JOID.inst().load();

		final AppLoop loop = new AppLoop(window, bridge);
		JOID.open(new UIMainMenu());
		loop.run();

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

}
```

`Backend` is `dev.joid.backend.lwjgl3.Backend`. `AppUIBridge` and `AppLoop` are the bridge and the event loop of [UI Bridge](ui-bridge.md); the loop ends each frame with `GLFW.glfwSwapBuffers(window)`.

![Diagram of the backends: LWJGL 2 has its own window, render and audio bridges; LWJGL 3 renders with the GlRenderBridge of joid-base-opengl and Vulkan with its own render bridge, and both share the window bridge of joid-base-glfw and the audio bridge of joid-base-openal; all build on joid-core](../images/diagram-backends.png "LWJGL 3 and Vulkan share the GLFW window bridge and the OpenAL audio bridge")

The backend is the only part of a JOID application that knows the engine. Your UIs run unchanged on all three, and the snapshot tests compare the backends pixel by pixel so that they also look the same.

| Module | Engine | Register with | Window bridge | Audio bridge | Generated shaders |
|---|---|---|---|---|---|
| `backend-lwjgl2` | LWJGL 2.9.1: OpenGL state of the current context | `dev.joid.backend.lwjgl2.Backend.register()` | LWJGL 2 `Display`, `Mouse`, `Keyboard` | LWJGL 2 OpenAL | GLSL 1.20 |
| `backend-lwjgl3` | LWJGL 3.3.4: OpenGL 3.3 core, rendered by the `base-opengl` module | `dev.joid.backend.lwjgl3.Backend.register(window)` | `base-glfw` module | `base-openal` module | GLSL 3.30 |
| `backend-vulkan` | LWJGL 3.3.4: Vulkan 1.3, shaderc | `dev.joid.backend.vulkan.Backend.register(window)` | `base-glfw` module | `base-openal` module | GLSL 4.50 compiled to SPIR-V at runtime |

Each `Backend.register` registers the audio, window and render bridges of its module; the clock bridge is already registered by JOID. The backend jars, their `prod` and `dev` flavors and the dependencies to declare are listed in [Installation](../getting-started/installation.md).

## LWJGL 3

- The render bridge creates its OpenGL objects in its constructor: the context must be current, with its capabilities created, before `Backend.register`. Every drawing call must then happen on that thread.
- The stencil buffer is needed by the masks of a UI (see [The UI Class](../ui/ui-class.md)).
- The jar does not contain LWJGL: your application declares `lwjgl`, `lwjgl-glfw`, `lwjgl-openal` and `lwjgl-opengl` 3.3.4, with the natives of each for its platforms.
- On macOS, start the JVM with `-XstartOnFirstThread`, as GLFW requires.

## Vulkan

The Vulkan backend owns its instance, device and swapchain, created on a GLFW window without client API. Raise the LWJGL stack size before any LWJGL call, then draw each frame between `beginFrame()` and `endFrame()`:

```java
public final class App {

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);

		Backend.register(window);
		final AppUIBridge bridge = new AppUIBridge();
		BridgeHandler.UI.register(bridge);
		JOID.inst().load();

		final RenderBridge render = (RenderBridge) BridgeHandler.RENDER.get();
		final IWindowBridge windowBridge = BridgeHandler.WINDOW.get();
		render.screen(windowBridge.getWidth(), windowBridge.getHeight());
		JOID.open(new UIMainMenu());

		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
			bridge.update();

			render.beginFrame();
			render.clear(0F, 0F, 0F, 1F);
			bridge.draw();
			render.endFrame();
			render.present();
		}

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

}
```

`Backend` is `dev.joid.backend.vulkan.Backend` and `RenderBridge` is `dev.joid.backend.vulkan.render.RenderBridge`. Register the input callbacks as in [UI Bridge](ui-bridge.md#driving-the-bridge-from-your-loop); when the window is resized, call `render.screen(width, height)` with the framebuffer size again and `bridge.load()`.

| Method of `dev.joid.backend.vulkan.render.RenderBridge` | Description |
|---|---|
| `beginFrame()` | Of `IRenderBridge`. Acquires the next swapchain image and starts recording. Recreates the swapchain first when the window size changed. Throws `IllegalStateException("The Vulkan frame has already begun")` when a frame is already open. |
| `endFrame()` | Of `IRenderBridge`. Submits the frame and waits for the GPU to finish it. |
| `present()` | Shows the image on the window. |

- Every `draw()` of your UI bridge, and every clear, happens between `beginFrame()` and `endFrame()`; outside, the bridge throws `Vulkan rendering must happen between beginFrame and endFrame`. `update()` and the input methods can run outside the frame.
- A Vulkan 1.3 device able to present to the window is required; without one, `Backend.register` throws `No Vulkan 1.3 device able to present to the window was found`.
- Wide and smooth lines are enabled when the device supports them.
- The swapchain presents without waiting for the vertical blank when the driver allows it (immediate mode, then mailbox, then FIFO).
- The jar does not contain LWJGL: your application declares `lwjgl`, `lwjgl-glfw`, `lwjgl-openal`, `lwjgl-vulkan` and `lwjgl-shaderc` 3.3.4, with the natives of `lwjgl`, `lwjgl-glfw`, `lwjgl-openal` and `lwjgl-shaderc`, plus those of `lwjgl-vulkan` on macOS (MoltenVK).
- On macOS, start the JVM with `-XstartOnFirstThread`.

## LWJGL 2

Register the backend before creating the `Display`, so that its natives are in place when LWJGL loads them. Ask for a stencil buffer:

```java
public final class App {

	public static void main(final String[] args) throws LWJGLException {
		Backend.register();
		Display.setDisplayMode(new DisplayMode(1920, 1080));
		Display.setResizable(true);
		Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));

		final AppUIBridge bridge = new AppUIBridge();
		BridgeHandler.UI.register(bridge);
		JOID.inst().load();

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.screen(Display.getWidth(), Display.getHeight());
		JOID.open(new UIMainMenu());

		final AppInput input = new AppInput(bridge);
		while (!Display.isCloseRequested()) {
			input.poll();
			bridge.update();
			render.clear(0F, 0F, 0F, 1F);
			bridge.draw();
			Display.update();

			if (Display.wasResized()) {
				render.screen(Display.getWidth(), Display.getHeight());
				bridge.load();
			}
		}

		Display.destroy();
	}

}
```

LWJGL 2 delivers the character and the key of a press in the same event, so the input loop is short. `WindowBridge` is `dev.joid.backend.lwjgl2.window.WindowBridge`, whose static `getKey(int)` converts an LWJGL 2 key code:

```java
@RequiredArgsConstructor
public final class AppInput {

	@NonNull private final AppUIBridge bridge;

	public void poll() {
		while (Mouse.next()) {
			final int button = Mouse.getEventButton();
			if (button != -1 && Mouse.getEventButtonState()) {
				this.bridge.mousePressed(ClickType.from(button));
			} else if (button != -1) {
				this.bridge.mouseReleased(ClickType.from(button));
			} else {
				this.bridge.mouseMoved();
			}

			if (Mouse.getEventDWheel() != 0) {
				this.bridge.mouseScroll(Mouse.getEventDWheel() / 120D);
			}
		}

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				this.bridge.keyTyped(Keyboard.getEventCharacter(), WindowBridge.getKey(Keyboard.getEventKey()));
			}
		}
	}

}
```

- The render bridge maps every call onto the OpenGL state of the current context: matrices go to the OpenGL matrix stacks, and `pushState()` / `popState()` save and restore the OpenGL state itself. It needs OpenGL 2.0 shaders and 3.0 framebuffer objects.
- A lit draw without a bound shader goes through the `fixed` shader of the core, so 3D models are shaded exactly as on LWJGL 3 and Vulkan.
- The window bridge reads LWJGL 2's `Display`, `Mouse` and `Keyboard`, and the clipboard through AWT. LWJGL 2 key codes follow the keyboard layout on Windows and Linux and the place of the key on macOS; `isPhysicalKeyDown` answers like `isKeyDown`.
- The audio bridge uses LWJGL 2's OpenAL. It creates the OpenAL context on the first video with sound, unless one already exists, and destroys it when the JVM exits.

### LWJGL 2 natives

The jar does not contain the LWJGL 2.9.1 classes, which your application declares, but contains the LWJGL and OpenAL natives for Windows, Linux and macOS. `Backend.register()` installs them once:

1. When the system property `org.lwjgl.librarypath` is set, or when one of the native libraries of the platform is found in a folder of `java.library.path`, nothing is extracted: the host provides them.
2. Otherwise the natives are extracted into `<java.io.tmpdir>/joid-lwjgl-2.9.1/<platform>` (`windows`, `linux` or `osx`), rewritten only when their size differs, and `org.lwjgl.librarypath` points to that folder.

`dev.joid.backend.lwjgl2.Natives.install()` performs this step alone; it runs once per JVM.

## Version check with JOID.checkVersion

Each official `Backend.register` first calls `JOID.checkVersion(JOID.VERSION)`. `JOID.VERSION` is a compile-time constant, so the backend keeps the version of JOID it was built against. When the core loaded at runtime has another major version, it prints:

```text
[JOID] This backend targets JOID 8.0.0 but JOID 9.0.0 is loaded
```

The backend still registers: the message tells you to align the jar versions. A backend of your own makes the same call (see [Writing a Backend](writing-a-backend.md)).

## The base-glfw and base-openal modules

The LWJGL 3 and Vulkan backends share two modules, also published as their own jars, `joid-base-glfw` and `joid-base-openal`, for engines built on GLFW or OpenAL.

### GLFW window bridge

`dev.joid.base.glfw.WindowBridge` implements `IWindowBridge` for a GLFW window:

| Member | Description |
|---|---|
| `new WindowBridge(long window)` | A bridge reading the given window. |
| `getWidth()` / `getHeight()` | The framebuffer size in pixels. |
| `getMouseX()` / `getMouseY()` | The cursor position converted into framebuffer pixels with `GlfwWindows.toFramebuffer(position, windowSize, framebufferSize)`, so the mouse matches the drawing on high-density screens. |
| `isMouseGrabbed()` | `true` when the cursor mode is `GLFW_CURSOR_DISABLED`. |
| `isKeyDown(Key)` | `glfwGetKey` of the GLFW keys that give `key` on the active keyboard layout. |
| `isPhysicalKeyDown(Key)` | `glfwGetKey` of the GLFW key at the place of `key`. |
| `getClipboard()` / `setClipboard(String)` | The GLFW clipboard. |

`dev.joid.base.glfw.input.GlfwKeys` holds the table between GLFW key codes and `Key`, for every engine on GLFW:

| Member | Description |
|---|---|
| `static getKey(int code)` | The `Key` of a GLFW key code on the active keyboard layout: a letter or punctuation key becomes the key of the character it types (`glfwGetKeyName`); `Key.UNKNOWN` when it has none. |
| `static getPhysicalKey(int code)` | The `Key` at the place of a GLFW key code, whatever the layout. |
| `static getCode(Key key)` | The GLFW key code at the place of `key`; `GLFW_KEY_UNKNOWN` when it has none. |
| `static isKeyDown(Key key, IntPredicate down)` | Whether `key` is held on the active keyboard layout, `down` telling whether a GLFW key code is held (`glfwGetKey`, or the input state of the host). |
| `static isPhysicalKeyDown(Key key, IntPredicate down)` | Whether the GLFW key at the place of `key` is held. |

`dev.joid.base.glfw.input.GlfwInputForwarder` forwards the GLFW events to a `UIBridge`: `create(bridge)`, then `attach(window)` to set the callbacks of a window, or its event methods when the host owns the callbacks (see [UI Bridge](ui-bridge.md#driving-the-bridge-from-your-loop)). `dev.joid.base.glfw.GlfwWindows.toFramebuffer(position, windowSize, framebufferSize)` converts a cursor position from window coordinates to framebuffer pixels, `position` unchanged for a window of size 0.

`dev.joid.base.glfw.snapshot.GlfwSnapshotWindow.create(width, height, hints)` initializes GLFW and creates the hidden, fixed-size window of a snapshot backend, after the window hints of `hints` (the context of your API); `getWindow()` gives its handle and `destroy()` destroys it and terminates GLFW. The LWJGL 3 and Vulkan snapshot backends use it; like the demo window, it is left out of the `-prod` jars and of the released `joid-base-glfw` jar.

### OpenAL audio bridge

`dev.joid.base.openal.AudioBridge` implements `IAudioBridge` with LWJGL 3's OpenAL:

- `createSource` uses the OpenAL context that is current, so a host that already plays sound shares its context; when none is current, it opens the default device, creates a context, makes it current and destroys it when the JVM exits.
- `AudioSource` streams 16-bit samples through a queue of OpenAL buffers, in mono for one channel and in stereo otherwise: a track of 3 channels or more is mixed down with [`AudioDownmix`](bridges.md#stereo-output-with-audiodownmix) first. The source is placed at the listener, so OpenAL does not spatialize it; JOID applies the distance attenuation itself (see [Playback, Video and Audio](../resources/playback.md)).

The LWJGL 2 audio bridge mixes down the same way.

## Embedding JOID in an existing host

JOID can draw inside an application that already owns the window, the graphics context and the main loop, such as a game.

### Thread and projection

Call `update()`, `draw()` and the input methods of your UI bridge from the thread that owns the graphics context, after the host drew its own frame. Each UI draws in its own projection, but the target, the viewport and the base projection come from you: call `screen(width, height)` of the render bridge before drawing (it draws to the window, with a viewport covering it and `ortho(0, width, height, 0, 0, 10000)`), and `load()` on your UI bridge when the host window is resized.

### Giving the host its state back

The LWJGL 2 bridge works on the live OpenGL state. Wrap the JOID frame to restore what it changes:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
render.pushState();
render.pushProjection();
render.pushMatrix();
try {
	render.screen(width, height);
	uiBridge.draw();
} finally {
	render.popMatrix();
	render.popProjection();
	render.popState();
}
```

![Diagram of an embedded frame: the host draws its frame, JOID pushes the state, projection and matrix, sets ortho and viewport, draws the UIs, then pops everything in a finally block](../images/diagram-backend-host.png "Push before the JOID frame, pop in a finally block: the host finds its state back")

The LWJGL 3 and Vulkan bridges keep their state in Java and apply it at each draw call. On OpenGL, the LWJGL 3 bridge binds its own vertex array, buffer, program, framebuffer, viewport, blending, depth, culling, stencil, line width, and a texture with a sampler object on texture unit 0, and leaves them bound: restore what your renderer needs after the JOID frame, including `glBindSampler(0, 0)` when your renderer relies on texture parameters.

### Natives, audio and interface size

- The LWJGL 2 backend extracts nothing when the host already provides its natives.
- Both audio bridges reuse an OpenAL context that already exists.
- Follow the interface size of the host with [`getInterfaceScale`](ui-bridge.md#interface-scale-with-getinterfacescale).

### Registering bridges one by one

`Backend.register` is a shortcut. With another windowing system, register your own `IWindowBridge` next to the render and audio bridges of a module:

```java
BridgeHandler.AUDIO.register(new AudioBridge());
BridgeHandler.WINDOW.register(new HostWindowBridge());
BridgeHandler.RENDER.register(GlRenderBridge.create(Lwjgl3GlBinding.inst()));
```

Here `AudioBridge` is `dev.joid.base.openal.AudioBridge`, `GlRenderBridge` is `dev.joid.base.opengl.render.GlRenderBridge`, `Lwjgl3GlBinding` is `dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding` and `HostWindowBridge` is your implementation of `IWindowBridge`. The `RenderBridge`, `WindowBridge` and `AudioBridge` of `dev.joid.backend.lwjgl2` also have public constructors; the Vulkan render bridge needs a GLFW window (`new RenderBridge(long window)`).

## The base-opengl module

The LWJGL 3 backend renders with `joid-base-opengl`, a module in plain Java that holds the whole OpenGL renderer and calls OpenGL only through binding interfaces. The LWJGL 3 backend implements them with LWJGL 3 (`dev.joid.backend.lwjgl3.binding`); another engine on OpenGL implements them with its own functions and gets the same rendering.

| Package `dev.joid.base.opengl` | Content |
|---|---|
| `binding` | `IGlBinding` (`glEnable`, `glDisable`, `glIsEnabled`, `glGetInteger`, `glGetString`, `glGetStringi`, `glGetFloatv`, and the getters of the five domain bindings), `IGlStateBinding` (blending, depth, stencil, color mask, viewport, line width, clear color), `IGlBufferBinding` (buffers, vertex arrays, attributes, `glDrawArrays`), `IGlProgramBinding` (shaders, programs, uniforms, uniform blocks), `IGlTextureBinding` (textures, units, sampler objects), `IGlFrameBufferBinding` (framebuffers, renderbuffers, blits, clears, reading pixels), and `GlConstants`, the OpenGL values the module passes to them. |
| `capability` | `GlCapabilities.read(IGlBinding)` reads the context once: version, GLSL version, `GlProfile` (`COMPATIBILITY`, `CORE`, `FORWARD_COMPATIBLE_CORE`), extensions, maximum texture size and line widths, and tells whether vertex arrays, uniform buffers, sampler objects and framebuffer objects are there. `GlStrategies.of(GlCapabilities)` is the one place that chooses how to render on that context: today GLSL 3.30 with the uniforms in a block, and it refuses a context without OpenGL 3.3 with an `IllegalStateException` naming what the context offers. |
| `render` | `GlRenderBridge` (`create(IGlBinding)`), the render bridge; `GlEnums`, the OpenGL values of the blend, stencil, wrap, filter, vertex and primitive enums of JOID; `shader.GlShader`, `texture.GlTexture` and `framebuffer.GlFrameBuffer`, on the core `Shader`, `Texture` and `FrameBufferHandle`. |
| `snapshot` | `GlSnapshotCapture.capture(binding, width, height)` and `getRenderer(binding)`, for an `ISnapshotBackend` on OpenGL; left out of the `-prod` jars and of the released `joid-base-opengl` jar. |

`GlRenderBridge.create(binding)` reads the capabilities and creates its vertex array, buffer and sampler objects: the context must be current. Every OpenGL call of JOID then goes through the bindings, so a binding that wraps another one sees all of them. On LWJGL 3, `GlContextRequest.CORE_33.apply()` sets the GLFW hints of the context JOID needs (OpenGL 3.3 core, forward compatible on macOS, 24 bits of depth, 8 of stencil).

## Demo windows

Each backend module has a demo window that opens the JOID demo UIs in dev and demo modes. The demo windows are only in the `-dev` jars: the `-prod` jars leave out every `demo` and `snapshot` package.

| Command | Main class |
|---|---|
| `./gradlew :backend-lwjgl2:runDemo` | `dev.joid.backend.lwjgl2.demo.DemoWindow` |
| `./gradlew :backend-lwjgl3:runDemo` | `dev.joid.backend.lwjgl3.demo.DemoWindow` |
| `./gradlew :backend-vulkan:runDemo` | `dev.joid.backend.vulkan.demo.DemoWindow` |

The LWJGL 3 and Vulkan demo windows extend `dev.joid.base.glfw.demo.DemoWindow`, an abstract GLFW loop that is part of the `-dev` jars of LWJGL 3 and Vulkan, not of the published `joid-base-glfw` jar. Its subclasses provide `getEngineName()`, `configureWindow()` (window hints), `registerBackend(long window)` and `present()`; the loop calls `beginFrame()` and `endFrame()` of the render bridge around each frame. Its input handling, which merges the GLFW key and character callbacks, is the one of `AppLoop` in [UI Bridge](ui-bridge.md). See [Developer Tools](../concepts/dev-tools.md) for the demo UIs.

## Reference

| Class | Member | Description |
|---|---|---|
| `dev.joid.backend.lwjgl2.Backend` | `static register()` | Checks the version, installs the natives, registers the audio, window and render bridges of LWJGL 2. |
| `dev.joid.backend.lwjgl3.Backend` | `static register(long window)` | Checks the version, registers the OpenAL audio bridge, the OpenGL 3.3 render bridge and the GLFW window bridge of `window`. |
| `dev.joid.backend.vulkan.Backend` | `static register(long window)` | Checks the version, registers the OpenAL audio bridge, the GLFW window bridge and a Vulkan render bridge on `window`. |
| `dev.joid.backend.lwjgl2.Natives` | `static install()` | Installs the LWJGL 2 natives once per JVM. |
| `dev.joid.internal.JOID` | `static checkVersion(String version)` | `true` when the major version of `version` matches the loaded JOID; otherwise prints the warning and returns `false`. |

## Pitfalls

- Create the OpenGL context, make it current and call `GL.createCapabilities()` before `dev.joid.backend.lwjgl3.Backend.register`.
- On Vulkan, a clear or a `draw()` outside `beginFrame()` / `endFrame()` throws.
- Without a stencil buffer, the masks of the UIs do not clip.
- The demo windows and `DemoUIBridge` are not in the `-prod` jars: never reference them from application code.

## See also

- Next: [Writing a Backend](writing-a-backend.md)
- [Bridges](bridges.md)
- [UI Bridge](ui-bridge.md)
- [Installation](../getting-started/installation.md)
- [Testkit](testkit.md)