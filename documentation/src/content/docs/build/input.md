---
title: "Input forwarding"
description: "Forward the input of a window to JOID: press, release, repeats, characters, and what the host does with an event JOID consumed."
genre: build
sidebar:
  order: 3
---

JOID reads no input device.
The window, or the host, receives the events and forwards each one to `BridgeHandler.UI`, the [[UIBridgeRegistry]], which answers whether JOID consumed it.
An event JOID consumed must not reach the host: a click on a button must not also fire the weapon of a game behind it.

## Forward every event

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

## Respect the consumption

1. Forward the event to JOID first.
2. When the call returns `true`, drop the event: the host does not see it.
3. When it returns `false`, give it to the host as if JOID did not exist.

When the window already had handlers before the backend registered, keep them and call them only for the events that JOID did not consume.
The GLFW forwarder of [base-glfw](/build/base-modules/glfw/) does exactly that with the callbacks set before `register()`.

## Send the position and the printed key

`keyPressed` takes two keys.
The first is the physical position, which games read to keep their movement keys under the hand; the second is the key that the layout prints there, which shortcuts and text read ([Keyboard](/use/learn/interactivity/keyboard/) gives the user side).
[[KeyLayout]] computes the second from the first, given the name your library gives each physical key; on a layout that your library cannot name, send the position twice.

JOID keeps the printed key of every held position in a [[KeyState]], so `keyReleased` takes the position alone: the release reaches the UIs with the key of its press, even when the layout changed in between.
That state also answers [[Key#isDown]] for the layout keys; the window bridge answers it for the others ([Window bridge](/build/bridges/window/)).

## Let JOID repeat the keys

Never forward a key repeat of the system as a `keyPressed`: the text fields of JOID repeat the keys they handle themselves, so a forwarded repeat types twice.
Ask [[UIBridgeRegistry#isKeyConsumed]] instead: it returns `true` when JOID consumed the press of that key, and the host then drops the repeat too.

## Characters and keys are two events

Send a key press and, when the key types a character, a separate character event.
`charTyped` ignores control characters (`\r`, `\b`, the codes of Ctrl combinations) and returns `false` for them: Enter, Backspace and shortcuts arrive as key presses.

## Releases follow their press

JOID remembers which presses it consumed, so that the host sees each key and button either completely or not at all.

- A mouse release goes to the UI bridges that received the press of that button, and is consumed when one of them consumed the press or the release.
- A key release goes to every UI bridge, and reaches every UI that received its press, even a node hidden since; it is consumed only when a UI bridge consumed the press of that key.
- Escape on the top closeable screen is always consumed, and the host never sees it; the screen closes when no node handles the key and the UI accepts to close.
- A press that an overlay lets through (its interaction does not cancel it) is not consumed, so its release is not either.

Forward every release, even when your host ignores them: a host that forwards only presses leaves JOID believing that a drag is still running.

## Checks

| Symptom | Cause |
|---|---|
| A click on a JOID button also acts in the host | The host handles the event before it asks JOID, or ignores the return value. |
| A slider keeps following the mouse after the button is up | The release is not forwarded. |
| A held Backspace erases two characters at a time | The repeats of the system reach `keyPressed`. |
| Typing in a text field also triggers shortcuts of the host | The key press and the character are not both forwarded to JOID first. |
| A shortcut on `Key.Z` does not follow an AZERTY keyboard | The forwarder sends the position as the printed key. |
