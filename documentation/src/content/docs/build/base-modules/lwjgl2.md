---
title: "base-lwjgl2"
description: "The LWJGL 2 module: the OpenGL and OpenAL bindings, a Display window bridge with native cursors, the input forwarder and the key table."
genre: build
sidebar:
  order: 3
---

`joid-base-lwjgl2` holds what JOID needs from LWJGL 2, which ships as a single jar: the bindings of its OpenGL and its OpenAL, and the bridges of its `Display`, `Mouse` and `Keyboard`.
The LWJGL 2 backend uses it as it is, and so does an engine that runs on LWJGL 2 and creates the `Display` itself.
It depends on base-opengl, base-openal and the `lwjgl` jar of LWJGL 2.9, never on its natives.

## Bindings

| Binding | For |
|---|---|
| [[Lwjgl2GlBinding#inst]], with [[Lwjgl2CoreGlFrameBufferBinding]] and [[Lwjgl2ExtGlFrameBufferBinding]] | the render bridge of [base-opengl](/build/base-modules/opengl/#create-the-bridge) |
| [[Lwjgl2AlBinding#inst]] | the audio bridge of [base-openal](/build/base-modules/openal/) |

## Window

[[Lwjgl2WindowBridge]] multiplies the size and the mouse of `Display` by its pixel scale factor, to report screen pixels on high-density screens, and flips the mouse to a top-left origin.
Its constructor turns off the clipping of `Mouse` to the window, so that the mouse reports positions outside the window and a drag goes on past its edge, as with GLFW.
The clipboard goes through AWT; a clipboard that the system refuses reads as `""`, with one warning in dev mode.

## Cursors

LWJGL 2 has no system cursors.
The window bridge shows them through JNA, with a native implementation per platform (Windows, macOS, X11) set on the `Display`.
The jar of the module does not embed JNA: the LWJGL 2 backend embeds it, and a host that already has JNA on its classpath keeps its own.
Without JNA, the cursor stays the default one, with one warning in dev mode.

## Input forwarder

LWJGL 2 delivers input through queues that the loop of the application drains.
[[Lwjgl2InputForwarder#poll]] drains them once per frame, before `frame()`, and gives every event to JOID.
A host that handles the input too drains the queues itself and asks JOID for each event: [[Lwjgl2InputForwarder#forwardMouseEvent]] after `Mouse.next()`, [[Lwjgl2InputForwarder#forwardKeyboardEvent]] after `Keyboard.next()`, and it handles the event only when the call returns `false`.

- A mouse event becomes a move, a press or a release, and the wheel becomes `mouseScroll(0, wheel / 120)`.
- A key event becomes a press or a release, and a press that types a character is followed by its character.
- A repeat event asks `isKeyConsumed` instead of pressing again, and sends its character, so an application can turn `Keyboard.enableRepeatEvents(true)` on without a double repeat.
- An event without key, which LWJGL 2 sends for a character alone, sends only its character.

## Keys

[[Lwjgl2Keys]] maps the key codes of LWJGL 2 to [[Key]] values.
LWJGL 2 cannot name a key by layout, so `getKey(code, character)` reads the printed key from the character that the press types, and remembers it for that position.
With Ctrl, a letter types a control character that names its letter.
While Shift or Alt is held, or Ctrl on another key, the character does not name the key, so the remembered key serves; before the first press of a key without a modifier, its position serves.
