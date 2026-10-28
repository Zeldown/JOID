---
summary: Maps the key codes of GLFW to the keys of JOID and back, by physical position or through the keyboard layout.
---

Its position table needs no GLFW call, so a backend whose engine reports GLFW key codes reuses it; `getKey` reads the layout through GLFW.

Learn: [Keys](/build/base-modules/glfw/#keys).

## getCode(Key)

Returns the GLFW key code at the physical position of the key, or `GLFW_KEY_UNKNOWN` for a key that GLFW does not name.

## getKey(int)

Returns the key that the code types on the current keyboard layout, read with `glfwGetKeyName`: on an AZERTY keyboard, the code of `Q` gives `Key.A`.

## getPhysicalKey(int)

Returns the key at the physical position of the code, whatever the layout, or `Key.UNKNOWN`.

## isKeyDown(Key,IntPredicate)

If `true`, the key has a GLFW code and the predicate reports that code as pressed.
