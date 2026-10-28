---
title: "LWJGL 2 backend"
description: "What the LWJGL 2 backend is made of: the OpenGL and OpenAL modules, a Display window bridge, polled input, bundled natives and native cursors."
genre: build
sidebar:
  order: 1
---

`joid-backend-lwjgl2` runs JOID on LWJGL 2.9.1, for applications built on its `Display`.
How an application installs and starts it is on [Choosing a backend](/use/learn/running/backends/).

## What it is made of

| Bridge | Class | From |
|---|---|---|
| Render | [[GlRenderBridge]] over [[Lwjgl2GlBinding]] | [base-opengl](/build/base-modules/opengl/) |
| Audio | [[AlAudioBridge]] over [[Lwjgl2AlBinding]] | [base-openal](/build/base-modules/openal/) |
| Window | [[Lwjgl2WindowBridge]] | this backend |
| Input | [[Lwjgl2InputForwarder]], called by the loop | this backend |

[[Lwjgl2Backend]] registers them; its `create()` takes no window, because LWJGL 2 has a single `Display`.
The backend keeps the default thread bridge and the default UI bridge of [[Backend]].

## What differs

### Natives

The jar bundles the LWJGL 2 natives for Windows, Linux and macOS.
[[Lwjgl2Natives#install]] installs them; whoever creates the `Display` calls it before `Display.create()`, and the backend never calls it itself:

1. When `org.lwjgl.librarypath` is set, or the natives are already on `java.library.path`, it does nothing.
2. Otherwise it extracts them into the cache folder of JOID ([Where JOID stores files](/use/learn/running/files/)), under `lwjgl`, and sets `org.lwjgl.librarypath`.

An extraction that fails throws an `IllegalStateException` that names the folder, and the next call tries again.

### Input belongs to the loop

LWJGL 2 delivers input through queues that the application loop drains, so the backend attaches nothing in `attachInput()`.
The application calls [[Lwjgl2InputForwarder#poll]] once per frame, before `frame()`:

- each mouse event becomes a move, a press or a release, and the wheel becomes `mouseScroll(0, wheel / 120)`;
- each key event becomes a press or a release, and a press that types a character is followed by its character;
- a repeat event of LWJGL 2 sends its character only, so an application can turn `Keyboard.enableRepeatEvents(true)` on without a double repeat.

The forwarder returns nothing to the loop: LWJGL 2 has no handler to chain to.

### Keys

[[Lwjgl2Keys]] maps the key codes of LWJGL 2 to [[Key]] values.
LWJGL 2 cannot name a key by layout, so `getKey(code, character)` reads the printed key from the character that the press types, and remembers it for that position.
With Ctrl, a letter types a control character that names its letter.
While Shift or Alt is held, or Ctrl on another key, the character does not name the key, so the remembered key serves; before the first press of a key without a modifier, its position serves.

### Window

[[Lwjgl2WindowBridge]] multiplies the size and the mouse of `Display` by its pixel scale factor, to report screen pixels on high-density screens, and flips the mouse to a top-left origin.
The clipboard goes through AWT; a clipboard that the system refuses reads as `""`, with one warning in dev mode.

### Cursors

LWJGL 2 has no system cursors.
The backend shows them through JNA, which its jar embeds, with a native implementation per platform (Windows, macOS, X11) set on the `Display`.
Without JNA, the cursor stays the default one, with one warning in dev mode.

### Context

The `Display` and the OpenGL context that the backend needs are on [Check the prerequisites](/use/learn/running/backends/#check-the-prerequisites).

## Tests

[[Lwjgl2SnapshotBackend]] renders into a `Display` on Linux and into a `Pbuffer` elsewhere, both with a 24-bit depth and an 8-bit stencil.
[Testing a backend](/build/testing-a-backend/#the-suites) runs the contract suites and the snapshot suite on your backend.
