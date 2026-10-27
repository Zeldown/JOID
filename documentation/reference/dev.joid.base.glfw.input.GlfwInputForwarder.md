---
summary: Forwards the input callbacks of a GLFW window to the UI bridges of JOID, ahead of the callbacks the window already had.
---

Learn: [Input forwarder](/build/base-modules/glfw/#input-forwarder), [Keep your own input handlers](/use/learn/running/embedding/#keep-your-own-input-handlers).

## attach(long)

Installs the character, cursor, scroll, key and mouse button callbacks of the window, and calls the callbacks that the window had before for every event that JOID does not consume.
A key repeat never reaches JOID as a press: the previous callback receives it unless JOID consumed the press of that key.
The LWJGL 3 and Vulkan backends call it during `register()`.
