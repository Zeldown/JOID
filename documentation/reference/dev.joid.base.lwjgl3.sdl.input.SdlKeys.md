---
summary: Maps the scancodes of SDL 3 to the keys of JOID and back, by physical position or through the keyboard layout.
---

Its position table needs no SDL call, so a backend whose engine reports SDL scancodes reuses it; `getKey` reads the layout through SDL.

Learn: [Keys](/build/base-modules/lwjgl3-sdl/#keys).

## getCode(Key)

Returns the SDL scancode at the physical position of the key, or `SDL_SCANCODE_UNKNOWN` for a key that SDL does not name.

## getKey(int)

Returns the key that the scancode types on the current keyboard layout, read with `SDL_GetKeyFromScancode`: on an AZERTY keyboard, the scancode of `Q` gives `Key.A`.

## getPhysicalKey(int)

Returns the key at the physical position of the scancode, whatever the layout, or `Key.UNKNOWN`.

## isKeyDown(Key,IntPredicate)

If `true`, the key has an SDL scancode and the predicate reports that scancode as pressed.
