# UIs

Every screen you build with JOID is a UI: a menu, a settings panel, a HUD, a popup. This page shows what a UI is, how you fill it with nodes, how you open and close it, and what happens during its life. It is the first of the Essentials pages; read them in order.

## A UI is a class

A UI is a subclass of `UI` (`dev.joid.lib.ui.core`). You build its content in `init()`:

```java
public final class SettingsUI extends UI {

    @Override
    public void init() {
        RectNode.create(560, 240, 800, 600).color(Color.DARKGRAY).attach(this);
    }

}
```

![The whole canvas with a dark gray rectangle in its middle](../images/ess-uis-settings.png "The whole 1920 × 1080 canvas at 0.3× scale: the rectangle sits in the middle.")

`UI` has a public no-argument constructor and no abstract method: you override only the hooks you need. Here, `init()` creates one dark rectangle and attaches it to the UI. The numbers are units of the 1920×1080 virtual canvas: whatever the size of the window, the rectangle sits in the middle of the screen. [Layout](layout.md) explains the canvas.

## Building the content in init

`init()` runs when the UI is loaded for the first time, and again each time the UI is reloaded. Put everything that belongs to the screen there: nodes, keybinds, scheduled tasks.

```java
@Override
public void init() {
    RectNode.create(0, 0, 1920, 80).color(Color.BLACK).attach(this);
    RectNode.create(0, 1000, 1920, 80).color(Color.BLACK).attach(this);

    this.keybind(() -> JOID.close(this), Key.Q, Key.LEFT_CONTROL);
    this.schedule(() -> System.out.println("Shown for two seconds"), 2000L);
}
```

![The whole canvas with a black bar along its top edge and another along its bottom edge](../images/ess-uis-bars.png "The two bars of init(), on the whole canvas at 0.3× scale.")

- `node.attach(this)` adds a node at the top level of the UI. `this.add(node1, node2)` does the same for several nodes.
- `keybind(runnable, keys...)` runs the code when a key is pressed while all the listed keys are down: here `Ctrl+Q` closes the UI.
- `schedule(runnable, delay)` runs the code once, at the first frame after `delay` milliseconds. `schedule(runnable)` runs it at the next frame, which is also the safe way to hand work from another thread to the UI.

> WARNING: Do not build nodes in the constructor. The first load clears every node, keybind and task added before `init()`.

## Opening and closing a UI

UIs are opened and closed through static methods of `JOID` (`dev.joid.internal.JOID`):

```java
JOID.open(new SettingsUI());

if (JOID.isOpen(SettingsUI.class)) {
    JOID.close(JOID.getUI(SettingsUI.class));
}
```

| Method | What it does |
| --- | --- |
| `JOID.open(ui)` | Hands the UI to the UI bridge, which loads it (this runs `init()`) and shows it. |
| `JOID.close(ui)` | Asks the UI to close, plays its closing transition if it has one, then removes it. |
| `JOID.isOpen(SettingsUI.class)` | Whether a UI of that class is open. |
| `JOID.getUI(SettingsUI.class)` | The open UI of that class, or `null`. |

The user can close a UI too: `Escape` closes the top UI, unless it is not `closeable`. What "open" means exactly (on top of the others, or replacing them) is decided by your UI bridge, set up in the [Quick Start](../getting-started/quick-start.md).

To keep a UI open, for example while there are unsaved changes, override `close()` and return `false`:

```java
@Override
public boolean close() {
    return !this.dirty;
}
```

`dirty` is a `boolean` field of the UI. `close()` runs for `JOID.close(ui)` and for `Escape`.

## Configuring a UI with @UIData

The `@UIData` annotation (`dev.joid.lib.ui.core.data`) sets the options of a UI class:

```java
@UIData(backgroundColor = "#000000A0", closeable = false, zoomable = false)
public final class SettingsUI extends UI {}
```

The most useful options:

| Option | Default | Effect |
| --- | --- | --- |
| `background` | `true` | Fills the window with `backgroundColor` behind the UI. |
| `backgroundColor` | `"#101010c0"` | A translucent dark gray that dims what is behind. |
| `closeable` | `true` | Whether `Escape` closes the UI. `JOID.close` works either way. |
| `zoomable` | `true` | Whether `Ctrl`/`Alt` + `+`/`-` zoom the UI. |
| `zlevel` | `0D` | Order among open UIs: a higher value stays on top. |
| `anchorX`, `anchorY` | `Align.CENTER` | Where the canvas sits in a window that is not 16:9. |

## Several UIs at once

Several UIs can be open together: a HUD below, a menu above, a notification layer on top. They are ordered by `zlevel`, then by opening order. The last one is drawn on top and receives input first.

```java
@UIData(zlevel = -10D, background = false, closeable = false)
public final class HudUI extends UI {}
```

A popup is a UI marked with `@UIDataPopup(active = true)` (`dev.joid.lib.ui.core.data.popup`): it blocks input to the UIs below it and opens and closes with a small scale animation.

```java
@UIDataPopup(active = true)
public final class ConfirmPopup extends UI {

    @Override
    public void init() {
        RectNode.create(710, 390, 500, 300).color(Color.DARKGRAY).attach(this);
    }

}
```

![A popup scales in over a settings screen, dims it, then scales out when Escape closes it](../images/ess-uis-popup.gif "ConfirmPopup opened over SettingsUI, then closed with Escape (whole canvas at 0.3× scale).")

## The life of a UI at a glance

| Moment | What happens |
| --- | --- |
| `new SettingsUI()` | Reads `@UIData`. No node exists yet. |
| First load (`JOID.open`) | Restores the saved [`@UIProperty`](state.md) fields, runs `init()`, plays the opening transition. |
| Every frame | Input goes to the nodes then to the UI hooks, the nodes update, then the UI draws. |
| Window resized | The UI is resized; `init()` does not run again. |
| `reload()` | Clears every node, runs `init()` again. Useful in development; to refresh only a part of the screen, use [signals](state.md). |
| Close | `close()` is asked, the closing transition plays, the nodes are detached and the persistent state is saved. |

Besides `init()` and `close()`, a UI can override `update()` (every frame), `preDraw` and `postDraw` (to draw below or above the nodes) and the input hooks such as `keyPressed`. You meet them in [Handling Input](input.md).

## Going further

- [The UI Class](../ui/ui-class.md): every hook, every `@UIData` option, keybinds, scheduled tasks, masks.
- [Opening and Closing UIs](../ui/managing-uis.md): the `force` variants, ordering, `Escape` handling, popups.
- [View and Scaling](../ui/view-and-scaling.md): how the canvas fits the window, zoom, coordinate conversions.
- [Transitions](../ui/transitions.md): opening and closing animations.
- [Core Concepts](../getting-started/core-concepts.md): the frame lifecycle and the bridges.

Next: [Nodes](nodes.md).