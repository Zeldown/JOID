# Opening and Closing UIs

UIs are opened, closed and looked up through static methods of `JOID` (`dev.joid.internal.JOID`), which hand the work to the UI bridge that accepts the UI. This page covers those methods, how several UIs coexist, how `Escape` closes them, and popups.

## Opening and closing

```java
final SettingsUI settings = new SettingsUI();
JOID.open(settings);

JOID.getUI(SettingsUI.class).ifPresent(JOID::close);
```

| Method | Description |
| --- | --- |
| `static Optional<IUIBridge> open(UI ui)` | Finds the bridge that accepts `ui` (the last registered bridge whose `canHandle(ui)` returns `true`) and calls its `open(ui)`. Returns that bridge, empty when no bridge accepts the UI. |
| `static Optional<IUIBridge> open(UI ui, boolean force)` | With `force` `false`, same as `open(ui)`. With `force` `true`, first closes every UI of that bridge without asking them (no `close()` hook, no Out transition: each one is released with `properlyClose()` and removed with the bridge's `close`), then opens `ui`. |
| `static void close(UI ui)` | Asks the UI to close through `ui.onClose()`: its `close()` hook can refuse, and an Out transition delays the removal until it ends. Then calls the bridge's `close(ui)`. Does nothing when no bridge accepts the UI. |
| `static void close(UI ui, boolean force)` | With `force` `false`, same as `close(ui)`. With `force` `true`, releases the UI with `properlyClose()` and calls the bridge's `close(ui)` without asking it and without transition. |
| `static boolean isOpen(UI ui)` | Whether the bridge of `ui` lists it as opened. |
| `static boolean isOpen(Class<? extends UI> uiClass)` | Whether an open UI is an instance of `uiClass` (subclasses count). |
| `static <T extends UI> Optional<T> getUI(Class<T> uiClass)` | The first open UI, in the bridge's order, that is an instance of `uiClass`; empty if none. |

All of them throw `NullPointerException` for a `null` argument. `JOID.close` works whatever the `closeable` option of the UI.

### Refusing to close with close()

Override `close()` to keep a UI open, for example to confirm unsaved changes (`dirty` is a `boolean` field of the UI, `ConfirmPopup` is the popup of [Popups](#popups-with-uidatapopup)):

```java
@Override
public boolean close() {
    if (this.dirty) {
        JOID.open(new ConfirmPopup());
        return false;
    }
    return true;
}
```

The hook runs for `JOID.close(ui)`, `Escape` and bridges that call `ui.onClose()`. It does not run for the `force` variants. While the Out transition of a UI is running, further close requests are ignored.

## What open does depends on the bridge

`JOID.open` and `JOID.close` only call the bridge's `open` and `close`; the bridge decides what they mean. `UIBridge` (`dev.joid.lib.bridge.ui`), the base class of UI bridges, leaves them to you:

- The bridge of the [Quick Start](../getting-started/quick-start.md) adds every opened UI on top of the others and removes closed ones.
- `DemoUIBridge`, the bridge of the demo window, closes the open UIs (through `onClose()`) before opening a UI that is not a popup. If one of them refuses, the new UI is not opened; if one plays an Out transition, the new UI opens when the transition ends. A popup opens on top without closing anything.

The bridge must load a UI it adds, with `ui.load(width, height)`. See [UI Bridge](../integration/ui-bridge.md).

## Several UIs at once

A `UIBridge` keeps its UIs in `getUiList()`, an `IndexedLinkedList<UI>` sorted by `zlevel` (rounded down), then by opening order:

| Phase | Order |
| --- | --- |
| Draw | From the first UI to the last: the last one is drawn on top. A UI with `visible = false` is skipped. |
| Update | From the first UI to the last, all UIs. |
| Input | From the last UI to the first, skipping UIs that are not `active` or not `visible`. The event stops at the first UI that consumes it, or at a popup. |

Give a UI a higher `zlevel` to keep it above UIs opened later, for example a HUD below menus and a notification layer above them:

```java
@UIData(zlevel = -10D, background = false, closeable = false)
public final class HudUI extends UI {}

@UIData(zlevel = 100D, background = false, closeable = false, active = false)
public final class ToastUI extends UI {}
```

The position in the list is computed when the bridge adds the UI; set `zlevel` in the annotation or before opening. `zlevel` also offsets the depth at which the UI is drawn.

`isOnTop()` of the bridge tells whether a UI is the top one. A UI not on top draws no tooltip and does not show the dev inspector.

### Active, visible and closeable at runtime

Change these options through `getData()`:

```java
hud.getData().setVisible(false);
menu.getData().setActive(false);
menu.getData().setCloseable(false);
```

| Option | `false` means |
| --- | --- |
| `active` | No input; still updated and drawn. Useful while a UI animates out. |
| `visible` | Not drawn and no input; still updated. |
| `closeable` | `Escape` does not close the UI. |

## Escape handling

When `Escape` is pressed, `UIBridge.keyTyped` goes through the active, visible UIs from the top:

1. If the UI is `closeable` and its `onClose()` agrees, the bridge closes it and `Escape` goes no further.
2. Otherwise the UI receives `Escape` as a normal key press (nodes, keybinds, `keyPressed`). If it consumes it, or if the UI is a popup, `Escape` goes no further; else the next UI below gets the same treatment.

So one `Escape` closes the top closeable UI, and a popup always stops `Escape` from reaching the UIs below it.

## Popups with @UIDataPopup

`@UIDataPopup` (`dev.joid.lib.ui.core.data.popup`) marks a UI as a popup:

```java
@UIDataPopup(active = true)
public final class ConfirmPopup extends UI {

    @Override
    public void init() {
        RectNode.create(710, 390, 500, 300).color(Color.DARKGRAY).attach(this);
    }

}
```

| Attribute | Default | Description |
| --- | --- | --- |
| `active` | `false` | Whether the UI is a popup. |
| `transition` | `PopupTransition.IN_OUT` | Which default transition plays: `NONE`, `IN` (opening only), `OUT` (closing only) or `IN_OUT`. |

A popup:

- is modal for input: events that reach it never go to the UIs below, consumed or not;
- gets a `PopTransition` at construction, unless `transition` is `NONE`; the states not selected by `transition` are disabled. See [Transitions](transitions.md#poptransition);
- is not closed by `DemoUIBridge` when another UI opens, and does not close the open UIs when it opens there.

Its default `@UIData` background dims the UIs below it. `getPopup()` returns the options as a `UIDataPopupObject` with `setActive(boolean)` and `setTransition(PopupTransition)`; the transition is chosen at construction, so changing it later has no effect. `PopupTransition` has `isIn()`, `isOut()` and `isActive()` (`true` unless `NONE`).

## See also

- [The UI Class](ui-class.md)
- [Transitions](transitions.md)
- [UI Bridge](../integration/ui-bridge.md)
- [Mouse and Keyboard](../interactions/mouse-and-keyboard.md)