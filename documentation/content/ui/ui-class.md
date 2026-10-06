# UI Class

The `UI` class is the root of every screen. Extend it, implement `init()`, and you've got a UI.

## Declaration

```java
public class MyUI extends UI {

    @Override
    public void init() {
        // Attach your nodes here
    }
}
```

## Configuration with `@UIData`

Control global behavior with the `@UIData` annotation. All attributes have sensible defaults:

```java
@UIData(
    active = true,
    visible = true,
    pause = true,
    closeable = true,
    zoomable = true,
    projection = true,
    background = true,
    backgroundColor = "#101010c0",
    zlevel = 0,
    anchorX = Align.CENTER,
    anchorY = Align.CENTER
)
public class MyUI extends UI { ... }
```

| Attribute | Default | Description |
|---|---|---|
| `active` | `true` | If false, UI is skipped entirely (no update, no draw). |
| `visible` | `true` | If false, UI is updated but not drawn. |
| `pause` | `true` | If the host honors it, pauses the underlying app/game while open. |
| `closeable` | `true` | If false, ESC doesn't close this UI. Code can still call `JOID.close(this)`. |
| `zoomable` | `true` | Enables CTRL+`+`/`-` zoom in/out. |
| `projection` | `true` | Set up the orthographic projection automatically. Disable for custom 3D. |
| `background` | `true` | Render a filled background rectangle behind the node tree. |
| `backgroundColor` | `"#101010c0"` | Hex color for the background. Supports `rgba(...)` and `gradient(...)`. |
| `zlevel` | `0` | Draw order between multiple concurrent UIs. Higher = on top. |
| `anchorX` / `anchorY` | `CENTER` | Logical anchor for the 1920×1080 design space. |

You can also mutate at runtime:

```java
this.getData().setCloseable(false).setBackground(false);
```

## Lifecycle

```
constructor → JOID.open(ui) → load() → init() → (frame loop) → close() → properlyClose()
```

### `init()`

Called once when the UI is opened (or reloaded via hot-reload). Attach all nodes here.

> TIP: **Never** call `init()` yourself. JOID calls it at the right time, with `UI.current` set so nodes created during `init` can resolve `getUi()` before being attached.

### `update()`

Called every game tick (frame when standalone). Override to run logic that doesn't need render state:

```java
@Override
public void update() {
    // your logic, run once the nodes have updated
}
```

### `preDraw(mouseX, mouseY)` / `postDraw(mouseX, mouseY)`

Called before / after the node tree is drawn. Use for custom overlay rendering:

```java
@Override
public void postDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.WHITE, 10);
}
```

### `close()`

Return `false` to veto the close requested by ESC or `JOID.close(ui)` (e.g., unsaved changes warning). Return `true`, the default, to let JOID close the UI. The final `onClose()` asks it, then starts the exit transition.

### `properlyClose()`

Internal. Calls `onDetach()` on all nodes, saves stores and properties, stops file monitors. Don't override.

## Keybinds

Register global UI shortcuts with `keybind(Runnable, Key... keys)`:

```java
@Override
public void init() {
    this.keybind(() -> JOID.open(new SettingsUI()), Key.TAB);
    this.keybind(() -> this.reload(), Key.R, Key.LEFT_CONTROL);
}
```

Multiple keys = combination (all pressed simultaneously). On a closeable UI — the default of `@UIData` — ESC closes the UI before the keybinds see it: bind `Key.ESCAPE` only on a UI declared `@UIData(closeable = false)`.

## Reload

`this.reload()` re-runs `init()` after clearing nodes. Useful for signals bound with `WatchProperty.RELOAD` (the default).

```java
mySignal.subscribe(val -> this.reload());  // or use .watch() on a specific node
```

Internally, reload detaches the previous nodes (`onDetach`) and clears them, stored callbacks are preserved, and `init()` runs again with fresh state.

## Scheduled tasks

Schedule callbacks to run after a delay or periodically:

```java
this.schedule(() -> System.out.println("Pong"), 1000L);            // run once after 1s
this.schedule(() -> this.tick(), 0L, 100L);                        // every 100ms
```

Without a period the task runs once, and a period of `0L` runs it on every frame. Tasks run on the render thread; use them for time-driven UI updates (timers, polling). They're thread-safe (`CopyOnWriteArrayList` backing).

## Stores & properties

Persistent state is saved automatically via `@UIStoreData` on fields of a `UIStore`, or via `@UIProperty` on fields of the UI itself. See [Stores](../state/stores.md).

```java
@UIProperty
private double zoomLevelConfig = 1D;

// Automatically persisted on close, restored on load.
```

Each UI keeps its properties in `config/property/<class name>.property`, as JSON: every type Gson can write survives the round trip — primitives, strings, lists, maps and plain objects, generics included. `@UIProperty("key")` names the entry after `key` instead of the field, and a field set back to `null` is removed from the file, so the next load keeps its default value. A corrupted file is deleted and the defaults stay.

## Coordinates and scale

A UI is designed on a 1920×1080 canvas. `ui.getView()` returns the `UIView` that maps this canvas onto the window, and every conversion goes through it — drawing, mouse, tooltips, effect resolution:

1. the canvas fits the window, widened or heightened when the window ratio differs, and placed by `anchorX` / `anchorY`;
2. the **interface scale** chosen by the host is applied around the anchor (see [Interface scale](bridge.md#interface-scale));
3. the **zoom** of the user (CTRL + `+` / `-`) is applied around the anchor.

```java
ui.getMouseX();                       // mouse on the canvas, every transform included
ui.getView().toScreenX(x);            // canvas → window pixels (also toScreenY / toScreenWidth / toScreenHeight)
ui.getView().toUiX(screenX);          // window pixels → canvas (also toUiY)
ui.getView().getVisibleWidth();       // canvas width visible on screen, mirrored by the scaledWidth signal
ui.zoom(0.8D);
```

The zoom ranges from `0.1` to `max(1, 1 / interface scale)`, so a shrunk interface can still be zoomed back to full size. The `zoomLevel`, `scaledWidth` and `scaledHeight` signals follow every change.

## Best practices

- **One UI, one responsibility.** Don't cram a settings menu, a minimap, and a chat into one `UI`. Use multiple UIs and open/close them individually.
- **Keep `init()` cheap.** If a signal triggers `reload()`, `init()` runs again. Avoid heavy I/O there — load resources once at construction or via the `ResourceBuilder` cache.
- **Prefer `watch(signal)` over manual callbacks.** Watchers auto-cleanup on `properlyClose()`.
- **Don't override `draw()` or `render()`.** Use `preDraw` / `postDraw` or attach overlay nodes.

## See also

- [Bridge](bridge.md) — how UIs connect to the host.
- [Transitions](transitions.md) — in/out animations when opening or closing.
- [Stores](../state/stores.md) — persistent state system.
- [Node Fundamentals](../nodes/node-fundamentals.md) — building the tree.
