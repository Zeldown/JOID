# Backends

A backend implements the window, render and audio [bridges](bridges.md) for one engine. JOID ships three: LWJGL 2, LWJGL 3 and Vulkan. Pick the one that matches your host, register it before loading JOID, and register your own [UI bridge](ui-bridge.md) next to it.

The backend is the only part of a JOID application that knows the engine. Your UIs run unchanged on all three, and the snapshot tests compare the backends pixel by pixel so that they also look the same; switching backends, or following a new engine version, never requires changes to your UI code.

| Module | Engine | Register with | Window bridge | Audio bridge | Generated shaders |
|---|---|---|---|---|---|
| `lwjgl2` | LWJGL 2.9.1: OpenGL with the fixed-function pipeline | `dev.joid.impl.lwjgl2.Backend.register()` | LWJGL 2 `Display`, `Mouse`, `Keyboard` | LWJGL 2 OpenAL | GLSL 1.20 |
| `lwjgl3` | LWJGL 3.3.4: OpenGL 3.3 | `dev.joid.impl.lwjgl3.Backend.register(window)` | `glfw` module | `openal` module | GLSL 3.30 |
| `vulkan` | LWJGL 3.3.4: Vulkan 1.3, shaderc | `dev.joid.impl.vulkan.Backend.register(window)` | `glfw` module | `openal` module | GLSL 4.50 compiled to SPIR-V at runtime |

Each `Backend.register` registers the audio, window and render bridges of its module; the clock bridge is already registered by JOID. The backend jars, their `prod` and `dev` flavors and the dependencies to declare are listed in [Installation](../getting-started/installation.md).

## LWJGL 3

Create a GLFW window with an OpenGL 3.3 core context and an 8-bit stencil buffer (the demo windows also ask for a 24-bit depth buffer), make its context current, then register the backend with the window handle:

```java
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Platform;

import dev.joid.impl.lwjgl3.Backend;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

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

`AppUIBridge` and `AppLoop` are the bridge and the event loop of [UI Bridge](ui-bridge.md); the loop ends each frame with `GLFW.glfwSwapBuffers(window)`.

- The render bridge creates its OpenGL objects in its constructor: the context must be current, with its capabilities created, before `Backend.register`. Every drawing call must then happen on that thread.
- The stencil buffer is needed by [UI masks](../ui/ui-class.md).
- The jar does not contain LWJGL: your application declares `lwjgl`, `lwjgl-glfw`, `lwjgl-openal` and `lwjgl-opengl` 3.3.4, with the natives of each for its platforms.
- On macOS, start the JVM with `-XstartOnFirstThread`, as GLFW requires.

## Vulkan

The Vulkan backend owns its instance, device and swapchain, created on a GLFW window without client API. Raise LWJGL's stack size before any LWJGL call, then drive each frame between `beginFrame()` and `endFrame()`:

```java
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.impl.vulkan.Backend;
import dev.joid.impl.vulkan.render.RenderBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;

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
        render.ortho(0D, windowBridge.getWidth(), windowBridge.getHeight(), 0D, 0D, 10000D);
        render.viewport(0, 0, windowBridge.getWidth(), windowBridge.getHeight());
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
    }

}
```

Set `ortho` and `viewport` from the framebuffer size again, and call `bridge.load()`, when the window is resized; register the input callbacks as in [UI Bridge](ui-bridge.md#driving-the-bridge-from-your-loop).

| Method of `dev.joid.impl.vulkan.render.RenderBridge` | Description |
|---|---|
| `beginFrame()` | Acquires the next swapchain image and starts recording. Recreates the swapchain first when the window size changed. Throws an `IllegalStateException` when a frame is already open. |
| `endFrame()` | Submits the frame and waits for the GPU to finish it. |
| `present()` | Shows the image on the window. |

- Every `draw()` of your UI bridge, and every clear, must happen between `beginFrame()` and `endFrame()`; outside, the bridge throws `Vulkan rendering must happen between beginFrame and endFrame`. `update()` and the input methods can run outside the frame.
- A Vulkan 1.3 device able to present to the window is required; without one, `Backend.register` throws `No Vulkan 1.3 device able to present to the window was found`.
- Wide and smooth lines are enabled when the device supports them.
- The swapchain presents without waiting for the vertical blank when the driver allows it (immediate mode, then mailbox, then FIFO).
- The jar does not contain LWJGL: your application declares `lwjgl`, `lwjgl-glfw`, `lwjgl-openal`, `lwjgl-vulkan` and `lwjgl-shaderc` 3.3.4, with the natives of `lwjgl`, `lwjgl-glfw`, `lwjgl-openal` and `lwjgl-shaderc`, plus those of `lwjgl-vulkan` on macOS (MoltenVK).
- On macOS, start the JVM with `-XstartOnFirstThread`.

## LWJGL 2

Register the backend before creating the `Display`, so that its natives are in place when LWJGL loads them. Ask for a stencil buffer:

```java
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.impl.lwjgl2.Backend;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;

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
        render.ortho(0D, Display.getWidth(), Display.getHeight(), 0D, 0D, 10000D);
        render.viewport(0, 0, Display.getWidth(), Display.getHeight());
        JOID.open(new UIMainMenu());

        while (!Display.isCloseRequested()) {
            bridge.update();
            render.clear(0F, 0F, 0F, 1F);
            bridge.draw();
            Display.update();

            if (Display.wasResized()) {
                render.ortho(0D, Display.getWidth(), Display.getHeight(), 0D, 0D, 10000D);
                render.viewport(0, 0, Display.getWidth(), Display.getHeight());
                bridge.load();
            }
        }

        Display.destroy();
    }

}
```

Feed the input from `Mouse.next()` and `Keyboard.next()`: `Keyboard.getEventCharacter()` and the key arrive in the same event, and `dev.joid.impl.lwjgl2.window.WindowBridge.getKey(int)` converts an LWJGL 2 key code into a `Key`. The `DemoWindow` of the module has the complete loop.

- The render bridge maps every call onto the OpenGL state of the current context: matrices go to the OpenGL matrix stacks, and `pushState()` / `popState()` save and restore the OpenGL state itself. It needs OpenGL 2.0 shaders and 3.0 framebuffer objects.
- The window bridge reads LWJGL 2's `Display`, `Mouse` and `Keyboard`, and the clipboard through AWT.
- The audio bridge uses LWJGL 2's OpenAL. It creates the OpenAL context on the first video with sound, unless one already exists, and destroys it when the JVM exits.

### LWJGL 2 natives

The jar does not contain the LWJGL 2.9.1 classes, which your application declares, but contains the LWJGL and OpenAL natives for Windows, Linux and macOS. `Backend.register()` installs them once:

1. When the system property `org.lwjgl.librarypath` is set, or when one of the native libraries of the platform is found in a folder of `java.library.path`, nothing is extracted: the host provides them.
2. Otherwise the natives are extracted into `<java.io.tmpdir>/joid-lwjgl-2.9.1/<platform>` (`windows`, `linux` or `osx`), rewritten only when their size differs, and `org.lwjgl.librarypath` points to that folder.

`dev.joid.impl.lwjgl2.Natives.install()` performs this step alone; it runs once per JVM.

## The glfw and openal modules

The LWJGL 3 and Vulkan backends share two modules, also published as their own jars for engines built on GLFW or OpenAL.

### GLFW window bridge

`dev.joid.impl.glfw.WindowBridge` implements `IWindowBridge` for a GLFW window:

| Member | Description |
|---|---|
| `new WindowBridge(long window)` | A bridge reading the given window. |
| `getWidth()` / `getHeight()` | The framebuffer size in pixels. |
| `getMouseX()` / `getMouseY()` | The cursor position converted into framebuffer pixels, so the mouse matches the drawing on high-density screens. |
| `isMouseGrabbed()` | `true` when the cursor mode is `GLFW_CURSOR_DISABLED`. |
| `isKeyDown(Key)` | `glfwGetKey` of the matching GLFW key. |
| `getClipboard()` / `setClipboard(String)` | The GLFW clipboard. |
| `static getKey(int code)` | The `Key` of a GLFW key code, `Key.UNKNOWN` when it has none. |

### OpenAL audio bridge

`dev.joid.impl.openal.AudioBridge` implements `IAudioBridge` with LWJGL 3's OpenAL:

- `createSource` uses the OpenAL context that is current, so a host that already plays sound shares its context; when none is current, it opens the default device, creates a context, makes it current and destroys it when the JVM exits.
- `AudioSource` streams 16-bit samples through a queue of OpenAL buffers, in mono for one channel and stereo otherwise. The source is placed at the listener, so its sound is not spatialized by OpenAL; JOID applies the [distance attenuation](../resources/playback.md#3d-audio-with-location-and-audiolistener) itself.

## Embedding JOID in an existing host

JOID can draw inside an application that already owns the window, the graphics context and the main loop, such as a game.

### Thread and projection

Call `update()`, `draw()` and the input methods of your UI bridge from the thread that owns the graphics context, after the host drew its own frame. Each UI draws in its own projection, but the viewport and the base projection come from you: call `ortho(0, width, height, 0, 0, 10000)` and `viewport(0, 0, width, height)` before drawing, and `load()` on your UI bridge when the host window is resized.

### Giving the host its state back

The LWJGL 2 bridge works on the live OpenGL state. Wrap the JOID frame to restore what it changes:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
render.pushState();
render.pushProjection();
render.pushMatrix();
try {
    render.ortho(0D, width, height, 0D, 0D, 10000D);
    render.viewport(0, 0, width, height);
    uiBridge.draw();
} finally {
    render.popMatrix();
    render.popProjection();
    render.popState();
}
```

The LWJGL 3 and Vulkan bridges keep their state in Java and apply it at each draw call. On OpenGL, the LWJGL 3 bridge binds its own vertex array, buffer, program, framebuffer, viewport, blending, depth, culling, stencil, line width, and a texture with a sampler object on texture unit 0, and leaves them bound: restore what your renderer needs after JOID's frame, including `glBindSampler(0, 0)` when your renderer relies on texture parameters.

### Natives, audio and interface size

- The LWJGL 2 backend extracts nothing when the host already provides its natives.
- Both audio bridges reuse an OpenAL context that already exists.
- Follow the interface size of the host with [`getInterfaceScale`](ui-bridge.md#interface-scale).

### Registering bridges one by one

`Backend.register` is a shortcut. With another windowing system, register your own `IWindowBridge` next to the render and audio bridges of a module: `new dev.joid.impl.lwjgl3.render.RenderBridge()` and `new dev.joid.impl.openal.AudioBridge()`, or the `RenderBridge`, `WindowBridge` and `AudioBridge` of `dev.joid.impl.lwjgl2`, all with public constructors. The Vulkan render bridge needs a GLFW window.

## Demo windows

Each backend module has a demo window that opens the JOID demo UIs, in dev and demo modes. It needs the `dev` jars, since the `prod` jars leave the demo out.

| Command | Main class |
|---|---|
| `./gradlew :lwjgl2:runDemo` | `dev.joid.impl.lwjgl2.demo.DemoWindow` |
| `./gradlew :lwjgl3:runDemo` | `dev.joid.impl.lwjgl3.demo.DemoWindow` |
| `./gradlew :vulkan:runDemo` | `dev.joid.impl.vulkan.demo.DemoWindow` |

The LWJGL 3 and Vulkan demo windows extend `dev.joid.impl.glfw.DemoWindow`, an abstract GLFW loop whose subclasses provide `getEngineName()`, `configureWindow()` (window hints), `registerBackend(long window)`, `beginFrame()` and `endFrame()`. Its input handling, which merges the GLFW key and character callbacks, is the reference for your own loop. See [Developer Tools](../getting-started/dev-tools.md) for the demo UIs.

## See also

- [Bridges](bridges.md) — the bridge interfaces and registries.
- [UI Bridge](ui-bridge.md) — the bridge and the loop your application writes.
- [Writing a Backend](writing-a-backend.md) — supporting another engine.
- [Installation](../getting-started/installation.md) — artifacts and dependencies.