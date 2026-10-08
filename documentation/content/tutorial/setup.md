# Tutorial 1: Project Setup

This four-part tutorial puts what [Core Concepts](../concepts/canvas.md) and the Essentials taught together in one real application: a settings screen. In this first part you turn the [Quick Start](../getting-started/quick-start.md) project into that application: a new package, a bold face in the font, and a first screen in place of the counter. Each later part starts from the code of the previous one.

## What you build

By the end of [Tutorial 4](polish.md), the application shows a settings card centered in the window:

- a header with an icon, the title "Settings" and a hint;
- an **Audio** section with a **Music** on/off switch and a **Volume** slider whose row hides while the music is off;
- a **General** section with a **Notifications** switch;
- a **Language** list where the selected entry carries a dot;
- values saved to disk and restored on the next launch;
- hover colors, tooltips, rounded corners, a gradient background, a sliding switch and an opening animation.

![The finished settings screen: a rounded light gray card on a dark gradient, with a header, Music and Notifications switches, a Volume slider at 80 % and a language list with English selected](../images/tutorial-overview.png "The settings screen at the end of Tutorial 4")

JOID draws nothing by itself: every color, size and shape of this screen is written in the tutorial code, in the neutral grays of the documentation. Change the constants and the same code draws your own design.

| Part | You put into practice |
| --- | --- |
| 1. Project Setup (this page) | The Quick Start project as a base, a font family, a first screen configured with `@UIData`. |
| [2. Building the Layout](layout.md) | Nodes and `body`, the size helpers, anchors, nested `FlexNode` columns, text and an image. |
| [3. Interactivity and State](interactivity.md) | `onClick`, signals followed by the nodes, controls you draw (a switch, a slider), a permanent store. |
| [4. Polish](polish.md) | A gradient background, effects, hover animation, tooltips drawn by the UI, a `TweenAnimator`, a transition. |

The application has these classes, all in the package `com.example.settings`:

| Class | Role | Written in |
| --- | --- | --- |
| `Main` | Creates the window, registers the backend and the bridge, loads JOID and the theme, runs the frame loop. | Quick Start, adapted in parts 1 and 3 |
| `AppUIBridge` | Hosts the open UIs and feeds them the input. | Quick Start, unchanged |
| `Theme` | The font and the colors of the application. | Quick Start, grows in parts 1, 2 and 4 |
| `SettingsUI` | The settings screen. | Part 1, grows in every part |
| `ToggleSwitchNode`, `VolumeSliderNode` | The on/off switch and the slider. | Part 3 |
| `SettingsStore` | The settings values, saved to disk. | Part 3 |

## Step 1: start from the Quick Start project

Make a copy of the project of the [Quick Start](../getting-started/quick-start.md), with its `-dev` jar: the developer tools help while you build the screen, and you switch to the `-prod` jar when you ship. Move `Main`, `AppUIBridge` and `Theme` to the package `com.example.settings`, and delete `CounterUI`: `SettingsUI` takes its place in step 3. Then point the `application` block of `build.gradle` to the new main class:

```groovy
application {
	mainClass = 'com.example.settings.Main'
	if (System.getProperty('os.name').toLowerCase().contains('mac')) {
		applicationDefaultJvmArgs = ['-XstartOnFirstThread']
	}
}
```

Finally, put these files in the working directory of the program (the project folder when you use `gradle run`):

| File | Content |
| --- | --- |
| `fonts/Montserrat-Regular.ttf`, `fonts/Montserrat-Bold.ttf` | A regular and a bold face of any TrueType or OpenType family. The tutorial uses Montserrat; any family works if you adjust the file names in `Theme`. |
| `icons/settings.png` | A 48×48 icon (PNG or SVG), shown in the header from part 2. |

`AppUIBridge` does not change in the whole tutorial: it keeps every open UI in a list, as in the Quick Start. If your Quick Start runs on LWJGL 2 or Vulkan, keep its `Main` too: the changes of this tutorial are the same on every backend.

## Step 2: add the bold face to Theme

The screen has bold titles, so the font needs a bold face. Passing several files to `MsdfFontLoader.load(...)` builds one family, as [Text](../essentials/text.md) showed. Replace `load()` in `Theme`:

```java
public static void load() {
	Theme.font = MsdfFontLoader.load(new File("fonts/Montserrat-Regular.ttf"), new File("fonts/Montserrat-Bold.ttf")).join();
}
```

A `TextInfo` that asks for `FontWeight.BOLD` draws the bold face; any other weight picks the closest loaded face. The first launch generates the atlas of the new file, which takes a few seconds; later launches read the cache. The UIs keep reading the font with `Theme.getFont()`, so they keep a constructor without arguments, which `Ctrl+Shift+R` needs to recreate a UI.

## Step 3: write the settings screen

Create `SettingsUI` in place of `CounterUI`. In this first part it shows only its title:

```java
package com.example.settings;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(backgroundColor = "#18181B")
public final class SettingsUI extends UI {

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(Theme.getFont(), FontWeight.BOLD, 40F, Color.WHITE);
		TextNode.create(960, 540).text(Text.create("Settings", title)).anchor(Align.CENTER).attach(this);
	}

}
```

- `@UIData(backgroundColor = "#18181B")` replaces the default translucent dark gray behind the UI with an opaque near-black. The other options of a UI class are in [UIs and Their Lifecycle](../concepts/uis.md).
- `TextInfo.create(font, weight, size, color)` is the bold style of the title. (960, 540) is the center of the canvas, and `anchor(Align.CENTER)` puts the center of the text there.

## Step 4: open it from Main

`Main` keeps the window, the startup order, the input forwarding and the frame loop of the Quick Start, which [The Frame Loop](../concepts/frame-loop.md) explains. Only two lines change. The window gets its title:

```java
final long window = GLFW.glfwCreateWindow(1280, 720, "Settings", 0L, 0L);
```

and `JOID.open` receives the settings screen:

```java
JOID.open(new SettingsUI());
```

The complete `Main` is at the end of this page.

## Step 5: run it

Run `gradle run`, or the `Main` class from your IDE (with `-XstartOnFirstThread` on macOS). You see a window filled with near-black, with "Settings" in bold white at its center:

![A near-black window with the word Settings in bold white at its center](../images/tutorial-setup-window.png "The first UI: one centered text node on the UI background")

Resize the window: the title stays centered and scales with it. Positions are units of the 1920×1080 virtual canvas, fitted to the window without stretching; wider or taller windows show extra canvas around it.

![The 1920×1080 canvas fitted into a 16:9, a 21:9 and a 4:3 window; the extra visible area is hatched](../images/diagram-canvas.png "One canvas, fitted into every window")

See [The Virtual Canvas](../concepts/canvas.md). Press `Escape`: the screen closes, as a UI does by default (its `closeable` option is `true`), and only the gray clear color of the loop remains.

> TIP: Start JOID with `JOID.inst().setDevMode(true).load()` instead of `JOID.inst().load()` while you follow the next parts: `Ctrl+R` or `F5` then reruns `init()` after each change, and `F3` shows the developer panel. See [Developer Tools](../concepts/dev-tools.md).

## The complete code

`SettingsUI` is complete above. `Theme`:

```java
package com.example.settings;

import java.io.File;

import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	private static MsdfFont font;

	private Theme() {}

	public static void load() {
		Theme.font = MsdfFontLoader.load(new File("fonts/Montserrat-Regular.ttf"), new File("fonts/Montserrat-Bold.ttf")).join();
	}

	public static MsdfFont getFont() {
		return Theme.font;
	}

}
```

`Main`:

```java
package com.example.settings;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Platform;

import dev.joid.impl.glfw.WindowBridge;
import dev.joid.impl.glfw.input.KeyCharacterMerger;
import dev.joid.impl.lwjgl3.Backend;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.utils.click.ClickType;

public final class Main {

	private final long window;
	private final AppUIBridge bridge;
	private final KeyCharacterMerger keyMerger;

	private long pressTime;
	private ClickType clickType;

	private Main(final long window, final AppUIBridge bridge) {
		this.window = window;
		this.bridge = bridge;
		this.keyMerger = KeyCharacterMerger.create(bridge::keyTyped);
	}

	public static void main(final String[] args) {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, Platform.get() == Platform.MACOSX ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, 24);
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);

		final long window = GLFW.glfwCreateWindow(1280, 720, "Settings", 0L, 0L);
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

		final Main main = new Main(window, bridge);
		main.listen();
		main.resize();

		JOID.open(new SettingsUI());
		main.loop();
	}

	private void listen() {
		GLFW.glfwSetKeyCallback(this.window, (handle, code, scancode, action, mods) -> this.onKey(code, action, mods));
		GLFW.glfwSetCharCallback(this.window, (handle, codepoint) -> this.keyMerger.charTyped(codepoint));
		GLFW.glfwSetMouseButtonCallback(this.window, (handle, button, action, mods) -> this.onMouseButton(button, action));
		GLFW.glfwSetCursorPosCallback(this.window, (handle, x, y) -> this.onCursorMove());
		GLFW.glfwSetScrollCallback(this.window, (handle, x, y) -> this.bridge.mouseScroll((int) (y * 120D)));
		GLFW.glfwSetFramebufferSizeCallback(this.window, (handle, width, height) -> this.resize());
	}

	private void loop() {
		while (!GLFW.glfwWindowShouldClose(this.window)) {
			if (GLFW.glfwGetWindowAttrib(this.window, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE) {
				GLFW.glfwWaitEvents();
				continue;
			}

			GLFW.glfwPollEvents();
			this.keyMerger.flush();

			this.bridge.update();
			BridgeHandler.RENDER.get().clear(0.1F, 0.1F, 0.1F, 1F);
			this.bridge.draw();
			GLFW.glfwSwapBuffers(this.window);
		}

		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
	}

	private void resize() {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		if (window.getWidth() == 0 || window.getHeight() == 0) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.ortho(0D, window.getWidth(), window.getHeight(), 0D, 0D, 10000D);
		render.viewport(0, 0, window.getWidth(), window.getHeight());
		this.bridge.load();
	}

	private void onKey(final int code, final int action, final int mods) {
		if (action == GLFW.GLFW_RELEASE) {
			return;
		}

		this.keyMerger.keyPressed(WindowBridge.getKey(code), code, mods);
	}

	private void onMouseButton(final int button, final int action) {
		if (action == GLFW.GLFW_PRESS) {
			this.clickType = ClickType.from(button);
			this.pressTime = System.currentTimeMillis();
			this.bridge.mousePressed(this.clickType);
		} else if (this.clickType != null) {
			this.bridge.mouseReleased(this.clickType);
			this.clickType = null;
		}
	}

	private void onCursorMove() {
		if (this.clickType != null) {
			this.bridge.mouseDragged(this.clickType, System.currentTimeMillis() - this.pressTime);
		}
	}

}
```

`AppUIBridge`, the bridge of the Quick Start in the new package:

```java
package com.example.settings;

import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.UIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.ui.core.UI;

public final class AppUIBridge extends UIBridge {

	@Override
	public void open(final UI ui) {
		this.add(ui);
	}

	@Override
	public void close(final UI ui) {
		this.remove(ui);
	}

	@Override
	public void add(final UI ui) {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		super.getUiList().add(ui);
		ui.load(window.getWidth(), window.getHeight());
	}

	@Override
	public void remove(final UI ui) {
		super.getUiList().remove(ui);
	}

	@Override
	public boolean canHandle(final UI ui) {
		return true;
	}

	@Override
	public boolean canHandle(final Class<? extends UI> clazz) {
		return true;
	}

	@Override
	public void drawHover(final UI ui, final List<String> lines, final double mouseX, final double mouseY) {}

}
```

## Recap

- The settings application is the Quick Start project in the package `com.example.settings`: the same `Main`, `AppUIBridge` and `Theme`, with a window title, a bold face and another screen.
- Several font files passed to `MsdfFontLoader.load(...)` make one family; `FontWeight` picks the face.
- `@UIData` configures the screen class, here its background.

## See also

- Next: [Tutorial 2: Building the Layout](layout.md) fills the screen with the settings card.
- [Quick Start](../getting-started/quick-start.md)
- [UIs and Their Lifecycle](../concepts/uis.md)
- [The Frame Loop](../concepts/frame-loop.md)
- [Text](../essentials/text.md)