---
summary: The window bridge of the snapshot runner, whose size, mouse, held keys, cursor and clipboard the commands set.
---

Learn: [Testing your UIs](/use/learn/running/testing/).

## SnapshotWindowBridge()

Creates a simulated window of size 0 × 0, with the default cursor and an empty clipboard.

## getWidth()

Returns the width of the window, in screen pixels.

## setWidth(int)

Sets the width of the window.

## getHeight()

Returns the height of the window, in screen pixels.

## setHeight(int)

Sets the height of the window.

## getCursor()

Returns the last cursor that JOID set, which the `cursor` command checks.

## setCursor(Cursor)

Records the cursor that JOID shows.

## isMouseGrabbed()

Returns `false`.

## isKeyDown(Key)

If `true`, a command holds that key.

## getKeys()

Returns the held keys, which the commands change.

## getMouseX()

Returns the horizontal position of the mouse.

## setMouseX(double)

Sets the horizontal position of the mouse, in screen pixels.

## getMouseY()

Returns the vertical position of the mouse.

## setMouseY(double)

Sets the vertical position of the mouse, in screen pixels.

## getClipboard()

Returns the text of the clipboard, empty at first.

## setClipboard(String)

Sets the text of the clipboard.
