---
summary: The window bridge of a GLFW window, which reads its size, mouse, keys, clipboard and cursor in screen pixels.
---

The LWJGL 3 and Vulkan backends create it from the handle of your window.

Learn: [Window bridge](/build/base-modules/lwjgl3-glfw/#window-bridge).

## GlfwWindowBridge(long)

Creates the bridge of an existing GLFW window, from its handle.

## destroy()

Destroys the cursors that the bridge created; call it before you destroy the window.

## getWidth()

Returns the width of the framebuffer of the window, in screen pixels.

## getHeight()

Returns the height of the framebuffer of the window, in screen pixels.

## getMouseX()

Returns the horizontal position of the cursor, converted from window coordinates to screen pixels; while the input forwarder forwards a mouse event, the position of that event.

## getMouseY()

Returns the vertical position of the cursor, converted from window coordinates to screen pixels; while the input forwarder forwards a mouse event, the position of that event.

## isMouseGrabbed()

If `true`, the cursor mode of the window is `GLFW_CURSOR_DISABLED`.

## isKeyDown(Key)

If `true`, GLFW reports the key at that physical position as pressed; a key that GLFW does not know is never down.

## getClipboard()

Returns the clipboard of GLFW, or an empty string when it holds no text.

## setClipboard(String)

Puts the text in the clipboard of GLFW.
