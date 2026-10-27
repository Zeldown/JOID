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

JOID records which presses it consumed, so that the host receives each key and button either completely or not at all:

| Event | Goes to | Consumed when |
|---|---|---|
| Mouse release | The UI bridges that received the press of that button. | One of them consumed the press or the release. |
| Key release | Every UI bridge, and every UI that received its press, even a node hidden since. | A UI bridge consumed the press of that key. |
| Escape on the top closeable screen | That screen, which closes when no node handles the key and the UI accepts to close. | Always: the host never receives it. |
| A press that an overlay lets through (its interaction does not cancel it) | The UIs below. | Never by that overlay, and its release neither. |

## Guarantees

| Rule | Why |
|---|---|
| Forward each event to JOID first; drop it when the call returns `true`, and give it to the host as if JOID did not exist when it returns `false`. | A consumed event that reaches the host acts twice: a click on a button also fires the weapon of a game behind it. |
| Keep the handlers that the window had before the backend registered, and call them only for the events that JOID did not consume. | The application keeps its own input behind JOID; the GLFW forwarder of [base-glfw](/build/base-modules/glfw/) does exactly that with the handlers set before `register()`. |
| Send to `keyPressed` the physical position, then the key that the layout prints there; on a layout that your library cannot name, send the position twice. | Games read the position to keep their movement keys under the hand, shortcuts and text read the printed key ([Keyboard](/use/learn/interactivity/keyboard/)); [[KeyLayout]] computes the printed key from the name that your library gives each physical key. |
| Send the position alone to `keyReleased`. | JOID keeps the printed key of every held position in a [[KeyState]], so the release reaches the UIs with the key of its press, even when the layout changed in between; that state also answers [[Key#isDown]] for the layout keys, and the [window bridge](/build/bridges/window/) answers it for the others. |
| Never forward a key repeat of the system as a `keyPressed`: ask [[UIBridgeRegistry#isKeyConsumed]], and drop the repeat when it returns `true`. | The text fields of JOID repeat the keys they handle themselves, so a forwarded repeat types twice. |
| Send a key press and, when the key types a character, a separate character event. | `charTyped` ignores control characters (``, ``, the codes of Ctrl combinations) and returns `false` for them: Enter, Backspace and shortcuts arrive as key presses. |
| Forward every release, even when your host ignores them. | Without the release of a button, JOID keeps a drag running. |

## Checks

| Symptom | Cause |
|---|---|
| A click on a JOID button also acts in the host | The host handles the event before it asks JOID, or ignores the return value. |
| A slider keeps following the mouse after the button is up | The release is not forwarded. |
| A held Backspace erases two characters at a time | The repeats of the system reach `keyPressed`. |
| Typing in a text field also triggers shortcuts of the host | The key press and the character are not both forwarded to JOID first. |
| A shortcut on `Key.Z` does not follow an AZERTY keyboard | The forwarder sends the position as the printed key. |
