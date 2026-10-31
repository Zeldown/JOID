---
title: "base-lwjgl3-sdl"
description: "The SDL 3 module: the key table and the mouse buttons of SDL, for a host whose window and input run on the SDL of LWJGL."
genre: build
sidebar:
  order: 7
---

`joid-base-lwjgl3-sdl` translates the input codes of SDL 3, through the `lwjgl-sdl` bindings of LWJGL, into the keys and the mouse buttons of JOID.
It holds no window bridge and no input forwarder: a host on SDL owns its window and its event loop, so its backend writes the window bridge and forwards each event itself ([Input forwarding](/build/input/)), with these two tables.

The module compiles against `lwjgl-sdl` 3.4 and embeds nothing: the host gives LWJGL, its SDL bindings and their natives at run time.

## Keys

[[SdlKeys]] maps the scancodes of SDL to [[Key]] values and back.
`getPhysicalKey(scancode)` gives the position, and `getKey(scancode)` the printed key, through a [[KeyLayout]] fed by `SDL_GetKeyFromScancode`: `Key.A` is the key that types `a`, wherever it sits.
Its position table (`getPhysicalKey`, `getCode`, `isKeyDown`) needs no SDL call, so a window bridge answers `isKeyDown` with it and the key state of its host; `getKey` reads the layout through SDL.

A key event of SDL becomes `keyPressed(SdlKeys.getPhysicalKey(scancode), SdlKeys.getKey(scancode))` or `keyReleased(...)` on [[BridgeHandler#UI]].

## Mouse buttons

[[SdlMouseButtons#getButton]] turns the button index of SDL, which counts from 1 with the middle button before the right one, into a [[MouseButton]] for `mousePressed` and `mouseReleased`.

## Text input

SDL sends characters only while its text input is on.
Turn it on and off from [[IWindowBridge#setTextInputArea]] of the window bridge: JOID gives the area of the cursor of the focused field when a field is focused or its cursor moves, and `null` once no field has it; pass the area to `SDL_SetTextInputArea`, so that the window of the input method opens next to the text.
