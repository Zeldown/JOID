---
summary: Maps the key codes of LWJGL 2 to the keys of JOID, by physical position or through the character that the key typed.
---

LWJGL 2 does not tell the layout, so the key of the layout comes from the character of each press, remembered for the presses with a modifier.

Learn: [Keys](/build/backends/lwjgl2/#keys).

## getPhysicalKey(int)

Returns the key at the physical position of the LWJGL 2 code, or `Key.UNKNOWN`.

## getKey(int,char)

Returns the key that the press typed on the current layout: the key of the character for a letter or symbol key, the key of the position for the others.
With Control, a letter types a control character, which gives its letter.
With Shift or Alt held, or Control on another key, it returns the key that this position typed last without a modifier.

## isKeyDown(Key)

If `true`, LWJGL 2 reports the key at that physical position as pressed.
