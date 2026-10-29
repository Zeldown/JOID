---
summary: The native cursors of X11, from the cursor theme of libXcursor or else from the cursor font.
---

Learn: [Cursors](/build/base-modules/lwjgl2/#cursors).

## X11NativeCursor()

Looks up libX11 and, when present, libXcursor through JNA; without libXcursor, prints a dev warning and uses the cursor font.
