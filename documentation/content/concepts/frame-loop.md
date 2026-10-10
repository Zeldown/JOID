# Frame Loop and Dev Tools

JOID has no loop of its own: your program, or the engine that hosts JOID, owns the window and the loop and calls your UI bridge once per frame. This page also covers the developer tools.

```java
while (!GLFW.glfwWindowShouldClose(window)) {
	GLFW.glfwPollEvents();
	bridge.frame();
	GLFW.glfwSwapBuffers(window);
}
```

![The host forwards the input, then bridge.frame() runs update(), beginFrame(), drawBackground(), draw() of the UIs from the lowest zindex, and endFrame(), then the next frame starts](../images/diagram-frame-loop.png "One frame: the input first, then bridge.frame()")

`glfwPollEvents()` runs the window callbacks, which forward the input to the bridge; `bridge.frame()` updates every open UI and draws them. `bridge` is your UI bridge, which holds the open UIs (see [UIs](uis.md)).

## Startup order

Before the first frame, prepare JOID once, in this order:

1. **Graphics context.** Create the window and make its OpenGL context current, or create the Vulkan window.
2. **Backend.** `Backend.register(window)` registers the window, render and audio bridges.
3. **UI bridge.** `BridgeHandler.UI.register(bridge)` registers the object that holds your UIs.
4. **JOID.** `JOID.inst().load()` applies the settings and prints the JOID banner.
5. **Fonts.** Load the fonts of your UIs, after JOID.
6. **Size.** `bridge.resize(width, height)` sets the viewport and fits the UIs; call it again when the framebuffer size changes.
7. **First UI.** `JOID.open(ui)` hands the UI to the bridge, which loads it and runs `init()`.

Set the global settings on `JOID.inst()` before `load()`:

```java
JOID.inst().setConfigDir(new File("run/config")).setDevMode(true).load();
```

| Method | Description |
| --- | --- |
| `setDevMode(boolean)` | Turns the developer tools on. Default `false`; `-dev` jar only. |
| `setConfigDir(File)` | Folder of the stores (`<dir>/store`) and UI properties (`<dir>/property`). Default: the `joid.config` system property, else `config`. |
| `setDemoMode(boolean)` | Loads the fonts of the demo UIs. Default `false`; `-dev` jar only. |
| `load()` | Prints the banner and loads the bundled fonts of the dev and demo modes. |

## One frame: input, update, draw

1. **Input.** The host forwards each event as it arrives: `keyPressed(Key)`, `charTyped(int)`, `mousePressed(MouseButton)`, `mouseReleased(MouseButton)`, `mouseMoved()` and `mouseScroll(double, double)`. The bridge offers it to its UIs from the top down; inside a UI the front-most node gets it first, then the UI hooks.
2. **Update.** `frame()` calls `update()` on the nodes of each UI, then the `update()` hook of the UI.
3. **Draw.** Between `beginFrame()` and `endFrame()` of the render bridge, `frame()` runs `drawBackground()`, then draws each visible UI from the lowest `zindex` up: its due scheduled tasks, its background, then its nodes.

A setter that follows a signal shows the new value at the next frame after the change.

Override `drawBackground()` in your bridge to clear the screen or draw behind every UI, as in the [Quick Start](../getting-started/quick-start.md):

```java
@Override
protected void drawBackground() {
	final IRenderBridge render = BridgeHandler.RENDER.get();
	render.clearColor(0.1F, 0.1F, 0.1F, 1F);
	render.clearDepth();
	render.clearStencil();
}
```

## Time from the clock bridge

Animations, hover blends, scheduled tasks and video read the clock of `BridgeHandler.CLOCK`, in milliseconds. Read time through it too, so your animations follow the same clock, including the manual clock of tests:

```java
final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
```

## One thread for everything

JOID is not thread-safe. Forward the input, call `frame()` and change nodes and followed signals from the thread that owns the graphics context. From another thread, hand the work to the UI with `schedule(runnable)`; the task runs at the start of the next draw:

```java
CompletableFuture.supplyAsync(() -> "Loaded").thenAccept(result -> this.schedule(() -> this.status.set(result)));
```

`status` is a `Signal<String>` field of the UI.

## Dev mode

With the `-dev` jar, `setDevMode(true)` adds an inspector panel, reload shortcuts, hot reload, a profiler and warnings that explain what JOID could not do.

![A settings card inspected with the dev panel: the hovered row is outlined in blue, a click locks it and the panel shows its details](../images/dev-inspect.gif "F3 shows the panel; hover a node to outline it, click to lock it and read its details")

| Shortcut | Effect |
| --- | --- |
| `F3` | Shows or hides the dev panel. |
| `Ctrl+R` or `F5` | Reloads the UI: `init()` runs again on the same instance, fields and signals are kept. |
| `Ctrl+Shift+R` or `Shift+F5` | Renews the UI: a new instance from the no-argument constructor replaces it. |
| Left `Alt` + wheel | Zooms by small steps; hold left `Shift` for larger ones. |
| `I`, `R`, `G` (panel shown) | Toggle Inspect, reload, toggle the grid. |
| `Enter` (panel shown) | Selects the parent of the inspected node. |

With Inspect on, a left click locks the outlined node and lists its bounds, callbacks, hierarchy and fields; a right click unlocks it. The grid measures distances in canvas units.

![The grid: blue center lines and the distances from the mouse to the four edges of the canvas](../images/dev-grid.png "The grid measures in canvas units, whatever the window size")

**Hot reload.** A UI reloads itself when a class of its folder or jar changes. JOID loads no new bytecode: run in debug mode so the HotSwap of your IDE replaces the method bodies.

**Profiler.** Each load prints its duration, and every frame of a UI that takes more than 16.66 ms prints `[!] Frame took <ms>ms to render`. Turn either off per UI:

```java
@UIDataDebug(profiler = false, hotreload = false)
public final class MenuUI extends UI {}
```

**Warnings.** JOID prints `[JOID] ...` on `System.err`, once per case, when it cannot do what your code asks: a setter that cannot follow its signals, an unavailable shader, an unreadable resource (drawn as a magenta and black checkerboard), a missing font weight.

## The demos

Each backend `-dev` jar contains a demo window, `dev.joid.backend.<backend>.demo.DemoWindow`, that opens a menu of demo UIs, one per topic. Run it from the repository with `./gradlew :backend-lwjgl3:runDemo`; its sources in `dev.joid.demo` are working examples.

![The demo menu: a grid of gray buttons, one per demo UI](../images/dev-demo-menu.png "Click a demo to open it; Escape goes back to the menu")

## Good to know

- `JOID.open(ui)` before `BridgeHandler.UI.register(bridge)` throws: no bridge accepts the UI yet.
- Changing a node or a followed signal from another thread can corrupt a frame: go through `schedule(...)`.
- `Ctrl+R` keeps fields and signals: use `Ctrl+Shift+R` to start from a fresh instance.

## See also

- [Quick Start](../getting-started/quick-start.md)
- [UIs](uis.md)
- [Input](input.md)
- [Bridges and Backends](../integration/backends.md)
- [Embedding JOID in an Application](../integration/ui-bridge.md)