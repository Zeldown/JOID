---
title: "Window bridge"
description: "The contract of the window bridge: drawable size, mouse, held keys, clipboard, cursor and text input area, all in screen pixels."
genre: build
sidebar:
  order: 2
---

The window bridge tells the core how large the drawable surface is, where the mouse is and which keys are held.
The core reads it through [[IWindowBridge]], from [[BridgeHandler#WINDOW]].
It never creates a window: the application or the host owns it.

This bridge reads the window of an imaginary engine, whose key codes are those of GLFW, so the key table of [base-lwjgl3-glfw](/build/base-modules/lwjgl3-glfw/) serves it:

```java file="com/example/backend/EngineWindowBridge.java" region="window"
```

## Required

| Member | What it returns or does |
|---|---|
| [[IWindowBridge#getWidth]], [[IWindowBridge#getHeight]] | The size of the drawable surface, in screen pixels; `0` while the window is minimized. |
| [[IWindowBridge#getMouseX]], [[IWindowBridge#getMouseY]] | The mouse position, in the same pixels, from the top-left corner; while a mouse event is forwarded, the position of that event ([Input forwarding](/build/input/#guarantees)). |
| [[IWindowBridge#isMouseGrabbed]] | `true` while the host hides and locks the cursor, during a camera control for example. |
| [[IWindowBridge#isKeyDown]] | Whether the key at that physical position is held, whatever the keyboard layout. |
| [[IWindowBridge#getClipboard]], [[IWindowBridge#setClipboard]] | The text of the system clipboard; an empty string when it holds no text. |

## Provided

| Member | Default | Override it to |
|---|---|---|
| [[IWindowBridge#setCursor]] | Does nothing. | Show a system [[Cursor]]. |
| [[IWindowBridge#setTextInputArea]] | Does nothing. | Switch the text input of the host on at the area of the cursor of the focused field, so that the window of the input method opens next to it, and off on `null`. |

The rest of the core only reads the window bridge:

- [[UIBridgeRegistry#frame]] compares the size with the previous frame and, when it changed and is not zero, resizes the projection and lays every UI out again;
- each UI converts the mouse position into design pixels, on every frame and on every mouse event, and finds its hovered nodes from it;
- [[Key#isDown]] asks `isKeyDown` for the keys that do not depend on the layout, and the key state of the UI registry for the others ([Input forwarding](/build/input/#guarantees));
- the cursor is set once per change, and never while `isMouseGrabbed()` returns `true`;
- the text input area is set once per change: the area of the cursor of the focused field of the front-most UI that takes input, in screen pixels, or `null` once no UI has a focused field.

## Guarantees

| Rule | Why |
|---|---|
| Report the size and the mouse in the same unit, screen pixels. | On a high-density screen, the size that the system gives the window and its size in screen pixels differ; mixing them shifts the hover by the scale factor. |
| Put the origin of the mouse at the top-left corner, `y` growing downwards. | The design canvas grows downwards. |
| Report the position the host gives, even outside `0` to the size: the core assumes no bound. | A drag goes on when the pointer leaves the window; a position held at the edge would keep the node of the edge hovered. |
| Answer `isKeyDown` by physical position, never through the layout. | The core translates the layout keys itself, from what the input forwarder sends. |
| Never throw from `getClipboard` or `setClipboard`. | A text field reads it on every paste; a clipboard the system refuses reads as `""`. |

On GLFW, the window bridge of [base-lwjgl3-glfw](/build/base-modules/lwjgl3-glfw/) respects every rule.
The input events do not go through the window bridge: [Input forwarding](/build/input/) covers them.
