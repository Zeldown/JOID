# Mouse and Keyboard

JOID receives mouse and key events from its UI bridge and dispatches them to the open UIs and their nodes. This page covers the mouse buttons (`ClickType`), the keys (`Key`), the modifier helpers, keybinds, mouse coordinates and the exact order in which an event travels.

## Reacting to clicks and keys

```java
public class EditorUI extends UI {

    @Override
    public void init() {
        RectNode
        .create(100, 100, 300, 80)
        .color(Color.WHITE)
        .onClick((node, mouseX, mouseY, clickType) -> {
            if (clickType.isRight()) {
                System.out.println("Context menu at " + mouseX + ", " + mouseY);
            }
        })
        .attach(this);

        this.keybind(() -> System.out.println("Saved"), Key.LEFT_CONTROL, Key.S);
    }

}
```

`onClick` fires only for a press over the node. `keybind` runs its action when a key event reaches the UI while every listed key is down.

## Mouse buttons with ClickType

`ClickType` (`dev.joid.lib.utils.click`) is the button of a mouse event.

| Constant | Button index | Predicate |
|---|---|---|
| `LEFT` | `0` | `isLeft()` |
| `RIGHT` | `1` | `isRight()` |
| `MIDDLE` | `2` | `isMiddle()` |
| `BACK` | `3` | `isBack()` |
| `FORWARD` | `4` | `isForward()` |
| `OTHER` | `-1` | `isOther()` |

| Method | Description |
|---|---|
| `static from(int button)` | Maps a button index to a constant; any index other than 0 to 4 gives `OTHER`. |
| `getButton()` | The button index of the constant, `-1` for `OTHER`. |

## Mouse callbacks

| Method | Lambda arguments | Fires |
|---|---|---|
| `onClick` | `(node, mouseX, mouseY, clickType)` | A press while the node is hovered, unless a node reached before it consumed the press. |
| `onMousePressed` | `(node, mouseX, mouseY, clickType)` | Every press, wherever the mouse is. |
| `onMouseReleased` | `(node, mouseX, mouseY, clickType)` | Every release, wherever the mouse is. |
| `onMouseDragged` | `(node, mouseX, mouseY, clickType, deltaTime)` | Every mouse move while a button is held; `clickType` is the held button. |
| `onMouseScroll` | `(node, mouseX, mouseY, value)` | Every wheel event. |

- `value` of a wheel event is the delta forwarded by the UI bridge: positive when the wheel rolls up, never `0` (the bridge drops still events). The demo window of the GLFW module sends 120 per notch.
- `deltaTime` of a drag event is the time value forwarded by the UI bridge; the demo windows of the GLFW and LWJGL 2 modules send the milliseconds elapsed since the button was pressed.
- A lambda given to any of these methods consumes the event. `onMousePressed`, `onMouseReleased`, `onMouseDragged` and `onMouseScroll` therefore take the event away from every node reached after theirs; see [Consumed input events](callbacks.md#consumed-input-events).
- `onMousePressed`, `onMouseReleased`, `onMouseDragged` and `onMouseScroll` fire whatever `visible(...)` and `enabled(...)` return. `onClick`, hover and drags require a visible, enabled node.

## Mouse coordinates

- The `mouseX` and `mouseY` given to callbacks and hooks are UI units: the window position of the mouse at the last drawn frame, converted through the UI's view. `UI.getMouseX()` and `UI.getMouseY()` return the same values.
- For coordinates relative to a node, subtract its absolute position: `mouseX - node.getAbsoluteX()`.
- The window position in pixels is `BridgeHandler.WINDOW.get().getMouseX()` and `getMouseY()`; convert between window and UI units with `ui.getView().toUiX(...)`, `toUiY(...)`, `toScreenX(...)` and `toScreenY(...)` (see [View and Scaling](../ui/view-and-scaling.md)).

### Hit testing with isHovered

`node.isHovered(mouseX, mouseY)` is the test used for clicks, hover and drags. It returns `true` when all of these hold:

- the node is in a UI, that UI is on top (`UI.isOnTop()`, as reported by its UI bridge), and the node is visible and enabled;
- the mouse is inside the area of the parent that clips the node with an overflow, if any;
- `getAbsoluteX() < mouseX <= getAbsoluteX() + getWidth()` and `getAbsoluteY() < mouseY <= getAbsoluteY() + getHeight()`.

`isHovered(mouseX, mouseY, false)` skips the enabled check. `isHovered()` without arguments returns the hover state of the last drawn frame (see [Hover and Tooltips](hover.md)).

## Keyboard callbacks with onKeyPressed

`onKeyPressed((node, c, key) -> ...)` fires for every key event the UI receives, wherever the mouse is and whatever `visible(...)` and `enabled(...)` return.

- `c` is the character the UI bridge sends with the key. The demo window of the GLFW module sends the typed character for text input and `(char) 0` for the other keys, including text keys pressed with Ctrl or Alt.
- `key` is a `Key` constant; a key the bridge cannot map is `Key.UNKNOWN`.
- There is no release event for keys. Read the current state of a key with `Key.isDown()`.
- The lambda consumes the event, so the nodes reached afterwards and the UI keybinds do not receive it. Override `post` to observe keys without consuming them (see [Callbacks](callbacks.md#observing-an-event-without-consuming-it)).

## Keys with Key

`Key` (`dev.joid.lib.utils.key`) lists the keys JOID knows.

| Group | Constants |
|---|---|
| Letters | `A` to `Z` |
| Digits | `DIGIT_0` to `DIGIT_9` |
| Function keys | `F1` to `F25` |
| Editing | `ESCAPE`, `ENTER`, `TAB`, `BACKSPACE`, `INSERT`, `DELETE` |
| Navigation | `RIGHT`, `LEFT`, `DOWN`, `UP`, `PAGE_UP`, `PAGE_DOWN`, `HOME`, `END` |
| Locks and system | `CAPS_LOCK`, `SCROLL_LOCK`, `NUM_LOCK`, `PRINT_SCREEN`, `PAUSE` |
| Punctuation | `SPACE`, `APOSTROPHE`, `COMMA`, `MINUS`, `PERIOD`, `SLASH`, `SEMICOLON`, `EQUAL`, `LEFT_BRACKET`, `BACKSLASH`, `RIGHT_BRACKET`, `GRAVE_ACCENT` |
| Numpad | `NUMPAD_0` to `NUMPAD_9`, `NUMPAD_DECIMAL`, `NUMPAD_DIVIDE`, `NUMPAD_MULTIPLY`, `NUMPAD_SUBTRACT`, `NUMPAD_ADD`, `NUMPAD_ENTER`, `NUMPAD_EQUAL` |
| Modifiers | `LEFT_SHIFT`, `LEFT_CONTROL`, `LEFT_ALT`, `LEFT_SUPER`, `RIGHT_SHIFT`, `RIGHT_CONTROL`, `RIGHT_ALT`, `RIGHT_SUPER`, `MENU` |
| Other | `UNKNOWN` |

`key.isDown()` returns the current state of the key, asked to the window bridge (`IWindowBridge.isKeyDown`). You can call it anywhere, for example in a click callback:

```java
.onClick((node, mouseX, mouseY, clickType) -> {
    if (Key.LEFT_SHIFT.isDown()) {
        System.out.println("Shift-click");
    }
})
```

## Modifier helpers

The static helpers of `UI` test the left and the right key of a modifier at once.

| Method | Returns `true` when |
|---|---|
| `UI.isCtrlKeyDown()` | `LEFT_CONTROL` or `RIGHT_CONTROL` is down. |
| `UI.isShiftKeyDown()` | `LEFT_SHIFT` or `RIGHT_SHIFT` is down. |
| `UI.isAltKeyDown()` | `LEFT_ALT` or `RIGHT_ALT` is down. |

## Keybinds with UI.keybind

`keybind(Runnable runnable, Key... keys)` registers a shortcut on a UI.

```java
@Override
public void init() {
    this.keybind(() -> JOID.open(new SettingsUI()), Key.LEFT_CONTROL, Key.O);
    this.keybind(() -> System.out.println("Help"), Key.F1);
}
```

- On each key event that no node consumed, every keybind whose keys are all down (`Key.isDown()`) runs, and the event is consumed. The keys are tested as a set: their order does not matter, and the key of the event itself is not compared.
- Several keybinds can run for the same event; their order is unspecified.
- Each call adds a keybind, even for a combination already registered.
- The UI clears its keybinds every time it initializes (first open and every `UI.reload()`), so register them in `init()`.

> WARNING: Keybinds run only when no node consumed the key. A focused text field consumes every key, and any `onKeyPressed` lambda consumes every key it receives, which disables the keybinds of its UI.

## UI input hooks

A `UI` can override the input hooks of `IUI`. They run after all the nodes of the UI, with the same context, so check `context.isCancelled()` to know whether a node handled the event, and cancel it to keep it from the UIs below.

| Hook | Arguments |
|---|---|
| `mousePressed` | `(mouseX, mouseY, clickType, context)` |
| `mouseReleased` | `(mouseX, mouseY, clickType, context)` |
| `mouseDragged` | `(mouseX, mouseY, clickType, deltaTime, context)` |
| `mouseScroll` | `(mouseX, mouseY, value, context)` |
| `keyPressed` | `(c, key, context)` |

```java
@Override
public void keyPressed(final char c, final Key key, final InternalContext context) {
    if (!context.isCancelled() && key == Key.TAB) {
        context.cancel();
        System.out.println("Next tab");
    }
}
```

## Event dispatch order

The UI bridge receives the events from the backend through `UIBridge.mousePressed(ClickType)`, `mouseReleased(ClickType)`, `mouseDragged(ClickType, long)`, `mouseScroll(int)` and `keyTyped(char, Key)` (see [UI Bridge](../integration/ui-bridge.md)). For each event:

1. The UI bridge walks its UIs from the top one down, skipping the UIs that are not active or not visible (`active` and `visible` of `@UIData`, readable and changeable through `ui.getData()`). A wheel event with a value of `0` is dropped.
2. Key events only: on `Key.ESCAPE`, a closeable UI (`closeable`, `true` by default) is asked to close; when it closes immediately, the dispatch stops there (see [Opening and Closing UIs](../ui/managing-uis.md)).
3. The UI dispatches the event to its nodes, front to back (see [Callbacks](callbacks.md#input-events-across-nodes) for the order inside a node). Events that arrive before the UI finished its first initialization are ignored.
4. Key events only, when no node consumed the event:
   1. the keybinds (see above);
   2. when the UI is zoomable (`zoomable`, `true` by default) and the event is still not consumed: `+` or `NUMPAD_ADD` with Ctrl or Alt zooms in by `0.1`, `-` or `NUMPAD_SUBTRACT` with Ctrl or Alt zooms out by `0.1`; the event is consumed when the zoom changed;
   3. in dev mode, when the event is still not consumed: Left Ctrl+R or F5 reloads the UI (with Left Shift held, the zoom also goes back to `1`), F3 shows or hides the inspector (see [Developer Tools](../getting-started/dev-tools.md)).
5. The UI hook (`mousePressed`, `keyPressed`...) runs with the context.
6. When the event is consumed, or the UI is a popup (`@UIDataPopup(active = true)`, see [Opening and Closing UIs](../ui/managing-uis.md)), the dispatch stops; otherwise the next UI below receives it.

Wheel events in dev mode: with Left Alt held, the wheel zooms the UI (in larger steps with Left Shift) and the event goes no further.

The entry points of a UI are public: `onMousePressed(ClickType)`, `onMouseReleased(ClickType)`, `onMouseDragged(ClickType, long)`, `onMouseScroll(int)` and `onKeyPressed(char, Key)` run steps 3 to 5 and return `true` when the event was consumed. Calling them simulates input on one UI.

## Keyboard focus

JOID has no global focus: every key event reaches every node of the UI until one consumes it. Focus belongs to the nodes that need it:

- A [TextFieldNode](../nodes/input/text-field.md) or [MultilineTextFieldNode](../nodes/input/multiline-text-field.md) takes the focus when clicked and loses it when a press lands elsewhere; while focused it consumes every key, so the keybinds and the nodes reached after it do not see the keys.
- For your own focus, keep the state yourself and consume keys only while focused:

```java
final BooleanSignal focused = new BooleanSignal();

RectNode
.create(100, 100, 300, 60)
.color(Color.WHITE)
.onClick((node, mouseX, mouseY, clickType) -> focused.set(true))
.onKeyPressed(new NodeKeyPressedCallback<RectNode>() {

    @Override
    public void apply(final RectNode node, final char c, final Key key) {
        if (key == Key.ENTER) {
            focused.set(false);
            return;
        }

        System.out.println("Typed " + c);
    }

    @Override
    public void post(final RectNode node, final InternalContext context, final char c, final Key key) {
        if (focused.getOrDefault()) {
            context.cancel(() -> this.apply(node, c, key));
        }
    }

})
.attach(this);
```

A [custom node](../nodes/custom-nodes.md) can do the same in its `keyPressed` hook.

## Last input of a node

Each node records the last events dispatched to its UI, whether or not they happened over the node:

| Getter | Description |
|---|---|
| `getLastClickType()` | Button of the last press; `null` before the first one. |
| `getLastClickTime()` | Time of the last press, in milliseconds of the clock bridge. |
| `getLastKey()` | Last key; `null` before the first one. |
| `getLastCharacter()` | Character of the last key event. |
| `getLastKeyTime()` | Time of the last key event, in milliseconds of the clock bridge. |

## See also

- [Callbacks](callbacks.md)
- [Hover and Tooltips](hover.md)
- [The UI Class](../ui/ui-class.md)
- [Opening and Closing UIs](../ui/managing-uis.md)
- [UI Bridge](../integration/ui-bridge.md)