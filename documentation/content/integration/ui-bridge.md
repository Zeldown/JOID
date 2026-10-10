# Embedding JOID in an Application

The UI bridge connects your application loop to JOID: it holds the open UIs, receives input, and updates and draws the UIs every frame. You write one by extending `UIBridge` (`dev.joid.lib.bridge.ui`), which already dispatches input, draws the UIs in order and tracks the top UI; you decide how UIs open and close.

```java
public class AppUIBridge extends UIBridge {

	@Override
	public void open(final @NonNull UI ui) {
		this.add(ui);
	}

	@Override
	public void close(final @NonNull UI ui) {
		this.remove(ui);
	}

	@Override
	public void add(final @NonNull UI ui) {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		super.getUiList().add(ui);
		ui.load(window.getWidth(), window.getHeight());
	}

	@Override
	public void remove(final @NonNull UI ui) {
		super.getUiList().remove(ui);
	}

	@Override
	public boolean canHandle(final @NonNull UI ui) {
		return true;
	}

	@Override
	public boolean canHandle(final @NonNull Class<? extends UI> clazz) {
		return true;
	}

	@Override
	protected void drawBackground() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.clearColor(0.1F, 0.1F, 0.1F, 1F);
		render.clearDepth();
		render.clearStencil();
	}

}
```

This bridge stacks UIs: `JOID.open(ui)` puts a UI on top of the others, and `JOID.close(ui)` or Escape removes it. Register it after the backend and before `JOID.inst().load()`:

```java
final AppUIBridge bridge = new AppUIBridge();
BridgeHandler.UI.register(bridge);
JOID.inst().load();
```

![Events go from the top UI down and stop at the first UI that consumes them; Escape reaches the top closeable UI before it closes](../images/diagram-ui-bridge-dispatch.png "Input goes down the UI list, top UI first")

## Run a frame with frame() and resize()

`frame()` runs one whole frame: `update()`, then `beginFrame()`, the `drawBackground()` hook, `draw()`, and `endFrame()` in a `finally`. Call `resize(width, height)` once before the first frame and after every window resize: it sets the render target to the window and lays out every UI again.

```java
GlfwInputForwarder.create(bridge).attach(window);
bridge.resize(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
while (!GLFW.glfwWindowShouldClose(window)) {
	GLFW.glfwPollEvents();
	bridge.frame();
	GLFW.glfwSwapBuffers(window);
}
```

![The host polls the window events, the input forwarder calls the bridge, bridge.frame() updates and draws, the host swaps the buffers; bridge.resize runs before the first frame and on each resize](../images/diagram-ui-bridge-loop.png "The application loop around frame() and resize()")

`drawBackground()` draws nothing by default: override it to clear the window, or leave it empty to draw the UIs over your own scene.

## Forward input with an input forwarder

The input forwarders translate the events of a windowing library. `GlfwInputForwarder` (`dev.joid.base.glfw.input`) registers the GLFW callbacks of a window with `attach(window)`. `Lwjgl2InputForwarder` (`dev.joid.backend.lwjgl2.input`) reads the LWJGL 2 queues with `poll()` once per frame. Both send each key to `keyPressed`, then its character to `charTyped`.

A host that owns its callbacks calls the bridge itself. Each input method returns `true` when a UI consumed the event; forward the event to the host only when it returns `false`:

```java
if (!this.bridge.mousePressed(MouseButton.from(button))) {
	this.game.mousePressed(button);
}
```

| Method | Call it when |
|---|---|
| `mousePressed(MouseButton button)` / `mouseReleased(MouseButton button)` | A button goes down or up. `MouseButton.from(int)` maps 0, 1, 2 to `LEFT`, `RIGHT`, `MIDDLE`. |
| `mouseMoved()` | The mouse moves. It drags while a button is held; the position itself is read from the window bridge every frame. |
| `mouseScroll(double notchesX, double notchesY)` | The wheel turns, in notches (`1` per notch away from the user). Divide Windows and LWJGL 2 values by `120`. |
| `keyPressed(Key key)` | A key is pressed or repeats. |
| `charTyped(int codepoint)` | A character is typed, one call per code point. Control characters are ignored. |

## Escape and closing

`keyPressed(Key.ESCAPE)` first reaches the top closeable UI: its nodes, keybinds and focused text field can consume it. When none does, the bridge asks the UI to close and calls your `close(ui)` once it accepts. `JOID.close(ui)` follows the same path; `JOID.close(ui, true)` skips the checks.

## Replace screens with StackUIBridge

To replace the current UI instead of stacking, extend `StackUIBridge`. It implements `open`, `close`, `add`, `remove` and `canHandle`: `open(ui)` asks the open UIs to close before it adds `ui`, except for popups and overlays. Return `false` from `canReplace(ui)` for a base screen that stays under the others:

```java
public class GameUIBridge extends StackUIBridge {

	@Override
	public boolean canReplace(final @NonNull UI ui) {
		return !(ui instanceof InventoryUI);
	}

}
```

Override `attachScreen()` and `detachScreen()` to show and hide the screen of your host when the first UI opens and the last one closes.

## Tooltips with drawHover

JOID has no tooltip style of its own: a hovered node with a text tooltip calls `drawHover` of its bridge, and `UIBridge` draws nothing. Override it to draw tooltips in the look of your application. Coordinates are canvas units, and `TextConverter.convertLines(content)` gives the lines:

```java
@Override
public void drawHover(final @NonNull UI ui, final @NonNull Object content, final double mouseX, final double mouseY) {
	final List<String> lines = TextConverter.convertLines(content);
	for (int i = 0; i < lines.size(); i++) {
		DrawUtils.TEXT.drawText(mouseX + 14D, mouseY + 14D + i * 22D, lines.get(i), this.info, Align.START, Align.START);
	}
}
```

Here `info` is a `TextInfo` field of your bridge. An engine with its own tooltip objects receives them in `content` as well.

## Interface scale with getInterfaceScale

A host with a GUI scale setting returns it from `getInterfaceScale(ui)`. `1` is the normal size; `0.5D` draws the UI at half that size and shows twice as much canvas. Keep passing the real window size and mouse position.

```java
@Override
public double getInterfaceScale(final @NonNull UI ui) {
	return 0.75D;
}
```

## Several UI bridges

An application can host UIs in several places, such as menus in the window and panels in a game world. Register one bridge per place; `canHandle` routes each UI to its bridge:

```java
public class WorldUIBridge extends AppUIBridge {

	@Override
	public boolean canHandle(final @NonNull UI ui) {
		return ui instanceof WorldPanel;
	}

	@Override
	public boolean canHandle(final @NonNull Class<? extends UI> clazz) {
		return WorldPanel.class.isAssignableFrom(clazz);
	}

}
```

```java
BridgeHandler.UI.register(new AppUIBridge());
BridgeHandler.UI.register(new WorldUIBridge());
```

When several bridges accept a UI, the one with the highest `getIndex()` wins, then the latest registered. Each bridge needs its own input and frame calls.

## Overlays and the host

Overlays (`@UIDataOverlay`) draw above the other UIs and receive input first. Override `isScreenOpen()` to also count the screens of your host, and `isOverlayHidden()` to return `true` while the host hides its interface. `draw(filter)` draws only the UIs the filter accepts, for a host that draws overlays in several layers.

## Reference

| Method | Description |
|---|---|
| `open(UI)` / `close(UI)` | You implement: what `JOID.open` and `JOID.close` do. |
| `add(UI)` / `remove(UI)` | You implement: put the UI in `getUiList()` and call `ui.load(width, height)`, or take it out. |
| `canHandle(UI)` / `canHandle(Class)` | You implement: `true` for the UIs this bridge hosts. |
| `frame()` / `resize(int width, int height)` | One whole frame; the window size changed. |
| `update()` / `draw()` / `draw(Predicate<UI> filter)` | Update every UI, draw the visible ones, bottom up. |
| `load()` | Lay out every UI again at the window size, keeping its zoom. |
| `drawBackground()` | Protected hook of `frame()` before the UIs; nothing by default. |
| `drawHover(UI, Object, double, double)` | Draw a tooltip; nothing by default. |
| `getInterfaceScale(UI)` | Scale factor of a UI, `1` by default. |
| `getIndex()` | Priority among UI bridges, `0` by default. |
| `isOnTop(UI)` / `isOpen(UI)` | Whether the UI receives hover, whether it is in the list. |
| `isScreenOpen()` / `isOverlayHidden()` | Overlay visibility rules for a host. |

## Good to know

- `add` must call `ui.load(width, height)`: a UI that was never loaded ignores input and draws nothing.
- Call every method of the bridge from the thread that owns the graphics context.
- `JOID.open(ui)` throws an `IllegalStateException` when no registered bridge accepts the UI.

## See also

- [Bridges and Backends](backends.md)
- [Writing a Backend](writing-a-backend.md)
- [UIs](../concepts/uis.md)
- [Input](../concepts/input.md)
- [Canvas and Scaling](../concepts/canvas.md)