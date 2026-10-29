---
title: "Input forwarding"
description: "Forward the input of a window to JOID: press, release, repeats, characters, and what the host does with an event JOID consumed."
genre: build
sidebar:
  order: 3
---

JOID reads no input device.
The window, or the host, receives the events and forwards each one to [[BridgeHandler#UI]], the [[UIBridgeRegistry]], which answers whether JOID consumed it.
An event that JOID consumed must not reach the host.

## Required

This forwarder listens to the events of an imaginary engine, whose listeners return `true` to stop an event, and whose key codes are those of GLFW:

```java file="com/example/backend/EngineInputForwarder.java" region="forwarder"
```

The backend connects it in `attachInput()` ([Writing a backend](/build/writing-a-backend/#forward-the-input)).
On GLFW and on LWJGL 2, the base modules translate one event of the library into these calls and return the same answer: [[GlfwInputForwarder#forwardKey]] and [[GlfwInputForwarder#forwardMouseButton]] in [base-lwjgl3-glfw](/build/base-modules/lwjgl3-glfw/#input-forwarder), [[Lwjgl2InputForwarder#forwardMouseEvent]] and [[Lwjgl2InputForwarder#forwardKeyboardEvent]] in [base-lwjgl2](/build/base-modules/lwjgl2/#input-forwarder).

| Event of your window | Call | Arguments |
|---|---|---|
| Key pressed | [[UIBridgeRegistry#keyPressed]] | the [[Key]] at that physical position, then the key that the layout prints there |
| Key repeated | [[UIBridgeRegistry#isKeyConsumed]] | the physical key |
| Key released | [[UIBridgeRegistry#keyReleased]] | the physical key |
| Character typed | [[UIBridgeRegistry#charTyped]] | the Unicode code point, a whole code point even outside the BMP |
| Mouse moved | [[UIBridgeRegistry#mouseMoved]] | none: JOID reads the position from the window bridge |
| Button pressed | [[UIBridgeRegistry#mousePressed]] | [[MouseButton#of]] of the button number: 0 left, 1 right, 2 middle, 3 back, 4 forward |
| Button released | [[UIBridgeRegistry#mouseReleased]] | the same button |
| Wheel | [[UIBridgeRegistry#mouseScroll]] | horizontal and vertical notches as GLFW reports them (positive is up), fractions allowed |

Each call returns `true` when JOID consumed the event.

## Provided

JOID records which presses it consumed, so that the host receives each key and button either completely or not at all: the release of a press that JOID consumed is consumed, and the release of a press that the host received is never consumed, even when a UI consumes the release itself.

| Event | Goes to | Consumed when |
|---|---|---|
| Mouse release | The UI bridges that received the press of that button. | One of them consumed the press. |
| Key release | Every UI bridge, and every UI that received its press, even a node hidden since. | A UI bridge consumed the press of that key. |
| Escape on the top closeable UI that is not an overlay | That UI, which closes when no node handles the key and the UI accepts to close. | Always: the host never receives it. |
| A press that an overlay lets through (its interaction does not cancel it) | The UIs below when no node of the overlay consumed it; the lower bridges and the host in every case. | Never by that overlay, and its release neither. |

## Guarantees

| Rule | Why |
|---|---|
| Forward each event to JOID first; drop it when the call returns `true`, and give it to the host as if JOID did not exist when it returns `false`. | A consumed event that reaches the host acts twice: a click on a button also fires the weapon of a game behind it. |
| Keep the handlers that the window had before the backend registered, and call them only for the events that JOID did not consume. | The application keeps its own input behind JOID; the GLFW forwarder of [base-lwjgl3-glfw](/build/base-modules/lwjgl3-glfw/) does exactly that with the handlers set before `register()`. |
| While you forward a mouse event, make the [window bridge](/build/bridges/window/) report the position of that event, not the newest position of the device. | At every mouse call, JOID reads the position from the window bridge and finds the hovered nodes again: a move and a press that arrive in the same frame each reach the node under their own position. The forwarders of [base-lwjgl2](/build/base-modules/lwjgl2/#input-forwarder) and [base-lwjgl3-glfw](/build/base-modules/lwjgl3-glfw/#input-forwarder) do it. |
| Send to `keyPressed` the physical position, then the key that the layout prints there; on a layout that your library cannot name, send the position twice. | Games read the position to keep their movement keys under the hand, shortcuts and text read the printed key ([Keyboard and shortcuts](/use/learn/interactivity/keyboard/)); [[KeyLayout]] computes the printed key from the name that your library gives each physical key. |
| Send the position alone to `keyReleased`. | JOID keeps the printed key of every held position in a [[KeyState]], so the release reaches the UIs with the key of its press, even when the layout changed in between; that state also answers [[Key#isDown]] for the layout keys, and the [window bridge](/build/bridges/window/) answers it for the others. |
| Never forward a key repeat of the system as a `keyPressed`: ask [[UIBridgeRegistry#isKeyConsumed]], and drop the repeat when it returns `true`. | The text fields of JOID repeat the keys they handle themselves, so a forwarded repeat types twice. |
| Send a key press and, when the key types a character, a separate character event. | `charTyped` ignores the control characters (Enter `U+000D`, Backspace `U+0008`, the codes of Ctrl combinations) and returns `false` for them: Enter, Backspace and shortcuts arrive as key presses. |
| Forward every release, even when your host ignores them. | Without the release of a button, JOID keeps a drag running. |

## Checks

| Symptom | Cause |
|---|---|
| A click on a JOID button also acts in the host | The host handles the event before it asks JOID, or ignores the return value. |
| A slider keeps following the mouse after the button is up | The release is not forwarded. |
| A fast click reaches the node where the pointer went right after it | The window bridge reports the newest position of the device while the event is forwarded, instead of the position of the event. |
| A held Backspace erases two characters at a time | The repeats of the system reach `keyPressed`. |
| Typing in a text field also triggers shortcuts of the host | The key press and the character are not both forwarded to JOID first. |
| A shortcut on `Key.Z` does not follow an AZERTY keyboard | The forwarder sends the position as the printed key. |
