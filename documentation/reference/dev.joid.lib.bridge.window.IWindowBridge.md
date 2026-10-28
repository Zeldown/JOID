---
summary: The bridge that reads the drawable size, the mouse, the held keys and the clipboard of the window, in screen pixels.
---

It never creates a window: the backend or the host owns it.
The input events do not go through it; the backend forwards them to `BridgeHandler.UI`.
[Window bridge](/build/bridges/window/) shows one that reads everything from the engine that owns the window.

Learn: [Window bridge](/build/bridges/window/), [Input forwarding](/build/input/).

## getWidth()

Returns the width of the drawable surface, in screen pixels; `0` while the window is minimized.

## getHeight()

Returns the height of the drawable surface, in screen pixels; `0` while the window is minimized.

## getMouseX()

Returns the horizontal position of the mouse, in screen pixels from the left edge.

## getMouseY()

Returns the vertical position of the mouse, in screen pixels from the top edge.

## isMouseGrabbed()

If `true`, the host hides and locks the cursor, for a camera for example, and JOID leaves the cursor alone.

## isKeyDown(Key)

If `true`, the key at that physical position is held, whatever the keyboard layout.

## getClipboard()

Returns the text of the system clipboard, or an empty string when it holds no text; never throws.

## setClipboard(String)

Puts the text in the system clipboard; never throws.

## setCursor(Cursor)

Shows that system cursor over the window; does nothing by default.
JOID calls it once per change, never while `isMouseGrabbed()` returns `true`.
