# The UI Class

`UI` (`dev.joid.lib.ui.core.UI`) is the root of a screen: it owns a tree of nodes, receives input from its bridge, and draws itself on the 1920×1080 virtual canvas. Extend it for each screen of your application, build its nodes in `init()`, and configure it with the `@UIData` annotation.

## A minimal UI

```java
@UIData(backgroundColor = "#000000A0", zoomable = false)
public final class MenuUI extends UI {

    @Override
    public void init() {
        RectNode.create(760, 400, 400, 280).color(Color.DARKGRAY).attach(this);

        this.keybind(() -> JOID.close(this), Key.Q, Key.LEFT_CONTROL);
    }

    @Override
    public void update() {
        if (this.getFps() > 0D && this.getFps() < 30D) {
            System.err.println("Slow menu: " + this.getFps() + " fps");
        }
    }

}
```

Open it with `JOID.open(new MenuUI())`; see [Opening and Closing UIs](managing-uis.md). `UI` has a public no-argument constructor and no abstract method: override only the hooks you need.

## Lifecycle

| Stage | Trigger | What happens |
| --- | --- | --- |
| Construction | `new MyUI()` | Reads `@UIData`, `@UIDataDebug` and `@UIDataPopup` from the class or its nearest annotated superclass, creates the view and, for a popup, the default transition. No node exists yet. |
| First load | The bridge calls `load(width, height)`, usually from `JOID.open` | Sizes the view to the window, restores the [`@UIProperty`](../state/properties.md) fields, clears any node, keybind or task added before, runs `init()`, marks the UI initialized, then starts the In state of its transition. In dev mode, also sets up the inspector, the profiler and hot reload. |
| Frames | The bridge | Input hooks, then `update()`, then the draw hooks. See [Core Concepts](../getting-started/core-concepts.md#the-frame-lifecycle). |
| Resize | The bridge calls `load(width, height)` again | Resizes the view only; `init()` does not run again. |
| Reload | `reload()`, dev shortcuts, hot reload | Detaches every node, clears the keybinds and scheduled tasks, restores the `@UIProperty` fields, runs `init()` again and replays the In transition. The zoom is kept. |
| Close | `JOID.close(ui)`, `Escape`, the bridge | `onClose()` asks your `close()` hook; if it agrees, plays the Out transition, then `properlyClose()` releases the UI and the bridge removes it. |

Everything you add in the constructor (nodes, keybinds, tasks) is discarded by the first load: add them in `init()`. During `init()`, `UI.getCurrent()` returns the UI being initialized; it returns `null` the rest of the time.

`properlyClose()` detaches every node, stops the hot reload watcher, saves every store, destroys the local stores of the UI and saves its `@UIProperty` fields. `JOID.close` and the out transition call it; call it yourself only from a bridge that removes a UI without going through `JOID.close`.

## Overridable hooks (IUI)

`UI` implements `IUI` (`dev.joid.lib.ui.core.IUI`), whose methods all have empty defaults. Mouse coordinates are in canvas units.

| Hook | Called |
| --- | --- |
| `void init()` | On the first load and on each reload. Build the nodes, keybinds and tasks here. |
| `boolean close()` | When the UI is asked to close (`JOID.close`, `Escape`, a bridge). Return `false` to keep it open. Default `true`. Not called by `JOID.close(ui, true)` nor `JOID.open(ui, true)`. |
| `void update()` | Every frame from `UIBridge.update()`, after the nodes have updated. |
| `void drawBackground(double mouseX, double mouseY)` | Every frame, after the `@UIData` background and before the transition and the view transform: it draws in the host's coordinate space (window pixels with the projection of the [Quick Start](../getting-started/quick-start.md)), not on the canvas. |
| `void preDraw(double mouseX, double mouseY)` | Inside the view, after the nodes with a negative `zindex` and before the nodes with a `zindex` from 0 to 99. |
| `void postDraw(double mouseX, double mouseY)` | Inside the view, after the nodes with a `zindex` from 0 to 99 and before the nodes with a `zindex` of 100 or more. |
| `void mousePressed(double mouseX, double mouseY, ClickType clickType, InternalContext context)` | After the nodes received the press (those with a positive `zindex` first). |
| `void mouseDragged(double mouseX, double mouseY, ClickType clickType, long deltaTime, InternalContext context)` | After the nodes received the drag. `deltaTime` is given by the host (milliseconds since the press in the bundled demo windows). |
| `void mouseReleased(double mouseX, double mouseY, ClickType clickType, InternalContext context)` | After the nodes received the release. |
| `void mouseScroll(double mouseX, double mouseY, int value, InternalContext context)` | After the nodes received the scroll. |
| `void keyPressed(char c, Key key, InternalContext context)` | Last, after the nodes, the keybinds, the zoom keys and the dev keys. |

The input hooks are always called, even when a node already consumed the event: check `context.isCancelled()` before acting, and call `context.cancel()` to consume the event so the UIs below do not receive it. See [Mouse and Keyboard](../interactions/mouse-and-keyboard.md).

```java
@Override
public void keyPressed(final char c, final Key key, final InternalContext context) {
    if (!context.isCancelled() && key == Key.TAB) {
        context.cancel(() -> JOID.close(this));
    }
}
```

`draw`, `onUpdate`, `onClose`, `load` and the `onMouseXxx`/`onKeyPressed` methods are `final`: they are the entry points bridges call, and they run the hooks above.

## Configuring a UI with @UIData

`@UIData` (`dev.joid.lib.ui.core.data`) sets the options of a UI class. JOID looks for it on the class, then on its superclasses, and uses the first one found (attributes are not merged).

```java
@UIData(zlevel = 10D, background = false, closeable = false, anchorX = Align.END, anchorY = Align.START)
public final class HudUI extends UI {}
```

| Attribute | Default | Effect |
| --- | --- | --- |
| `active` | `true` | When `false`, `UIBridge` sends the UI no input; it is still updated and drawn. |
| `visible` | `true` | When `false`, the UI is neither drawn nor sent input; it is still updated. |
| `closeable` | `true` | When `true`, `Escape` closes the UI. `JOID.close` works either way. |
| `zoomable` | `true` | Enables the `Ctrl`/`Alt` + `+`/`-` zoom keys. |
| `background` | `true` | Fills the whole window with `backgroundColor` before drawing the UI. |
| `backgroundColor` | `"#101010c0"` | Any string accepted by `Color.decode`: `#RRGGBB`, `#RRGGBBAA`, `rgb(...)`, `rgba(...)`, `gradient(...)`. See [Colors and Gradients](../styling/colors.md). |
| `projection` | `true` | When `true`, the UI sets its own orthographic projection for the canvas. When `false`, it draws with the projection the host has set. |
| `zlevel` | `0D` | Order among the UIs of a bridge: higher is drawn later and receives input first. Also offsets the depth of the UI. |
| `anchorX` | `Align.CENTER` | Horizontal anchor of the canvas in the window and pivot of the scaling. See [View and Scaling](view-and-scaling.md). |
| `anchorY` | `Align.CENTER` | Vertical anchor. |
| `pause` | `true` | Not read by JOID. A host bridge can read `ui.getData().pause()` to pause its own simulation while the UI is open. |

`getData()` returns the options as a `UIDataObject`, whose setters (`setActive`, `setVisible`, `setCloseable`, `setZoomable`, `setBackground`, `setBackgroundColor(String)`, `setProjection`, `setZlevel`, `setPause`, `setAnchorX`, `setAnchorY`) change them at runtime and return the object. `getBackgroundColor()` returns the decoded `Color`, and `getAnchorPositionX()`/`getAnchorPositionY()` the anchor in canvas units (0, 960 or 1920; 0, 540 or 1080).

```java
this.getData().setCloseable(false).setBackground(false);
```

> NOTE: The view reads the anchors when the UI is constructed, and the bridge orders its UIs when it adds them: change `anchorX`, `anchorY` and `zlevel` through the annotation, or before opening the UI.

The other annotations of a UI are [`@UIDataPopup`](managing-uis.md#popups-with-uidatapopup) and [`@UIDataDebug`](../getting-started/dev-tools.md#profiler).

## Adding nodes

`add(Node... nodes)` loads each node into the UI and adds it to the top-level node list; `node.attach(ui)` does the same. Top-level nodes are kept in `getNodeList()`, an `IndexedConcurrentList<Node>` sorted by `zindex`. See [Node Fundamentals](../nodes/node-fundamentals.md).

```java
this.add(RectNode.create(0, 0, 1920, 80).color(Color.BLACK), RectNode.create(0, 1000, 1920, 80).color(Color.BLACK));
```

## Keybinds with keybind

`keybind(Runnable runnable, Key... keys)` runs `runnable` when a key is pressed while all `keys` are down:

```java
this.keybind(() -> JOID.open(new SettingsUI()), Key.S, Key.LEFT_CONTROL);
```

- Keybinds are checked after the nodes, only if no node consumed the key. A matching keybind consumes the key; every matching keybind runs.
- Each call adds a keybind. The keybinds are cleared before each `init()`, so register them in `init()`.
- `Escape` closes a closeable UI before the keybinds see it; bind it only on a UI with `closeable = false`, or one whose `close()` returns `false`.
- `getKeybindMap()` returns the registered keybinds.

## Scheduled tasks with schedule

| Method | Runs `runnable` |
| --- | --- |
| `schedule(Runnable runnable)` | Once, at the next draw of the UI. |
| `schedule(Runnable runnable, long delay)` | Once, at the first draw after `delay` milliseconds. |
| `schedule(Runnable runnable, long delay, long period)` | At the first draw after `delay` ms, then at the first draw after each `period` ms. A `period` of `0` runs it on every draw. |

```java
this.schedule(() -> System.out.println("Two seconds after init"), 2000L);
this.schedule(() -> System.out.println("Every second"), 0L, 1000L);
```

Tasks run at the start of `draw`, on the thread that draws the UI, and are timed with the clock bridge. The task list is thread-safe, so `schedule` is the way to hand work from another thread to the UI. Tasks are cleared before each `init()`. `getScheduledTaskList()` returns the pending `UIScheduledTask` objects.

## Reloading with reload

`reload()` rebuilds the UI: it detaches all nodes, clears keybinds and tasks, runs `init()` again and replays the In transition, keeping the size and zoom. Prefer [watching signals](../state/watch.md) to rebuild only the nodes that depend on a value.

## Masks with mask and startMask

Masks clip drawing to a rectangle or to the opaque pixels of a resource, using the stencil buffer. Use them inside draw hooks or custom nodes:

```java
@Override
public void postDraw(final double mouseX, final double mouseY) {
    this.mask(100, 100, 400, 200, () -> DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.RED, 80D));
}
```

| Method | Description |
| --- | --- |
| `mask(double x, double y, double width, double height, Drawing drawing)` | Runs `drawing` clipped to the rectangle. |
| `mask(double x, double y, double width, double height, Drawing drawing, boolean enabled)` | Same; when `enabled` is `false`, runs `drawing` without clipping. |
| `mask(Resource resource, double x, double y, double width, double height, Drawing drawing)` | Runs `drawing` clipped to the pixels of `resource`, drawn in the rectangle, whose alpha is at least 0.5. |
| `mask(Resource resource, double x, double y, double width, double height, Drawing drawing, boolean enabled)` | Same, with the `enabled` switch. |
| `startMask(double x, double y, double width, double height)` | Starts a rectangle mask; everything drawn until `stopMask()` is clipped. |
| `startMask(Resource resource, double x, double y, double width, double height)` | Starts a resource mask. |
| `stopMask()` | Ends the last started mask. Throws `EmptyStackException` when no mask is active. |

Masks nest: an inner mask clips to the intersection with the outer ones. The `mask(...)` methods stop their mask even when `drawing` throws. `Drawing` is the functional interface `dev.joid.lib.render.context.Drawing` (`void draw()`). For a mask on a node, use [`MaskNodeEffect`](../styling/mask.md).

## Smoothing values with lerpByFramerate

`lerpByFramerate(double value, double target, double speed, double snapDiff, boolean snap)` moves `value` towards `target` by a frame-rate independent step: at 60 fps and `speed` 1, a third of the remaining distance per frame, never past the target. When the distance is `snapDiff` or less, it returns `target` if `snap` is `true`, `value` otherwise. Before the first frame, it returns `value`.

With `panelX` a `double` field and `open` a `boolean` field of the UI:

```java
@Override
public void update() {
    this.panelX = this.lerpByFramerate(this.panelX, this.open ? 0D : -400D, 1D, 0.5D, true);
}
```

## Mouse, frame time and frame rate

| Getter | Returns |
| --- | --- |
| `double getMouseX()`, `double getMouseY()` | The mouse position in canvas units, as sampled at the last draw. |
| `double getFrameTime()` | Duration of the last frame in milliseconds, from the clock bridge (`1000/60` on the first frame, `0` before). |
| `double getFps()` | Frames drawn per second, updated once per second (`0` during the first second). |
| `long getRenderTime()` | Duration of the last `draw` in nanoseconds. |
| `long getLastFrame()` | Clock time of the last frame, in nanoseconds. |
| `boolean isOnTop()` | Whether the bridge reported the UI as the top one at the last draw. |
| `boolean isInitialized()` | Whether `init()` has run. |

`UI.isCtrlKeyDown()`, `UI.isShiftKeyDown()` and `UI.isAltKeyDown()` are static helpers that return `true` when the left or right modifier is down; see [Mouse and Keyboard](../interactions/mouse-and-keyboard.md).

## Stores and properties

- `useStore(Class<T> clazz, Object... args)` returns the [store](../state/stores.md) of the given class, creating it with `args` the first time. A store with a local context is kept per UI and destroyed when the UI closes; other stores are shared.
- Fields annotated with [`@UIProperty`](../state/properties.md) are restored before each `init()` and saved when the UI closes, in `<configDir>/property/<class name>.property`.

## Reference

| Member | Description |
| --- | --- |
| `static UI getCurrent()` | The UI running `init()`, or `null`. |
| `UIDataObject getData()` | The `@UIData` options. |
| `UIDataDebugObject getDebug()` | The `@UIDataDebug` options. |
| `UIDataPopupObject getPopup()` | The `@UIDataPopup` options. |
| `IndexedConcurrentList<Node> getNodeList()` | The top-level nodes. |
| `Map<Key[], Runnable> getKeybindMap()` | The keybinds. |
| `List<UIScheduledTask> getScheduledTaskList()` | The pending tasks. |
| `Map<Class<? extends UIStore>, UIStore> getStoreMap()` | The local stores of the UI. |
| `UIView getView()` | The view that maps the canvas to the window. See [View and Scaling](view-and-scaling.md). |
| `DoubleSignal getZoomLevel()` | The zoom, as a signal. |
| `DoubleSignal getScaledWidth()`, `DoubleSignal getScaledHeight()` | The visible size of the canvas, in canvas units, as signals. |
| `double getWidth()`, `double getHeight()` | The window size the UI was loaded with, in window pixels. |
| `void zoom(double zoom)` | Sets the zoom, clamped to its limits. |
| `Transition getTransition()`, `UI setTransition(Transition transition)` | The open and close animation; `null` for none. See [Transitions](transitions.md). |
| `Optional<IUIBridge> getBridge()` | The bridge that handles this UI, empty when no bridge handles it. |
| `int getIndex()` | `zlevel` rounded down, used to order the UIs of a bridge. |
| `void drawHover(List<String> lines, double mouseX, double mouseY)` | Draws a text tooltip; delegates to the bridge's `drawHover`. Override it to draw the tooltips of this UI yourself. See [Hover and Tooltips](../interactions/hover.md). |
| `Node getDevNode()` | The inspector in dev mode, otherwise `null`. |
| `double getRenderPipelineLevel()`, `void setRenderPipelineLevel(double level)` | Depth offset for the nodes drawn next; nodes that draw in depth, such as `ModelNode`, raise it so later nodes stay in front. Reset to 0 at each draw. |
| `FileAlterationMonitor getFileMonitor()` | The hot reload watcher, or `null`. |
| `getStencilStack()` | The stack of active masks (its element type is private). |
| `void load(double width, double height)`, `void load(double width, double height, double zoom)` | For bridges: sizes the UI to the window (zoom 1 for the first form) and initializes it on first call. |
| `boolean onMousePressed(ClickType)`, `onMouseReleased(ClickType)`, `onMouseDragged(ClickType, long)`, `onMouseScroll(int)`, `onKeyPressed(char, Key)` | For bridges: dispatch an event to the nodes and the hooks; return `true` when it was consumed. They return `false` without dispatching before the first load. |
| `void onUpdate()` | For bridges: updates the nodes, then calls `update()`. |
| `void draw(double mouseX, double mouseY)` | For bridges: draws the UI; the mouse is in window coordinates. |
| `boolean onClose()` | For bridges and `JOID.close`: asks `close()`, starts the Out transition, returns `true` when the UI can be removed now. |
| `void properlyClose()` | Releases the UI (see [Lifecycle](#lifecycle)). |

## See also

- [Opening and Closing UIs](managing-uis.md)
- [View and Scaling](view-and-scaling.md)
- [Transitions](transitions.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)
- [UI Bridge](../integration/ui-bridge.md)