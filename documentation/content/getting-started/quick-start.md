# Quick Start

This page is the smallest complete JOID program: a window on the LWJGL 3 backend, a UI bridge, a font and a first `UI` with a button that counts its clicks. Copy the four classes into the package `com.example` and run them.

![A dimmed window with a rounded gray button at its center reading Clicks: 2](../images/intro-quick-start.png "The program of this page after two clicks")

| Class | Role |
| --- | --- |
| `AppUIBridge` | Hosts the open UIs, receives the input and clears the screen. |
| `Theme` | Loads the font once, after JOID. |
| `CounterUI` | The screen: a button that counts its clicks. |
| `Main` | Creates the window, registers the backend and the bridge, loads JOID and runs the frame loop. |

## Step 1: set up the project

Use the Gradle build of [Installation](installation.md) with the LWJGL 3 jar. JOID ships no font for your UIs: put any TrueType or OpenType file in your working directory as `fonts/Montserrat-Regular.ttf`. To run with `gradle run`, add the `application` plugin:

```groovy
plugins {
	id 'java'
	id 'application'
}

application {
	mainClass = 'com.example.Main'
	if (System.getProperty('os.name').toLowerCase().contains('mac')) {
		applicationDefaultJvmArgs = ['-XstartOnFirstThread']
	}
}
```

## Step 2: the UI bridge

A UI bridge decides where UIs live. `StackUIBridge` keeps a stack of screens: opening a screen closes the previous one, and popups and overlays open on top. Each frame, `drawBackground()` runs before the UIs draw; clear the screen there:

```java
package com.example;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.ui.StackUIBridge;

public final class AppUIBridge extends StackUIBridge {

	@Override
	protected void drawBackground() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.clearColor(0.1F, 0.1F, 0.1F, 1F);
		render.clearDepth();
		render.clearStencil();
	}

}
```

## Step 3: the font

`MsdfFontLoader.load(...)` reads a font file and returns a `CompletableFuture<MsdfFont>`; `join()` waits for it. The first load generates the MSDF atlas into a cache folder, so it takes a moment; later runs read the cache.

```java
package com.example;

import java.io.File;

import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	private static MsdfFont font;

	private Theme() {}

	public static void load() {
		Theme.font = MsdfFontLoader.load(new File("fonts/Montserrat-Regular.ttf")).join();
	}

	public static MsdfFont getFont() {
		return Theme.font;
	}

}
```

## Step 4: the first UI

A screen extends `UI` and builds its nodes in `init()`. Positions are units of the 1920×1080 virtual canvas, which JOID fits into the window.

```java
package com.example;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public final class CounterUI extends UI {

	private static final Color INK   = Color.decode("#999999");
	private static final Color HOVER = Color.decode("#808080");

	private final IntegerSignal clicks = IntegerSignal.of(0);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(Theme.getFont(), 40F, Color.WHITE);

		RectNode
		.create(760, 440, 400, 120)
		.color(CounterUI.INK)
		.hoveredColor(CounterUI.HOVER)
		.effect(RoundedNodeEffect.create(16F))
		.onClick((node, mouseX, mouseY, button) -> this.clicks.increment())
		.body(rect -> {
			TextNode.create(rect.dw(2), rect.dh(2)).text(Text.create("Clicks: " + this.clicks.get(), info)).anchor(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}
```

- `RectNode.create(x, y, width, height)` creates a 400×120 rectangle centered on the canvas; `hoveredColor(...)` is the color it blends to under the mouse.
- `effect(RoundedNodeEffect.create(16F))` rounds its corners with a radius of 16.
- `onClick(...)` runs when the rectangle is pressed. `button` is the `MouseButton`.
- `body(...)` builds the children right away. Children are positioned relative to their parent: `rect.dw(2)` is half its width, and `anchor(Align.CENTER)` makes the position the center of the text.
- `attach(this)` adds a node to the UI; `attach(rect)` adds it to another node.
- `Text.create("Clicks: " + this.clicks.get(), info)` reads the `IntegerSignal`: JOID follows the signals an expression reads and recomputes the text on each click.

## Step 5: the window and the frame loop

`Main` creates an OpenGL 3.3 core window with GLFW, registers the backend and the bridge, loads JOID and the font, opens the UI and runs the loop:

```java
package com.example;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.Backend;
import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.base.glfw.input.GlfwInputForwarder;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;

public final class Main {

	public static void main(final String[] args) {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GlContextRequest.CORE_33.apply();
		final long window = GLFW.glfwCreateWindow(1280, 720, "JOID Quick Start", 0L, 0L);
		if (window == 0L) {
			throw new IllegalStateException("Unable to create the window");
		}

		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		Backend.register(window);

		final AppUIBridge bridge = new AppUIBridge();
		BridgeHandler.UI.register(bridge);
		JOID.inst().load();
		Theme.load();

		GlfwInputForwarder.create(bridge).attach(window);
		GLFW.glfwSetFramebufferSizeCallback(window, (handle, width, height) -> {
			if (width > 0 && height > 0) {
				bridge.resize(width, height);
			}
		});

		final IWindowBridge windowBridge = BridgeHandler.WINDOW.get();
		bridge.resize(windowBridge.getWidth(), windowBridge.getHeight());
		JOID.open(new CounterUI());

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

The order of the startup calls matters:

1. The OpenGL context is current before `Backend.register(window)`, which registers the window, render and audio bridges.
2. `BridgeHandler.UI.register(bridge)` makes your bridge the host of the UIs.
3. `JOID.inst().load()` runs once, after the bridges and before any UI opens; load your fonts after it.
4. `bridge.resize(width, height)` sets the viewport and refits every open UI; call it at startup and when the framebuffer changes.
5. `JOID.open(ui)` hands the UI to its bridge, which loads it and runs its `init()`.

`GlfwInputForwarder` forwards the keys, characters, mouse buttons, cursor and scroll of the window to the bridge. `bridge.frame()` updates the UIs, then draws them between `beginFrame()` and `endFrame()` of the render bridge.

## Step 6: run it

Run `gradle run`. The window shows the button on a dimmed background, the default background of a UI; each click increments the counter. `Escape` closes the UI.

With the `-dev` jar, enable the developer tools before loading JOID, then press `F3` for the developer panel:

```java
JOID.inst().setDevMode(true).load();
```

## Other backends

The UI, the font and the bridge stay the same; only `Main` changes. For Vulkan, create the window without a client API, raise the LWJGL stack size before anything else, and present the frame through the render bridge:

```java
Configuration.STACK_SIZE.set(1024);
GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
final long window = GLFW.glfwCreateWindow(1280, 720, "JOID Quick Start", 0L, 0L);
Backend.register(window);
```

```java
bridge.frame();
((VulkanRenderBridge) BridgeHandler.RENDER.get()).present();
```

For LWJGL 2, create a `Display` with depth and stencil bits, call `Backend.register()`, and forward the input with `Lwjgl2InputForwarder`:

```java
Natives.install();
Display.setDisplayMode(new DisplayMode(1280, 720));
Display.setResizable(true);
Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
Backend.register();
```

```java
final Lwjgl2InputForwarder input = Lwjgl2InputForwarder.create(bridge);
while (!Display.isCloseRequested()) {
	input.poll();
	bridge.frame();
	Display.update();
	if (Display.wasResized()) {
		bridge.resize(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
	}
}
```

The demo window of each backend (`dev.joid.backend.<backend>.demo.DemoWindow` in the `-dev` jars) is a complete reference of this setup.

## See also

- Next: [Canvas and Scaling](../concepts/canvas.md)
- [UIs](../concepts/uis.md)
- [Frame Loop and Dev Tools](../concepts/frame-loop.md)
- [Bridges and Backends](../integration/backends.md)
- [Embedding JOID in an Application](../integration/ui-bridge.md)