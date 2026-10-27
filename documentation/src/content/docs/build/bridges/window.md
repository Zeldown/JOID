---
title: "Window bridge"
description: "The contract of the window bridge: drawable size, mouse, held keys, clipboard and cursor, all in screen pixels."
genre: build
sidebar:
  order: 2
---

The window bridge tells the core how large the drawable surface is, where the mouse is and which keys are held.
The core reads it through [[IWindowBridge]], from `BridgeHandler.WINDOW`.
It never creates a window: the application or the host owns it.

This bridge reads the window of an imaginary engine, whose key codes are those of GLFW, so the key table of [base-glfw](/build/base-modules/glfw/) serves it:

```java file="com/example/backend/EngineWindowBridge.java" region="window"
```

## Required

| Member | What it returns or does |
|---|---|
| [[IWindowBridge#getWidth]], [[IWindowBridge#getHeight]] | The size of the drawable surface, in screen pixels; `0` while the window is minimized. |
| [[IWindowBridge#getMouseX]], [[IWindowBridge#getMouseY]] | The mouse position, in the same pixels, from the top-left corner. |
| [[IWindowBridge#isMouseGrabbed]] | `true` while the host hides and locks the cursor, during a camera control for example. |
| [[IWindowBridge#isKeyDown]] | Whether the key at that physical position is held, whatever the keyboard layout. |
| [[IWindowBridge#getClipboard]], [[IWindowBridge#setClipboard]] | The text of the system clipboard; an empty string when it holds no text. |

## Provided

| Member | Default | Override it to |
|---|---|---|
| [[IWindowBridge#setCursor]] | Does nothing. | Show a system [[Cursor]]. |

The rest of the core only reads the window bridge:

- [[UIBridgeRegistry#frame]] compares the size with the previous frame and, when it changed and is not zero, resizes the projection and lays every UI out again;
- each UI converts the mouse position into design pixels;
- [[Key#isDown]] asks `isKeyDown` for the keys that do not depend on the layout, and the key state of the UI registry for the others ([Input forwarding](/build/input/#send-the-position-and-the-printed-key));
- the cursor is set once per change, and never while `isMouseGrabbed()` returns `true`.

## Guarantees

| Rule | Why |
|---|---|
| Report the size and the mouse in the same unit, screen pixels. | On a high-density screen, the size that the system gives the window and its size in screen pixels differ; mixing them shifts the hover by the scale factor. |
| Put the origin of the mouse at the top-left corner, `y` growing downwards, from `0` to `height - 1`. | The design canvas grows downwards. |
| Answer `isKeyDown` by physical position, never through the layout. | The core translates the layout keys itself, from what the input forwarder sends. |
| Never throw from `getClipboard` or `setClipboard`. | A text field reads it on every paste; a clipboard the system refuses reads as `""`. |

On GLFW, the window bridge of [base-glfw](/build/base-modules/glfw/) respects every rule.
The input events do not go through the window bridge: [Input forwarding](/build/input/) covers them.
