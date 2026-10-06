# Tutorial 1: Project Setup

This four-part tutorial builds one real application with JOID: a settings screen. In this first part you create the project, open a window on the LWJGL 3 backend, connect JOID to it, and show a first UI. Each later part starts from the code of the previous one.

## What you will build

By the end of [Tutorial 4](polish.md), the application shows a settings card centered in the window:

- a header with an icon and the title "Settings";
- an **Audio** section with a **Music** on/off switch and a **Volume** slider;
- a **General** section with a **Notifications** switch;
- a **Language** list where one entry is selected;
- values that are saved to disk and restored on the next launch;
- hover animations, tooltips, rounded corners, a gradient background and an opening animation.

![The finished settings screen: a rounded dark card on a near-black to indigo gradient, with a header, Music and Notifications switches, a Volume slider at 80 % and a language list with English selected](../images/tutorial-overview.png "The settings screen at the end of Tutorial 4")

| Part | You learn |
| --- | --- |
| 1. Project Setup (this page) | The build, the backend, the UI bridge, the window loop, a first `UI`. |
| [2. Building the Layout](layout.md) | Nodes, `attach` and `body`, `FlexNode`, sizes and anchors, text and images. |
| [3. Interactivity and State](interactivity.md) | Callbacks, input controls, signals, `watch`, a persistent store. |
| [4. Polish](polish.md) | Colors and gradients, effects, hover animation, tooltips, a transition. |

The application has these classes, all in the package `com.example.settings`:

| Class | Role | Written in |
| --- | --- | --- |
| `Main` | Creates the window, registers the backend and the bridge, loads JOID and the font, runs the frame loop. | Part 1 |
| `AppUIBridge` | Hosts the open UIs and feeds them input. | Part 1 |
| `SettingsUI` | The settings screen. | Part 1, grows in every part |
| `ToggleSwitchNode`, `VolumeSliderNode` | The on/off switch and the slider. | Part 3 |
| `SettingsStore` | The settings values, saved to disk. | Part 3 |

## Step 1: create the project

Set up a Gradle project with the `joid-lwjgl3-8.0.0-dev.jar` jar, the shared libraries and the LWJGL 3 modules, exactly as in [Installation](../getting-started/installation.md#gradle-setup). The `-dev` jar contains the developer tools, which help while you build the screen; you switch to `-prod` when you ship.

Add the `application` plugin so that `gradle run` starts the program (Gradle 6.4 or later):

```groovy
plugins {
    id 'java'
    id 'application'
}

application {
    mainClass = 'com.example.settings.Main'
    if (System.getProperty('os.name').toLowerCase().contains('mac')) {
        applicationDefaultJvmArgs = ['-XstartOnFirstThread']
    }
}
```

Then add two files to the working directory of the program (the project folder when you use `gradle run`):

| File | Content |
| --- | --- |
| `fonts/Montserrat-Regular.ttf`, `fonts/Montserrat-Bold.ttf` | A regular and a bold face of any TrueType or OpenType family. The tutorial uses Montserrat; any family works if you adjust the file names in `Main`. |
| `icons/settings.png` | A 48×48 icon (PNG or SVG), shown in the header from part 2. |

JOID turns each font file into an MSDF atlas the first time it loads it, and caches the result; see [How Fonts Work](../fonts/how-fonts-work.md).

## Step 2: write the UI bridge

JOID never decides where a UI lives: a UI bridge does. `UIBridge` (`dev.joid.lib.bridge.ui`) already dispatches input, updates and draws its UIs; you only decide how UIs are added and removed. This bridge keeps every open UI in a list, the last one on top:

```java
package com.example.settings;

import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.IUIBridge;
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
        this.getUiList().add(ui);
        ui.load(window.getWidth(), window.getHeight());
    }

    @Override
    public void remove(final UI ui) {
        this.getUiList().remove(ui);
    }

    @Override
    public boolean isOnTop(final UI ui) {
        return this.getUiList().getLast() == ui;
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
    public IUIBridge getInstance() {
        return this;
    }

    @Override
    public void drawHover(final UI ui, final List<String> lines, final double mouseX, final double mouseY) {}

}
```

- `ui.load(width, height)` sizes the UI to the window and, the first time, runs its `init()`, where the UI builds its nodes.
- `drawHover` draws text tooltips for every UI of the bridge. This one draws nothing; in [part 4](polish.md) the settings screen draws its own tooltips.

See [UI Bridge](../integration/ui-bridge.md) for every method of a bridge.

## Step 3: write a first UI

A screen extends `UI` (`dev.joid.lib.ui.core`) and builds its nodes in `init()`. The settings screen receives the font from `Main` and, for now, shows only its title:

```java
package com.example.settings;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(backgroundColor = "#030712")
public final class SettingsUI extends UI {

    private final IFont font;

    public SettingsUI(final IFont font) {
        this.font = font;
    }

    @Override
    public void init() {
        TextNode.create(960, 540).text(Text.create("Settings", TextInfo.create(this.font, FontWeight.BOLD, 40, Color.WHITE))).anchor(Align.CENTER).attach(this);
    }

}
```

- `@UIData` (`dev.joid.lib.ui.core.data`) configures the UI class. `backgroundColor` fills the whole window before the UI draws; the default is a translucent dark gray.
- Positions are in units of the 1920×1080 virtual canvas: (960, 540) is its center, whatever the window size.
- `TextInfo.create(font, weight, size, color)` describes how the text looks; `Text.create(text, info)` is the text itself; `TextNode` displays it. `anchor(Align.CENTER)` makes (960, 540) the center of the text instead of its top-left corner.
- `attach(this)` adds the node to the UI.

## Step 4: create the window and run the frame loop

`Main` creates an OpenGL 3.3 core window with GLFW, registers the LWJGL 3 backend and your bridge, loads JOID and the font family, opens the settings screen and runs the loop. The GLFW callbacks forward the input to the bridge.

```java
package com.example.settings;

import java.io.File;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Platform;

import dev.joid.impl.glfw.WindowBridge;
import dev.joid.impl.lwjgl3.Backend;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.key.Key;

public final class Main {

    private final long window;
    private final AppUIBridge bridge;

    private Key pendingKey;
    private ClickType clickType;
    private long pressTime;

    private Main(final long window, final AppUIBridge bridge) {
        this.window = window;
        this.bridge = bridge;
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

        final MsdfFont font = MsdfFontLoader.load(new File("fonts/Montserrat-Regular.ttf"), new File("fonts/Montserrat-Bold.ttf")).join();

        final Main main = new Main(window, bridge);
        main.listen();
        main.resize();

        JOID.open(new SettingsUI(font));
        main.loop();
    }

    private void listen() {
        GLFW.glfwSetKeyCallback(this.window, (handle, code, scancode, action, mods) -> this.onKey(code, action, mods));
        GLFW.glfwSetCharCallback(this.window, (handle, codepoint) -> this.onCharacter(codepoint));
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
            this.flushPendingKey();

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

        this.flushPendingKey();
        final Key key = WindowBridge.getKey(code);
        final boolean text = code >= GLFW.GLFW_KEY_SPACE && code <= GLFW.GLFW_KEY_GRAVE_ACCENT || code >= GLFW.GLFW_KEY_KP_0 && code <= GLFW.GLFW_KEY_KP_ADD;
        if (text && (mods & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT)) == 0) {
            this.pendingKey = key;
            return;
        }

        this.bridge.keyTyped((char) 0, key);
    }

    private void onCharacter(final int codepoint) {
        final Key key = this.pendingKey == null ? Key.UNKNOWN : this.pendingKey;
        this.pendingKey = null;
        this.bridge.keyTyped((char) codepoint, key);
    }

    private void flushPendingKey() {
        if (this.pendingKey == null) {
            return;
        }

        final Key key = this.pendingKey;
        this.pendingKey = null;
        this.bridge.keyTyped((char) 0, key);
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

The startup order matters:

1. The OpenGL context is made current and `GL.createCapabilities()` is called before `Backend.register(window)`, because the LWJGL 3 render bridge creates GPU objects right away. `Backend.register` registers the window, render and audio bridges.
2. `BridgeHandler.UI.register(bridge)` makes your bridge the host of the UIs.
3. `JOID.inst().load()` runs once, after the bridges are registered and before any UI opens.
4. `MsdfFontLoader.load(...)` loads both faces as one family and returns a `CompletableFuture<MsdfFont>`; `join()` waits for it. A `TextInfo` that asks for `FontWeight.BOLD` draws the bold face, any other weight picks the closest face.
5. `resize()` sets a pixel projection and the viewport, then `bridge.load()` resizes every open UI. It runs again when the window is resized.
6. `JOID.open(ui)` hands the UI to the bridge, which loads it.

Each frame, the loop polls the input (forwarded to the bridge by the callbacks), calls `bridge.update()`, clears the screen and calls `bridge.draw()`. The key handling pairs each key press with the character GLFW reports right after it, so a text key reaches JOID once with both its `Key` and its character. The [Quick Start](../getting-started/quick-start.md#other-backends) shows the changes for the LWJGL 2 and Vulkan backends; the rest of the tutorial is identical on every backend.

## Step 5: run it

Run `gradle run`, or the `Main` class from your IDE (with `-XstartOnFirstThread` on macOS). The first launch takes a few seconds while the font atlases are generated; later launches read them from the cache.

You should see a window filled with near-black, with "Settings" in bold white at its center. Resize the window: the title stays centered and scales with it, because the UI is drawn on the 1920×1080 canvas fitted into the window. Press `Escape`: the screen closes, which is what a UI does by default (its `closeable` option is `true`), and only the gray clear color of the loop remains.

![A near-black window with the word Settings in bold white at its center](../images/tutorial-setup-window.png "The first UI: one centered text node on the UI background")

> TIP: With the `-dev` jar, start JOID with `JOID.inst().setDevMode(true).load()` instead of `JOID.inst().load()`. Then `F3` opens the node inspector and `Ctrl+R` or `F5` reloads the UI, which is handy while you follow the next parts. See [Developer Tools](../getting-started/dev-tools.md).

## Recap

- A JOID application is a host (here `Main` and GLFW) plus a backend that registers the window, render and audio bridges.
- You register a UI bridge, here `AppUIBridge`, that holds the open UIs; `JOID.open(ui)` goes through it.
- A screen is a subclass of `UI` that builds its nodes in `init()`, on a 1920×1080 canvas.
- `JOID.inst().load()` runs once at startup; fonts load in the background with `MsdfFontLoader`.

Next, [Tutorial 2: Building the Layout](layout.md) fills the screen with the settings card.

## See also

- [Installation](../getting-started/installation.md)
- [Quick Start](../getting-started/quick-start.md)
- [UI Bridge](../integration/ui-bridge.md)
- [The UI Class](../ui/ui-class.md)
- [Backends](../integration/backends.md)