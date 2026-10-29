---
summary: Forwards the input of a GLFW window to the UI bridges of JOID, ahead of the GLFW callbacks the window already had.
---

Learn: [Input forwarder](/build/base-modules/lwjgl3-glfw/#input-forwarder), [Keep your own input handlers](/use/learn/running/embedding/#keep-your-own-input-handlers).

## attach(long)

Installs the character, cursor, scroll, key and mouse button callbacks of the window, and calls the callbacks that the window had before for every event that JOID does not consume.
When the window has a `CharMods` callback, JOID receives the characters through it, and the previous character callbacks only get the characters that JOID did not consume.
A key repeat never reaches JOID as a press: the previous callback receives it unless JOID consumed the press of that key.
The LWJGL 3 and Vulkan backends call it during `register()`.

## forwardKey(int,int)

Forwards one key event of GLFW, its key code and its action, to `BridgeHandler.UI`: `GLFW_PRESS` presses the key at that position with the key that the layout prints there, `GLFW_RELEASE` releases it, and `GLFW_REPEAT` asks whether JOID consumed its press.
Returns `true` when JOID consumed the event, which the host then drops.

## forwardMouseButton(int,int)

Forwards one mouse button event of GLFW, its button and its action, to `BridgeHandler.UI` as a press or a release.
Returns `true` when JOID consumed the event, which the host then drops.
