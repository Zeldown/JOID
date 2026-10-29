---
summary: The window bridge of the `Display` of LWJGL 2, in screen pixels, with the clipboard of AWT and native cursors.
---

Learn: [Window](/build/base-modules/lwjgl2/#window), [Cursors](/build/base-modules/lwjgl2/#cursors).

## Lwjgl2WindowBridge()

Creates the window bridge of the current LWJGL 2 display, without changing anything in `Display` nor in `Mouse`.

## getWidth()

Returns the width of the `Display` times its pixel scale factor.

## getHeight()

Returns the height of the `Display` times its pixel scale factor.

## getMouseX()

Returns the horizontal position of the mouse, in screen pixels from the left edge.

## getMouseY()

Returns the vertical position of the mouse, in screen pixels from the top edge.

## isMouseGrabbed()

If `true`, LWJGL 2 grabs the mouse.

## isKeyDown(Key)

If `true`, LWJGL 2 reports the key at that physical position as pressed.

## getClipboard()

Returns the text of the system clipboard through AWT, or an empty string; with the dev jar, prints once when the clipboard cannot be read.

## setClipboard(String)

Puts the text in the system clipboard through AWT; with the dev jar, prints once when the clipboard cannot be written.
