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
constructor → JOID.open(ui) → load() → init() → (frame loop) → onClose() → properlyClose()
```

### `init()`

Called once when the UI is opened (or reloaded via hot-reload). Attach all nodes here.

> TIP: **Never** call `init()` yourself. JOID calls it at the right time, with `UI.current` set so nodes created during `init` can resolve `getUi()` before being attached.

### `update()`

Called every game tick (frame when standalone). Override to run logic that doesn't need render state:

```java
@Override
public void update() {
    super.update();  // runs node updates
    // your logic
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

### `onClose()`

Return `false` to veto ESC-based close (e.g., unsaved changes warning). Return `true` to let JOID close the UI.

### `properlyClose()`

Internal. Calls `onDetach()` on all nodes, saves stores and properties, stops file monitors. Don't override.

## Keybinds

Register global UI shortcuts with `keybind(Runnable, Integer... keys)`:

```java
@Override
public void init() {
    this.keybind(() -> JOID.open(new SettingsUI()), Keyboard.KEY_ESCAPE);
    this.keybind(() -> this.reload(), Keyboard.KEY_R, Keyboard.KEY_LCONTROL);
}
```

Multiple keys = combination (all pressed simultaneously).

## Reload

`this.reload()` re-runs `init()` after clearing nodes. Useful for signals bound with `WatchProperty.RELOAD` (the default).

```java
mySignal.subscribe(val -> this.reload());  // or use .watch() on a specific node
```

Internally, reload clears children, stored callbacks are preserved, and `init()` runs again with fresh state.

## Scheduled tasks

Schedule callbacks to run after a delay or periodically:

```java
this.schedule(() -> System.out.println("Pong"), 1000L, 0L);        // run once after 1s
this.schedule(() -> this.tick(), 0L, 100L);                        // every 100ms
```

Tasks run on the render thread; use them for time-driven UI updates (timers, polling). They're thread-safe (`CopyOnWriteArrayList` backing).

## Stores & properties

Persistent state is saved automatically via `@UIStoreData` on fields of a `UIStore`, or via `@UIProperty` on fields of the UI itself. See [Stores](../state/stores.md).

```java
@UIProperty
private double zoomLevelConfig = 1D;

// Automatically persisted on close, restored on load.
```

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
