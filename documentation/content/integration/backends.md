# Backends

[Bridges and Backends](../concepts/bridges.md) listed the official backends and the [Quick Start](../getting-started/quick-start.md) registered the LWJGL 3 one. This page covers each of them in detail. A backend implements the window, render and audio [bridges](bridges.md) for one engine. JOID ships three: LWJGL 2, LWJGL 3 and Vulkan. Pick the one that matches your host, register it before loading JOID, and register your own [UI bridge](ui-bridge.md) next to it.

## A first window with LWJGL 3

Create a GLFW window with an OpenGL context and an 8-bit stencil buffer, make its context current, then register the backend with the window handle. Any OpenGL context from 2.0 to 4.6 works, compatibility or core; this example asks for 3.3 core:

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

![Diagram of the backends: LWJGL 2 has its own window bridge on Display, Mouse and Keyboard; LWJGL 3 and Vulkan share the window bridge of joid-base-glfw; LWJGL 2 and LWJGL 3 render with the GlRenderBridge of joid-base-opengl, Vulkan with its own render bridge; the three share the AlAudioBridge of joid-base-openal; all build on joid-core](../images/diagram-backends.png "LWJGL 2 and LWJGL 3 share the OpenGL renderer, the three backends share the OpenAL audio bridge")

The backend is the only part of a JOID application that knows the engine. Your UIs run unchanged on all three, and the snapshot tests compare the backends pixel by pixel so that they also look the same.

| Module | Engine | Register with | Window bridge | Audio bridge | Generated shaders |
|---|---|---|---|---|---|
| `backend-lwjgl2` | LWJGL 2.9.1: OpenGL 2.0 to 4.6 of the current context, compatibility or core, rendered by the `base-opengl` module | `dev.joid.backend.lwjgl2.Backend.register()` | LWJGL 2 `Display`, `Mouse`, `Keyboard` | `base-openal` module | GLSL 1.10 to 3.30, chosen from the context |
| `backend-lwjgl3` | LWJGL 3.3.4: OpenGL 2.0 to 4.6, compatibility, core or forward-compatible core, rendered by the `base-opengl` module | `dev.joid.backend.lwjgl3.Backend.register(window)` | `base-glfw` module | `base-openal` module | GLSL 1.10 to 3.30, chosen from the context |
| `backend-vulkan` | LWJGL 3.3.4: Vulkan 1.3, shaderc | `dev.joid.backend.vulkan.Backend.register(window)` | `base-glfw` module | `base-openal` module | GLSL 4.50 compiled to SPIR-V at runtime |

Each `Backend.register` registers the audio, window and render bridges of its module; the clock bridge is already registered by JOID. The backend jars, their `prod` and `dev` flavors and the dependencies to declare are listed in [Installation](../getting-started/installation.md).

## LWJGL 3

- The render bridge creates its OpenGL objects in its constructor: the context must be current, with its capabilities created, before `Backend.register`. Every drawing call must then happen on that thread.
- The bridge reads the version, the profile and the extensions of the context and adapts to them on its own, see [OpenGL versions](#opengl-versions). It refuses a context below OpenGL 2.0 or without framebuffer objects with an `IllegalStateException` that names what the context offers.
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
- The swapchain presents without waiting for the vertical blank when the driver allows it (immediate mode, then mailbox, then FIFO).
- The jar does not contain LWJGL: your application declares `lwjgl`, `lwjgl-glfw`, `lwjgl-openal`, `lwjgl-vulkan` and `lwjgl-shaderc` 3.3.4, with the natives of `lwjgl`, `lwjgl-glfw`, `lwjgl-openal` and `lwjgl-shaderc`, plus those of `lwjgl-vulkan` on macOS (MoltenVK).
- On macOS, start the JVM with `-XstartOnFirstThread`.

### Images of the host on Vulkan

A host that renders with Vulkan lends its images to JOID with `Resource.of(VulkanImage.create(image, view, width, height, levels))` or a `VulkanImageSupplier` (`dev.joid.backend.vulkan.render.texture`), wrapped in a `VulkanBorrowedTexture` by the `VulkanImageResourceResolver` that `Backend.register` registers. JOID samples the image through its view and never writes, transitions or destroys it, so the image must:

- belong to the `VkDevice` of JOID: create it with `getContext().getDevice()` of the render bridge, or with `getContext().createImage(...)` and `createImageView(...)`;
- be created with `VK_IMAGE_USAGE_SAMPLED_BIT`, in a color format JOID can sample, with `levels` mip levels in its view (more than 1 to sample mipmaps);
- be in `VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL`, with its writes finished, between `beginFrame()` and `endFrame()` of every frame that draws it.

The bridge caches a descriptor set per view: before destroying a view JOID has drawn, call `releaseHandle(view)` of the render bridge, as its own textures do.

## LWJGL 2

Install the natives before creating the `Display`, so that they are in place when LWJGL loads them, and register the backend once the `Display` exists: its render bridge reads the OpenGL context. Ask for a stencil buffer:

```java
public final class App {

	public static void main(final String[] args) throws LWJGLException {
		Natives.install();
		Display.setDisplayMode(new DisplayMode(1920, 1080));
		Display.setResizable(true);
		Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
		Backend.register();

		final AppUIBridge bridge = new AppUIBridge();
		BridgeHandler.UI.register(bridge);
		JOID.inst().load();

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.screen(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
		JOID.open(new UIMainMenu());

		final AppInput input = new AppInput(bridge);
		while (!Display.isCloseRequested()) {
			input.poll();
			bridge.update();
			render.clear(0F, 0F, 0F, 1F);
			bridge.draw();
			Display.update();

			if (Display.wasResized()) {
				render.screen(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
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

- The render bridge is the `GlRenderBridge` of `base-opengl`, on LWJGL 2's OpenGL (`dev.joid.backend.lwjgl2.binding.Lwjgl2GlBinding`): the renderer of LWJGL 3, with the same choices for each context (see [OpenGL versions](#opengl-versions)) and the same pixels. It keeps the matrices and the state in Java, and gives the host its OpenGL state back (see [Giving the host its state back](#giving-the-host-its-state-back)). It uses its own vertex array as soon as the context has OpenGL 3.0 or `GL_ARB_vertex_array_object`, and calls OpenGL only through LWJGL 2, whose own checks of the bound buffers stay right.
- The window bridge reads LWJGL 2's `Display`, `Mouse` and `Keyboard`, the clipboard through AWT, and reports its size and the mouse in framebuffer pixels: `Display` and `Mouse` count in points, so it multiplies them by `Display.getPixelScaleFactor()`, which is `1` except on a macOS Retina screen with high density enabled (`-Dorg.lwjgl.opengl.Display.enableHighDPI=true`, outside fullscreen). Size the screen from the window bridge, not from `Display.getWidth()`; it sets the [mouse cursors](#mouse-cursors) through JNA. LWJGL 2 key codes follow the keyboard layout on Windows and Linux and the place of the key on macOS; `isPhysicalKeyDown` answers like `isKeyDown`.
- The audio bridge is the `AlAudioBridge` of `base-openal` on LWJGL 2's OpenAL (`dev.joid.backend.lwjgl2.binding.Lwjgl2AlBinding`). It creates the OpenAL context with `AL.create()` on the first video with sound, unless one already exists, and destroys it when the JVM exits; `AL.create()` loads the OpenAL native that `Backend.register()` installed.

### LWJGL 2 natives

The jar does not contain the LWJGL 2.9.1 classes, which your application declares, but contains the LWJGL and OpenAL natives for Windows, Linux and macOS. `Natives.install()` installs them once, before `Display.create()`; `Backend.register()` calls it too:

1. When the system property `org.lwjgl.librarypath` is set, or when one of the native libraries of the platform is found in a folder of `java.library.path`, nothing is extracted: the host provides them.
2. Otherwise the natives are extracted into `<java.io.tmpdir>/joid-lwjgl-2.9.1/<platform>` (`windows`, `linux` or `osx`), rewritten only when their size differs, and `org.lwjgl.librarypath` points to that folder.

`dev.joid.backend.lwjgl2.Natives.install()` runs once per JVM.

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
| `setCursor(Cursor)` / `getCursor()` | Shows a standard GLFW cursor (`glfwCreateStandardCursor`, the shapes of GLFW 3.4), created on first use and kept for the next ones; `DEFAULT`, or a shape the system lacks, gives the window its default cursor. `getCursor()` returns the last `Cursor` set. |
| `destroy()` | Destroys the cursors the bridge created; call it before `glfwDestroyWindow`. The demo window does. |

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

`dev.joid.base.openal.AlAudioBridge` implements `IAudioBridge` on OpenAL, in plain Java: every OpenAL call goes through an `IAlBinding` (`dev.joid.base.openal.binding`), so the same code runs on LWJGL 3 (`Lwjgl3AlBinding.inst()`, in the module), on LWJGL 2 (`Lwjgl2AlBinding.inst()`, in the LWJGL 2 backend) or on the OpenAL of a host.

```java
BridgeHandler.AUDIO.register(AlAudioBridge.create(Lwjgl3AlBinding.inst()).gain((gain, group) -> gain * this.settings.getVolume(group)));
```

- `createSource` uses the OpenAL context that is current, so a host that already plays sound shares its context; when none is current, it creates one through the binding (default device, context made current) and destroys it when the JVM exits.
- `ownContext(false)` leaves the OpenAL context to the program that embeds JOID, as a game that owns its sound engine: the bridge never creates one, and its sources stay silent while no context is current (no sound device, sound engine reloading), then start on the context of the game. By default (`true`), the bridge creates its own context when none is current.
- `AlAudioSource` streams 16-bit samples through a pool of OpenAL buffers, in mono for one channel and in stereo otherwise: a track of 3 channels or more is mixed down with [`AudioDownmix`](bridges.md#stereo-output-with-audiodownmix) first. It counts the buffered samples, reuses the played buffers, and plays again when a playing source ran dry. The source is placed at the listener, so OpenAL does not spatialize it; JOID applies the distance attenuation itself (see [Playback, Video and Audio](../resources/playback.md)).
- A source follows the context of the host: when the current context changes, as when a game reloads its sound engine, the source recreates its OpenAL source on the new context and drops the samples it had buffered; without a current context, it does nothing.
- `gain(IAudioGain)` sets the volume applied by the program that embeds JOID: the gain of a source goes through `IAudioGain.apply(gain, group)` (by default the gain itself), with the group given to the source (`null` for its default group). The source reads it again at every call (`write`, `getBufferedSamples`, `gain`, `play`...) and sends it to OpenAL when it changed, so a volume read there, such as the master volume times the volume of the category of the group in a game, follows its changes live.

| `IAlBinding` method | OpenAL call |
|---|---|
| `createContext()`, `destroyContext()`, `getCurrentContext()` | Device and context; `getCurrentContext()` returns an object that identifies the current context (compared with `equals`), `null` when none. |
| `genSource()`, `deleteSource(source)` | A source relative to the listener, at its position. |
| `genBuffer()`, `deleteBuffer(buffer)`, `bufferData(buffer, channels, samples, sampleRate)` | Buffers of 16-bit samples, mono or stereo. |
| `play(source)`, `pause(source)`, `stop(source)`, `gain(source, gain)` | Playback and `AL_GAIN`. |
| `queueBuffer(source, buffer)`, `unqueueBuffer(source)` | The buffer queue of a source. |
| `isPlaying(source)`, `getQueuedBuffers(source)`, `getProcessedBuffers(source)` | `AL_SOURCE_STATE`, `AL_BUFFERS_QUEUED`, `AL_BUFFERS_PROCESSED`. |

`joid-base-openal` declares LWJGL 3 as a compile-only dependency: the backends and applications on LWJGL 3 provide `lwjgl-openal` themselves, and an engine on another binding never loads `Lwjgl3AlBinding`.

The LWJGL 2 audio bridge mixes down the same way.

## Mouse cursors

Each backend shows the [cursor of the node under the pointer](../interactions/mouse-and-keyboard.md#mouse-cursor) with the system cursors of its window:

| Backend | Cursors |
|---|---|
| LWJGL 3, Vulkan | The standard cursors of GLFW 3.4 through `dev.joid.base.glfw.WindowBridge`: `POINTER` is the pointing hand, `MOVE` the four-way arrow (`GLFW_RESIZE_ALL_CURSOR`), the resize cursors the double arrows. A shape the system or its cursor theme lacks falls back to the default cursor. |
| LWJGL 2 | The system cursors of each OS, without images, through JNA (embedded in the jar, see [Installation](../getting-started/installation.md#embedded-libraries)): `LoadCursorW` on Windows, the cursor theme (`XcursorLibraryLoadCursor`) then the cursor font on X11, `NSCursor` on macOS. The handle goes to the `Display` of LWJGL 2, which keeps it for its window and hides it while the mouse is grabbed; nothing is applied while the mouse is grabbed. A shape the system lacks, or a JNA that cannot load, keeps the default cursor (with one warning in dev mode). The classes live in `dev.joid.backend.lwjgl2.window.cursor`: `NativeCursors.get()` picks the `NativeCursor` of the OS (`WindowsNativeCursor`, `X11NativeCursor`, `MacNativeCursor`). |
| Your engine | `IWindowBridge.setCursor(Cursor)` does nothing by default, so the cursor stays the one of your window until you implement it (see [Writing a Backend](writing-a-backend.md#window-and-audio-bridges)). |

## Embedding JOID in an existing host

JOID can draw inside an application that already owns the window, the graphics context and the main loop, such as a game.

### Thread and projection

Call `update()`, `draw()` and the input methods of your UI bridge from the thread that owns the graphics context, after the host drew its own frame. Each UI draws in its own projection, but the target, the viewport and the base projection come from you: call `screen(width, height)` of the render bridge before drawing (it draws to the window, with a viewport covering it and `ortho(0, width, height, 0, 0, 10000)`), and `load()` on your UI bridge when the host window is resized.

### Giving the host its state back

The OpenGL bridge of LWJGL 2 and LWJGL 3 gives the host its OpenGL state back by itself, so call `beginFrame()` before the JOID frame and `endFrame()` after it:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
render.beginFrame();
try {
	render.screen(width, height);
	uiBridge.draw();
} finally {
	render.endFrame();
}
```

![Diagram of an embedded frame: the host draws its frame, beginFrame() starts the journal, screen(width, height) sets the window pixels, the UIs draw, then endFrame() in a finally block puts back what JOID changed](../images/diagram-backend-host.png "beginFrame before the JOID frame, endFrame in a finally block: the host finds its state back")

Every OpenGL call of the bridge goes through a journal. The first time the bridge changes a state during a frame, the journal reads its value with `glGet*`; `endFrame()` puts back exactly the values it read, and only those. Its own objects (textures, vertex array, framebuffers) are left out; the parameters of a texture of the host, the attributes of a vertex array of the host and the bindings of each texture unit are put back. A call outside a frame (creating a shader or a framebuffer, uploading a texture) is journaled on its own. At the start of each frame, the bridge also sets the state it needs whatever the host left: no scissor, logic operation, polygon offset, sRGB conversion, primitive restart, depth clamp or rasterizer discard, filled polygons, 1-pixel lines without smoothing, counter-clockwise front faces with back faces culled, depth function `LESS`, depth cleared to 1 and stencil to 0, stencil write mask `0xFF`, pixel store at its defaults (alignment 4) without pixel buffer, and, in a compatibility profile, no alpha test, lighting, fog or color material. Caches of the host that mirror the OpenGL state stay right, since the state comes back unchanged.

When the host draws inside a JOID frame (an item or a model of the host shown in a UI), wrap its drawing in `host(...)`: the bridge puts the state of the host back before it, and journals again after it:

```java
render.host(() -> hostRenderer.drawItem(stack, x, y));
```

The Vulkan bridge keeps its state in Java and applies it at each draw call; its `host(...)` runs the drawing as is.

### Matrices of a fixed-function host

JOID keeps its matrices in Java and never reads the OpenGL matrix stacks. A host whose interface draws with the fixed-function matrices (`glOrtho`, `glTranslate`, as Minecraft up to 1.12 does) can hand them to JOID instead of calling `screen(width, height)`: `HostMatrixImport` (`dev.joid.base.opengl.render.host`) reads `GL_PROJECTION_MATRIX` and `GL_MODELVIEW_MATRIX` and loads them into the projection and model-view of the bridge, so the UIs draw in the coordinates of the host:

```java
final GlRenderBridge bridge = (GlRenderBridge) BridgeHandler.RENDER.get();
final HostMatrixImport matrices = HostMatrixImport.create(bridge);

bridge.beginFrame();
try {
	matrices.apply();
	uiBridge.draw();
} finally {
	bridge.endFrame();
}
```

`HostMatrixImport.create(bridge)` throws an `IllegalStateException` on a core context, which has no fixed-function matrices.

### Natives, audio and interface size

- The LWJGL 2 backend extracts nothing when the host already provides its natives.
- Both audio bridges reuse an OpenAL context that already exists.
- Follow the interface size of the host with [`getInterfaceScale`](ui-bridge.md#interface-scale-with-getinterfacescale).

### Registering bridges one by one

`Backend.register` is a shortcut. With another windowing system, register your own `IWindowBridge` next to the render and audio bridges of a module:

```java
BridgeHandler.AUDIO.register(AlAudioBridge.create(Lwjgl3AlBinding.inst()));
BridgeHandler.WINDOW.register(new HostWindowBridge());
BridgeHandler.RENDER.register(GlRenderBridge.create(Lwjgl3GlBinding.inst()));
```

Here `AlAudioBridge` and `Lwjgl3AlBinding` come from `dev.joid.base.openal`, `GlRenderBridge` is `dev.joid.base.opengl.render.GlRenderBridge`, `Lwjgl3GlBinding` is `dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding` and `HostWindowBridge` is your implementation of `IWindowBridge`. On LWJGL 2, the bridges are `GlRenderBridge.create(Lwjgl2GlBinding.inst())`, `AlAudioBridge.create(Lwjgl2AlBinding.inst())` (both bindings in `dev.joid.backend.lwjgl2.binding`) and `new WindowBridge()` (`dev.joid.backend.lwjgl2.window`); the Vulkan render bridge needs a GLFW window (`new RenderBridge(long window)`).

## The base-opengl module

The LWJGL 2 and LWJGL 3 backends render with `joid-base-opengl`, a module in plain Java that holds the whole OpenGL renderer and calls OpenGL only through binding interfaces. The LWJGL 2 backend implements them with LWJGL 2 (`dev.joid.backend.lwjgl2.binding`, `Lwjgl2GlBinding.inst()`) and the LWJGL 3 backend with LWJGL 3 (`dev.joid.backend.lwjgl3.binding`, `Lwjgl3GlBinding.inst()`); another engine on OpenGL implements them with its own functions and gets the same rendering.

| Package `dev.joid.base.opengl` | Content |
|---|---|
| `binding` | `IGlBinding` (`glEnable`, `glDisable`, `glIsEnabled`, `glGetInteger`, `glGetIntegerv`, `glGetString`, `glGetStringi`, `glGetFloatv`, `glGetTexParameteri`, `glGetVertexAttribi`, `glGetVertexAttribPointerv`, `glGetVertexAttribfv`, `glGetMaterialfv`, `glClear`, `glReadBuffer`, `glReadPixels`, and the getters of the domain bindings), `IGlStateBinding` (blending, depth, stencil, color mask, viewport, clear values, culling and front face, line width, polygon mode, pixel store), `IGlBufferBinding` (buffers, vertex arrays, attributes, `glDrawArrays`), `IGlProgramBinding` (shaders, programs, attribute and fragment output locations, uniforms), `IGlTextureBinding` (textures, units, `glBindSampler`), `IGlFrameBufferBinding` (framebuffers, renderbuffers, blits), returned by `getFrameBufferBinding(GlFrameBufferFamily)` for the core and ARB entry points (`CORE`) or the `EXT` ones, and `GlConstants`, the OpenGL values the module passes to them. |
| `capability` | `GlCapabilities.read(IGlBinding)` reads the context once: version, GLSL version, `GlProfile` (`COMPATIBILITY`, `CORE`, `FORWARD_COMPATIBLE_CORE`), extensions and maximum texture size; `hasVertexArrays()`, `hasSamplerObjects()`, `hasFrameBufferBlit()` and `getFrameBufferFamily()` (`null` without framebuffer objects) tell what it can do. `GlStrategies.of(GlCapabilities)` is the one place that chooses how to render on that context, see [OpenGL versions](#opengl-versions). |
| `render` | `GlRenderBridge` (`create(IGlBinding)`), the render bridge; `GlEnums`, the OpenGL values of the blend, stencil, wrap, filter, vertex and primitive enums of JOID; `shader.GlShader`, `texture.GlTexture` and `framebuffer.GlFrameBuffer`, on the core `Shader`, `Texture` and `FrameBufferHandle`; `texture.GlBorrowedTexture`, a texture of the host on the core `BorrowedTexture`, and `texture.IGlTexture` (`getId()`, `sample(TextureSampling)`), what the bridge binds; `vertex.GlVertexInput` (`ArrayObjectVertexInput`, `DefaultVertexInput`) and `texture.IGlMipmapBuilder` (`BlitMipmapBuilder`, `DrawMipmapBuilder`), the strategies of the bridge. |
| `render.host` | `IGlHostGuard` (`enter()`, `exit()`, `host(Runnable)`), implemented by `JournalGlHostGuard`: a `GlStateJournal` of `GlStateKey` values, written by `Journal*Binding` decorators of each binding, and `GlPipelineReset`, the state JOID sets at the start of each frame. `GlRenderBridge.getBinding()` returns the journaled binding. `HostMatrixImport` (`create(GlRenderBridge)`, `apply()`) loads the fixed-function matrices of a host into the bridge, see [Matrices of a fixed-function host](#matrices-of-a-fixed-function-host). |
| `resource` | `GlTextureResourceResolver.inst()`, which turns an `Integer` or an `IntSupplier` into a [texture of the host](../resources/resources.md#textures-of-the-host); `Backend.register` of LWJGL 2 and LWJGL 3 registers it. |
| `snapshot` | `GlSnapshotCapture.capture(bridge, width, height)` and `getRenderer(binding)`, for an `ISnapshotBackend` on OpenGL, and `GlStateSnapshot.read(binding, capabilities)`, the whole OpenGL state for [`HostStateContractSuite`](testkit.md#hoststatecontractsuite-tests); left out of the `-prod` jars and of the released `joid-base-opengl` jar. |

`GlRenderBridge.create(binding)` reads the capabilities, chooses its strategies and creates its vertex buffer, and its vertex array when the context has them: the context must be current. Every OpenGL call of JOID then goes through the bindings, so a binding that wraps another one sees all of them (the bridge wraps them itself in its journal), and `dev.joid.backend.lwjgl3.Backend.register(window, binding)` registers the bridges with such a binding. On LWJGL 3, `GlContextRequest` sets the GLFW hints of a context: `CORE_33` (OpenGL 3.3 core, forward compatible on macOS), `CORE_32_FORWARD` (3.2 core, forward compatible) or `COMPATIBILITY` (no version hint: the highest compatibility context of the driver, 2.1 on macOS), each with 24 bits of depth and 8 of stencil.

### OpenGL versions

The bridge renders the same pixels on every context from OpenGL 2.0 to 4.6. `GlStrategies` chooses once, from the capabilities:

| Concern | Choice |
|---|---|
| Shaders | The highest of GLSL 1.10, 1.20, 1.30, 1.40, 1.50 and 3.30 that the context compiles, with the uniforms declared one by one (`UniformLayout.LOOSE`). Attribute locations are bound before linking, and `fragColor` to output 0 in GLSL 1.30 to 1.50. A shader that needs more than the dialect, such as `uint` in GLSL 1.20, is refused with an `UnsupportedOperationException` naming the feature and the dialect it needs. |
| Uniforms | Sent member by member with `glUniform*` when they change, on every context. JOID uses no uniform buffer on OpenGL. |
| Sampling | The filter and the wrap are texture parameters, set when they change for that texture. JOID uses no sampler object, and binds sampler 0 on the units it uses when the context has sampler objects. |
| Vertex input | Its own vertex array object with OpenGL 3.0 or `GL_ARB_vertex_array_object`. Without them, the attributes of the default vertex array are pointed at the JOID buffer before each draw. |
| Framebuffers | The OpenGL 3.0 and `GL_ARB_framebuffer_object` functions, else those of `GL_EXT_framebuffer_object`; a context without either is refused. |
| Mipmaps | A chain of linear blits, level by level, as on Vulkan. Without `glBlitFramebuffer` (EXT framebuffers without `GL_EXT_framebuffer_blit`), each level is drawn from the previous one with a linear quad, which gives the same pixels on Mesa llvmpipe but may differ slightly on other drivers; dev mode prints `[JOID] This OpenGL context cannot blit framebuffers (...)` once. |
| Texture size | `allocate` above `GL_MAX_TEXTURE_SIZE` throws an `IllegalArgumentException` with the size and the limit. |

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
| `dev.joid.backend.lwjgl2.Backend` | `static register()` | Checks the version, installs the natives, registers the OpenAL audio bridge, the LWJGL 2 window bridge and the OpenGL render bridge of the current context. |
| `dev.joid.backend.lwjgl3.Backend` | `static register(long window)` | Checks the version, registers the OpenAL audio bridge, the OpenGL render bridge and the GLFW window bridge of `window`. |
| `dev.joid.backend.lwjgl3.Backend` | `static register(long window, IGlBinding binding)` | The same, with the render bridge on `binding`, a binding that wraps `Lwjgl3GlBinding.inst()`. |
| `dev.joid.backend.vulkan.Backend` | `static register(long window)` | Checks the version, registers the OpenAL audio bridge, the GLFW window bridge and a Vulkan render bridge on `window`. |
| `dev.joid.backend.lwjgl2.Natives` | `static install()` | Installs the LWJGL 2 natives once per JVM; call it before `Display.create()`. |
| `dev.joid.base.opengl.render.host.HostMatrixImport` | `static create(GlRenderBridge bridge)`, `apply()` | Loads the projection and model-view matrices of a fixed-function host into the bridge; refuses a core context. |
| `dev.joid.internal.JOID` | `static checkVersion(String version)` | `true` when the major version of `version` matches the loaded JOID; otherwise prints the warning and returns `false`. |

## Pitfalls

- Create the OpenGL context, make it current and call `GL.createCapabilities()` before `dev.joid.backend.lwjgl3.Backend.register`.
- Create the `Display` (or let the host create its context) before `dev.joid.backend.lwjgl2.Backend.register`, and install the natives with `Natives.install()` before `Display.create()`.
- On LWJGL 2 without vertex array objects (OpenGL 2.1 without `GL_ARB_vertex_array_object`), an attribute of the host that points into client memory instead of a buffer is not put back: LWJGL 2 only takes a buffer offset or a Java buffer.
- On a context without `glBlitFramebuffer`, mipmapped textures may not be pixel-exact with the other backends.
- A host draw inside a JOID frame without `host(...)` runs on the state JOID set, and JOID then puts back the state of the host as it was before the frame, over what the host draw changed.
- Never draw JOID while the host records a display list (`glNewList`): its calls would be recorded in it.
- On Vulkan, a clear or a `draw()` outside `beginFrame()` / `endFrame()` throws.
- Without a stencil buffer, the masks of the UIs do not clip.
- The demo windows and `DemoUIBridge` are not in the `-prod` jars: never reference them from application code.

## See also

- Next: [Writing a Backend](writing-a-backend.md)
- [Bridges](bridges.md)
- [UI Bridge](ui-bridge.md)
- [Installation](../getting-started/installation.md)
- [Testkit](testkit.md)