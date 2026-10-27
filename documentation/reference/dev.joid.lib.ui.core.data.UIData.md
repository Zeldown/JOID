---
summary: Annotation that configures how a UI is drawn, receives the input and closes.
---
Put it on a [[UI]] class; a subclass inherits it unless it declares its own.
JOID reads it into a [[UIDataObject]], returned by [[UI#getData]], whose setters change the options while the UI runs.

Learn: [UIs](/use/learn/describing-the-ui/uis/#configure-a-ui).

## active()

If `false`, the UI is drawn but receives no input, and the UIs below it receive the input instead.

## visible()

If `false`, the UI is neither drawn nor sent any input, and closing it skips its closing transition.

## closeable()

If `false`, Escape leaves the UI open; [[JOID#close]] still closes it.
An overlay never closes with Escape.

## zoomable()

If `false`, the zoom keys of dev mode (Ctrl or Alt with + and -, and Alt with the wheel) leave the zoom of the UI unchanged.

## projection()

If `true`, the UI sets its own projection over the window; if `false`, it draws inside the projection that the host has set.

## background()

If `true`, the UI draws the UI backdrop over the whole window, behind its nodes, in `backgroundColor`; the backdrop fades with the transition of the UI.

## backgroundColor()

Color of the UI backdrop, as a hexadecimal string `#RRGGBB` or `#RRGGBBAA`.

## zindex()

Order of the UI inside its group of the stack of its bridge (overlays below the screens, screens and popups, overlays above them): a higher value is drawn above and receives the input first.

## zlevel()

Depth, along the Z axis, added before the UI is drawn, in the units of the projection of the host.

## anchorX()

Side of the window that the canvas is pinned to when the window is wider than 16:9, and that the zoom keeps in place.

## anchorY()

Side of the window that the canvas is pinned to when the window is taller than 16:9, and that the zoom keeps in place.
