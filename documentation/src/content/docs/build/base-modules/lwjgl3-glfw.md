---
title: "base-lwjgl3-glfw"
description: "The GLFW module: a window bridge in screen pixels, an input forwarder that chains to the GLFW callbacks of the application, the key table and hidden windows for tests."
genre: build
sidebar:
  order: 4
---

`joid-base-lwjgl3-glfw` turns a GLFW window, through LWJGL 3, into the window bridge and the input of JOID.
The LWJGL 3 and Vulkan backends use it as it is; [base-lwjgl2](/build/base-modules/lwjgl2/) does the same for the `Display` of LWJGL 2.

## Window bridge

[[GlfwWindowBridge]] takes the handle of an existing window: `new GlfwWindowBridge(window)`.

| Contract | How |
|---|---|
| Size | `glfwGetFramebufferSize`: screen pixels. |
| Mouse | `glfwGetCursorPos`, converted from window coordinates to screen pixels; while the input forwarder forwards a mouse event, the position of the last cursor event of the window instead. |
| Held keys | [[GlfwKeys#isKeyDown]], by physical position. |
| Grabbed mouse | `true` while the cursor mode is `GLFW_CURSOR_DISABLED`. |
| Cursors | The standard cursors of GLFW, created on first use and kept; [[GlfwWindowBridge#destroy]] releases them. Before GLFW 3.4, which has no move, not-allowed nor diagonal resize cursor, those four show the default cursor. |
| Clipboard | The clipboard of GLFW; an empty string when it holds no text. |

[Window bridge](/build/bridges/window/) gives the contract these rows fulfill.

## Input forwarder

[[GlfwInputForwarder#attach]] installs the character, cursor, scroll, key and mouse button callbacks of a GLFW window and forwards each event to [[BridgeHandler#UI]]:

- a key press becomes `keyPressed(position, printed key)`, a release `keyReleased(position)`;
- a key repeat never reaches JOID as a press: the forwarder asks `isKeyConsumed` whether the callback of the application may receive it;
- a button press becomes `mousePressed`, a release `mouseReleased`;
- a move, a button and a scroll reach JOID at the position of the last cursor event, which GLFW sends before them, rather than at the position that the system gives later in the frame; the cursor leaving the window clears that position;
- the scroll offsets go through as notches, fractions included, and the characters as whole code points.

The forwarder keeps the callbacks that the window had before `attach` and calls them for every event that JOID does not consume.
An application that sets its callbacks before registering the backend therefore keeps them, behind JOID ([Input forwarding](/build/input/#guarantees)).
When the window has a `CharMods` callback, JOID takes the characters there, since GLFW calls it before the character callback, and holds back both previous callbacks for a character it consumed.

A host that receives the events of GLFW through a system of its own, or that sets its callbacks after `attach`, forwards each event itself: [[GlfwInputForwarder#forwardKey]] and [[GlfwInputForwarder#forwardMouseButton]] take the code and the action of GLFW, and return `true` when JOID consumed the event.
The other events need no translation: give them to `mouseMoved()`, `mouseScroll(x, y)` and `charTyped(codepoint)` of [[BridgeHandler#UI]].

## Keys

[[GlfwKeys]] maps the key codes of GLFW to [[Key]] values and back.
`getPhysicalKey(code)` gives the position, and `getKey(code)` the printed key, through a [[KeyLayout]] fed by `glfwGetKeyName`: `Key.A` is the key that types `a`, wherever it sits.
Its position table (`getPhysicalKey`, `getCode`, `isKeyDown`) needs no GLFW call, so a backend whose engine reports the key codes of GLFW reuses it with a [[KeyLayout]] of its own, as the forwarder of [Input forwarding](/build/input/) does; `getKey` reads the layout through GLFW.

## Hidden windows

[[GlfwSnapshotWindow]] opens a hidden, undecorated window of a fixed size for the snapshot backends of the tests; a `Runnable` sets the hints of the backend before the window exists.
Undecorated, the window gives its whole surface to the framebuffer; on a high-density macOS screen, the framebuffer is larger than the size asked for.
