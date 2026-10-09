# UIs and Their Lifecycle

Every screen you build with JOID is a UI: a menu, a settings panel, a HUD, a popup. This page shows what a UI is, how you fill it, open it and close it, and what happens during its life. The `CounterUI` of the [Quick Start](../getting-started/quick-start.md) was one; here you meet the rest of what a UI does.

## A UI is a class

A UI is a subclass of `UI` (`dev.joid.lib.ui.core`). You build its content in `init()`:

```java
public final class SettingsUI extends UI {

	@Override
	public void init() {
		RectNode.create(560, 240, 800, 600).color(Color.LIGHTGRAY).attach(this);
	}

}
```

![The whole dark canvas with a light gray rectangle in its middle](../images/ess-uis-settings.png "The whole 1920 × 1080 canvas at 0.3× scale: the rectangle sits in the middle.")

`UI` has a public no-argument constructor and no abstract method: you override only the hooks you need. Here `init()` creates one rectangle and attaches it to the UI. The numbers are units of the 1920×1080 virtual canvas of the previous page: whatever the size of the window, the rectangle sits in the middle of the canvas (see [The Virtual Canvas](canvas.md)).

## The life of a UI

![A UI goes from its constructor to JOID.open, init, the frames and close; reload runs init again, and a close refused by close() keeps it open](../images/ess-diagram-ui.png "The life of a UI.")

| Moment | What happens |
| --- | --- |
| `new SettingsUI()` | Reads `@UIData`. No node exists yet. |
| `JOID.open(ui)` | The UI bridge adds the UI, which restores its saved fields (see [Saving State](../essentials/saving-state.md)), runs `init()` and plays its opening transition. |
| Every frame | Input goes to the nodes, then to the UI hooks; the nodes update, then the UI draws. |
| Window resized | The UI is resized and keeps its zoom; `init()` does not run again. |
| `reload()` | Detaches every node, runs `init()` again on the same instance. |
| Close | `close()` is asked; the closing transition plays, the nodes are detached, the stores and properties are saved. |

## Building the content with init

`init()` runs when the UI is loaded the first time and again on each `reload()`. Put everything that belongs to the screen there: nodes, keybinds, scheduled tasks.

```java
@Override
public void init() {
	RectNode.create(0, 0, 1920, 80).color(Color.GRAY).attach(this);

	RectNode.create(0, 1000, 1920, 80).color(Color.GRAY).attach(this);

	super.keybind(() -> JOID.close(this), Key.LEFT_CONTROL, Key.Q);
	super.schedule(() -> System.out.println("Shown for two seconds"), 2000L);
}
```

![The whole dark canvas with a gray bar along its top edge and another along its bottom edge](../images/ess-uis-bars.png "The two bars of init(), on the whole canvas at 0.3× scale.")

- `node.attach(this)` adds a node at the top level of the UI. `super.add(first, second)` does the same for several nodes.
- `keybind(runnable, keys...)` runs the code when one of the keys is pressed while all of them are down: here `Ctrl + Q` closes the UI. `Key` is in `dev.joid.lib.input.key`.
- `schedule(runnable, delay)` runs the code once, at the first frame after `delay` milliseconds. `schedule(runnable)` runs it at the next frame: it is also the way to hand work from another thread to the UI.

Each load starts from a clean UI: the nodes, keybinds and tasks of the previous `init()` are removed first.

## Opening and closing with JOID

UIs are opened and closed through the static methods of `JOID` (`dev.joid.internal`):

```java
JOID.open(new SettingsUI());

if (JOID.isOpen(SettingsUI.class)) {
	JOID.close(JOID.getUi(SettingsUI.class));
}
```

| Method | What it does |
| --- | --- |
| `JOID.open(ui)` | Hands the UI to the UI bridge that accepts it, which loads it and shows it. Returns that bridge. |
| `JOID.open(ui, true)` | Closes every UI of that bridge first, then opens this one. |
| `JOID.close(ui)` | Asks the UI to close, plays its closing transition, then removes it. |
| `JOID.isOpen(SettingsUI.class)` | Whether a UI of that class is open. |
| `JOID.getUi(SettingsUI.class)` | The open UI of that class, or `null`. |

The user can close a UI too: `Escape` closes the top UI when it is `closeable`, unless a focused text field takes the key first to cancel its edit. What "open" means exactly (above the others, or replacing them) is decided by your UI bridge, set up in the [Quick Start](../getting-started/quick-start.md).

## Refusing to close with close()

To keep a UI open, for example while there are unsaved changes, override `close()` and return `false`:

```java
private boolean dirty;
```

```java
@Override
public boolean close() {
	return !this.dirty;
}
```

`dirty` is a field your UI sets while there are unsaved changes. `close()` runs for `JOID.close(ui)` and for `Escape`.

## Configuring a UI with @UIData

The `@UIData` annotation (`dev.joid.lib.ui.core.data`) sets the options of a UI class:

```java
@UIData(backgroundColor = "#000000A0", closeable = false, zoomable = false)
public final class MenuUI extends UI {}
```

| Option | Default | Effect |
| --- | --- | --- |
| `background` | `true` | Fills the window with `backgroundColor` behind the UI. |
| `backgroundColor` | `"#101010c0"` | A translucent dark gray that dims what is behind. |
| `closeable` | `true` | Whether `Escape` closes the UI. `JOID.close` works either way. |
| `zoomable` | `true` | Whether `Ctrl` or `Alt` with `+` and `-` zoom the UI. |
| `zlevel` | `0D` | Order among the open UIs: a higher value is drawn on top. |
| `anchorX`, `anchorY` | `Align.CENTER` | Where the canvas sits in a window that is not 16:9, and the pivot of the zoom (see [The Virtual Canvas](canvas.md#pinning-a-ui-with-anchorx-and-anchory)). |

`ui.getData()` changes these values while the UI runs (`getData().setZlevel(200D)`); the change applies at the next frame.

## Several UIs and popups

Several UIs can be open together: a HUD below, a menu above, a notification layer on top. They are ordered by `zlevel`, then by opening order: the last one is drawn on top and receives input first.

```java
@UIData(zlevel = -10D, background = false, closeable = false)
public final class HudUI extends UI {}
```

A popup is a UI marked with `@UIDataPopup(active = true)` (`dev.joid.lib.ui.core.data.popup`): it keeps the input from the UIs below it, and opens and closes with a short scale animation.

```java
@UIDataPopup(active = true)
public final class ConfirmPopup extends UI {

	@Override
	public void init() {
		RectNode.create(710, 390, 500, 300).color(Color.WHITE).attach(this);
	}

}
```

![A white popup scales in over a settings screen and dims it, then scales out when Escape closes it](../images/ess-uis-popup.gif "ConfirmPopup opened over SettingsUI, then closed with Escape (whole canvas at 0.3× scale).")

Besides `init()` and `close()`, a UI can override `update()` (every frame), `preDraw` and `postDraw` (to draw below or above the nodes) and input hooks such as `keyPressed`. [Input and Callbacks](input.md) shows the input hooks; [The UI Class](../ui/ui-class.md) lists them all.

## Reloading while you work

In dev mode, `Ctrl + R` (or `F5`) calls `reload()`: `init()` runs again on the same instance, so the fields and signals of the UI keep their values. `Ctrl + Shift + R` (or `Shift + F5`) replaces the UI with a new instance, built with its no-argument constructor, and starts from scratch. [Developer Tools](dev-tools.md) lists every shortcut.

## Pitfalls

- Do not build nodes in the constructor: the first load removes every node, keybind and task added before `init()`.
- `JOID.open(ui)` throws an `IllegalStateException` when no registered UI bridge accepts the UI: register your bridge before opening anything.
- `Ctrl + Shift + R` needs a constructor without arguments; a UI that has none can only be reloaded with `Ctrl + R`.

## See also

- Next: [Nodes and the Node Tree](nodes.md)
- [The UI Class](../ui/ui-class.md): every hook, every `@UIData` option, keybinds, scheduled tasks, masks.
- [Opening and Closing UIs](../ui/managing-uis.md): the `force` variants, ordering, `Escape`, popups.
- [View and Scaling](../ui/view-and-scaling.md): how the canvas fits the window, zoom, coordinate conversions.
- [Transitions](../ui/transitions.md): opening and closing animations.
- [The Frame Loop](frame-loop.md): what happens in each frame.