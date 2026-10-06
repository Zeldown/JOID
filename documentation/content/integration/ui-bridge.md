# UI Bridge

The UI bridge connects your application loop to JOID: it holds the opened UIs, receives the mouse and keyboard events of your window, and updates and draws the UIs every frame. You write one by extending `UIBridge` (`dev.joid.lib.bridge.ui`), which already dispatches the events and draws the UIs in order; you decide how UIs open and close, which UI gets the hover, and how tooltips look.

## A complete UI bridge

```java
import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.bridge.ui.UIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.align.Align;

public class AppUIBridge extends UIBridge {

    private TextInfo tooltip;

    public AppUIBridge tooltip(final TextInfo tooltip) {
        this.tooltip = tooltip;
        return this;
    }

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
        return this.getUiList().getLast() == ui && ui.getData().active() && ui.getData().visible();
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
    public void drawHover(final UI ui, final List<String> lines, final double mouseX, final double mouseY) {
        if (this.tooltip == null || lines.isEmpty()) {
            return;
        }

        double width = 0D;
        for (final String line : lines) {
            width = Math.max(width, this.tooltip.getWidth(line));
        }
        width += 20D;

        final double lineHeight = this.tooltip.getHeight();
        final double height = 12D + lines.size() * lineHeight;
        final double x = Math.min(mouseX + 14D, ui.getView().toUiX(ui.getWidth()) - width - 4D);
        final double y = Math.min(mouseY + 14D, ui.getView().toUiY(ui.getHeight()) - height - 4D);

        DrawUtils.SHAPE.drawRoundedRect(x, y, width, height, Color.decode("#18181b"), 6F);
        for (int i = 0; i < lines.size(); i++) {
            DrawUtils.TEXT.drawText(x + 10D, y + 6D + i * lineHeight, lines.get(i), this.tooltip, Align.START, Align.START);
        }
    }

}
```

This bridge stacks the UIs: `JOID.open` puts a UI on top of the others, and `JOID.close` or ESC removes it. For a bridge that replaces the current UI, close the opened UIs in `open` before adding the new one (see [Opening and closing](#opening-and-closing)).

## Driving the bridge from your loop

Register the bridge after the backend, load JOID, then forward the events of your window and call `update()` and `draw()` once per frame. This loop uses GLFW with the [LWJGL 3 backend](backends.md#lwjgl-3):

```java
import org.lwjgl.glfw.GLFW;

import dev.joid.impl.glfw.WindowBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.key.Key;

public final class AppLoop {

    private final long        window;
    private final AppUIBridge bridge;

    private Key       pendingKey;
    private ClickType pressed;
    private long      pressTime;

    public AppLoop(final long window, final AppUIBridge bridge) {
        this.window = window;
        this.bridge = bridge;

        GLFW.glfwSetMouseButtonCallback(window, (handle, button, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                this.pressed = ClickType.from(button);
                this.pressTime = System.currentTimeMillis();
                this.bridge.mousePressed(this.pressed);
            } else if (this.pressed != null) {
                this.bridge.mouseReleased(this.pressed);
                this.pressed = null;
            }
        });
        GLFW.glfwSetCursorPosCallback(window, (handle, x, y) -> {
            if (this.pressed != null) {
                this.bridge.mouseDragged(this.pressed, System.currentTimeMillis() - this.pressTime);
            }
        });
        GLFW.glfwSetScrollCallback(window, (handle, x, y) -> this.bridge.mouseScroll((int) (y * 120D)));
        GLFW.glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> this.onKey(key, action, mods));
        GLFW.glfwSetCharCallback(window, (handle, codepoint) -> this.onCharacter(codepoint));
        GLFW.glfwSetFramebufferSizeCallback(window, (handle, width, height) -> this.resize());
        this.resize();
    }

    public void run() {
        while (!GLFW.glfwWindowShouldClose(this.window)) {
            GLFW.glfwPollEvents();
            this.flushPendingKey();

            this.bridge.update();
            BridgeHandler.RENDER.get().clear(0F, 0F, 0F, 1F);
            this.bridge.draw();
            GLFW.glfwSwapBuffers(this.window);
        }
    }

    private void resize() {
        final IWindowBridge windowBridge = BridgeHandler.WINDOW.get();
        if (windowBridge.getWidth() == 0 || windowBridge.getHeight() == 0) {
            return;
        }

        final IRenderBridge render = BridgeHandler.RENDER.get();
        render.ortho(0D, windowBridge.getWidth(), windowBridge.getHeight(), 0D, 0D, 10000D);
        render.viewport(0, 0, windowBridge.getWidth(), windowBridge.getHeight());
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
        if (this.pendingKey != null) {
            final Key key = this.pendingKey;
            this.pendingKey = null;
            this.bridge.keyTyped((char) 0, key);
        }
    }

}
```

```java
Backend.register(window);
final AppUIBridge bridge = new AppUIBridge();
BridgeHandler.UI.register(bridge);
JOID.inst().load();

final AppLoop loop = new AppLoop(window, bridge);
JOID.open(new UIMainMenu());
loop.run();
```

The `DemoWindow` classes of the backends contain the same loops for GLFW and for LWJGL 2 (see [Backends](backends.md#demo-windows)).

## Feeding input events

`UIBridge` provides the event methods; call them from the thread that draws.

| Method | When to call it | Argument |
|---|---|---|
| `mousePressed(ClickType clickType)` | A mouse button goes down. | `ClickType.from(button)` maps 0 to `LEFT`, 1 to `RIGHT`, 2 to `MIDDLE`, 3 to `BACK`, 4 to `FORWARD`, anything else to `OTHER`. |
| `mouseReleased(ClickType clickType)` | The button goes up. | The button that was pressed. |
| `mouseDragged(ClickType clickType, long delaTime)` | The mouse moves while a button is held. | The held button, and the milliseconds since it was pressed. |
| `mouseScroll(int value)` | The wheel turns. | Positive when the wheel turns away from the user, 120 per notch. `0` is ignored. Overflow scrolling uses the sign; the dev-mode zoom (Alt + wheel) uses the amount. |
| `keyTyped(char c, Key key)` | A key is pressed or repeats. | The character it types (`0` when none) and the engine-neutral `Key` (`Key.UNKNOWN` when unknown). |

- The mouse position is not an event: each UI reads `getMouseX()` and `getMouseY()` from the [window bridge](bridges.md#iwindowbridge) when it is drawn, and input events use the position of the last frame.
- Deliver the character and the key of a press together: text fields read the character, shortcuts read the key. GLFW reports them in two callbacks, which is why the loop above holds a text key until its character arrives.
- Each event goes to the UIs from the top one down. Inactive or hidden UIs are skipped. The dispatch stops at the first UI that cancels the event, and at the first UI that is an active [popup](../ui/managing-uis.md), so the UIs below a popup receive nothing.
- `keyTyped` with `Key.ESCAPE` tries to close each UI on its way, from the top: a closeable UI whose `onClose()` accepts is closed through `close(ui)` of the bridge, and the key goes no further; a UI that is not closeable, or that refuses, receives the key like any other.

See [Mouse and Keyboard](../interactions/mouse-and-keyboard.md) for what nodes do with these events.

## The frame: load, update and draw

| Method | Description |
|---|---|
| `load()` | Lays out every opened UI again at the size of the window bridge. Call it after a resize, once `ortho` and `viewport` match the new size. |
| `update()` | Calls the update of every opened UI, from the bottom one, including inactive and hidden UIs. |
| `draw()` | Draws every visible UI from the bottom one up. |

Before the first frame and after every resize, set an orthographic projection and a viewport covering the window, in pixels, with the origin at the top-left corner:

```java
render.ortho(0D, width, height, 0D, 0D, 10000D);
render.viewport(0, 0, width, height);
```

Each UI then draws in its own projection, fitted to the [1920×1080 virtual canvas](../ui/view-and-scaling.md). `draw()` stacks the UIs in depth: it translates the first one to `z = -2000` plus its `zlevel`, and each next one 10 units above the depth reached by the previous UI, plus its own `zlevel`. An exception thrown while drawing is printed and stops the drawing of that frame; the matrix stack is restored.

The UI list is ordered by `zlevel` (`@UIData`, compared on its integer part), then by the order in which UIs were added: a UI with a higher `zlevel` is drawn above and receives the events first.

## Methods you implement

| Method | Called by | What it must do |
|---|---|---|
| `open(UI ui)` | `JOID.open(ui)`, and `JOID.open(ui, true)` after it closed every UI of the bridge | Decide how the UI joins the others, then `add(ui)`. |
| `close(UI ui)` | `JOID.close(ui)` once `ui.onClose()` accepted, ESC, and a UI whose out transition ends | Take the UI out, usually with `remove(ui)`. The UI is already cleaned up. |
| `add(UI ui)` | your `open`, and tools such as the [testkit](testkit.md) | Add the UI to `getUiList()` and call `ui.load(width, height)` with the window size: a UI that was never loaded ignores every event. |
| `remove(UI ui)` | your `close` | Remove the UI from `getUiList()`. |
| `isOnTop(UI ui)` | every UI, at every frame | `true` for the UI whose nodes may be hovered and show tooltips. |
| `canHandle(UI ui)` / `canHandle(Class<? extends UI> clazz)` | `BridgeHandler.UI.get(...)` | `true` for the UIs this bridge hosts. |
| `getInstance()` | — | Return the bridge itself. |
| `drawHover(UI ui, List<String> lines, double mouseX, double mouseY)` | a hovered node with a text tooltip | Draw the lines next to the mouse. |
| `getInterfaceScale(UI ui)` | every UI, at every frame | Optional. See [Interface scale](#interface-scale). |
| `getIndex()` | the UI registry | Optional. See [Several UI bridges](#several-ui-bridges). |

`UIBridge` also implements `getUiList()`, which returns the ordered list of UIs, and `isOpened(UI ui)`, which tells whether a UI is in it.

### Opening and closing

`JOID.close(ui)` first calls `ui.onClose()`: the UI can refuse, and a UI with an out [transition](../ui/transitions.md) starts it and calls `close(ui)` of its bridge itself when it ends. Your `close` only takes the UI out of the list. `JOID.close(ui, true)` skips the checks.

To replace the current UI instead of stacking, close the others in the `open` method of your bridge, unless the new UI is a popup (this needs `java.util.ArrayList`):

```java
@Override
public void open(final UI ui) {
    if (!ui.getPopup().active()) {
        for (final UI opened : new ArrayList<>(this.getUiList().ordered())) {
            if (!opened.onClose()) {
                return;
            }
            this.close(opened);
        }
    }
    this.add(ui);
}
```

A UI with an out transition starts it and returns `false` from `onClose()`, so this `open` stops there; the UI closes itself when the transition ends. The demo bridge (`DemoUIBridge`) goes further and opens the new UI from the end callback of the transition.

## Tooltips with drawHover

A node with text tooltips calls `drawHover` of its UI's bridge while it is hovered and its UI is on top. The call happens inside the UI's drawing:

- coordinates are in UI units, so `mouseX` and `mouseY` are the mouse position in the UI;
- the depth test is disabled, and the render state is restored afterwards;
- `ui.getView().toUiX(...)` and `toUiY(...)` convert window pixels, for example `ui.getWidth()`, into UI units to keep the tooltip inside the window.

Tooltips made of nodes (`NodeHoverElement`, `CustomHoverElement`) draw themselves and do not call `drawHover`. See [Hover and Tooltips](../interactions/hover.md).

## Interface scale

A host that lets its users scale the interface (a game GUI scale, an accessibility setting) returns a factor from `getInterfaceScale(UI)`. `1` is the normal size, where the virtual canvas fits the window; `0.5` draws the UI at half that size around its anchor, and shows twice as much of it.

```java
@Override
public double getInterfaceScale(final UI ui) {
    return ui instanceof UIHud ? this.settings.getHudScale() : 1D;
}
```

Every UI asks at each frame and updates its view, its mouse conversion and its `scaledWidth` and `scaledHeight` signals when the value changes. The scale combines with the zoom of the UI, whose maximum becomes `1 / scale` when the scale is below 1. Keep passing the real window size and mouse position: never scale them yourself. See [View and Scaling](../ui/view-and-scaling.md).

## Several UI bridges

An application can host UIs in several places, for example menus in the main window and panels inside a game world. Register one bridge per place; `canHandle` routes each UI to its bridge:

```java
public class WorldUIBridge extends AppUIBridge {

    @Override
    public boolean canHandle(final UI ui) {
        return ui instanceof WorldPanel;
    }

    @Override
    public boolean canHandle(final Class<? extends UI> clazz) {
        return WorldPanel.class.isAssignableFrom(clazz);
    }

}
```

```java
BridgeHandler.UI.register(new AppUIBridge());
BridgeHandler.UI.register(new WorldUIBridge());

JOID.open(new ShopPanel());
JOID.open(new UISettings());
```

`ShopPanel`, a `WorldPanel`, goes to the `WorldUIBridge`; `UISettings` goes to the `AppUIBridge`. When several bridges handle a UI, the one with the highest `getIndex()` wins, then the latest registered. `canHandle(Class)` answers the lookups by class: `JOID.getUI(Class)` and `JOID.isOpen(Class)`. When no bridge handles a UI, `JOID.open` throws an `IllegalStateException` (`No IUIBridge can open ShopPanel: register one whose canHandle accepts it`). Each bridge runs its own loop: forward events and call `update()` and `draw()` on each of them where they belong.

## See also

- [Bridges](bridges.md) — the registries and the other bridges.
- [Opening and Closing UIs](../ui/managing-uis.md) — `JOID.open`, `JOID.close`, popups and `zlevel`.
- [View and Scaling](../ui/view-and-scaling.md) — virtual canvas, zoom and interface scale.
- [Backends](backends.md) — window setup and demo loops of each engine.