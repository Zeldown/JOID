---
summary: Sets the system cursors that LWJGL 2 cannot set by itself, through JNA and the native cursor of LWJGL 2.
---

Learn: [Cursors](/build/backends/lwjgl2/#cursors).

## forPlatform()

Returns the cursor implementation of the current system, or `null` when JNA is missing, with a dev warning: the cursor then stays the default one.

## NativeCursor()

Creates an implementation, for a subclass.

## apply(Cursor)

Shows the cursor over the `Display`, loading its native handle once; after one failure, prints a dev warning and stops trying.

## load(Cursor)

JOID calls it the first time a cursor is shown.
Return the native handle of that system cursor.
