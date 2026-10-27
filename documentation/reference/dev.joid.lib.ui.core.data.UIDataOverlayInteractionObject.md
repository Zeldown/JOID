---
summary: The input options of an overlay for a running UI.
---
[[UIDataOverlayObject#interaction]] returns it.

Learn: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).

## create()

Creates the options with the default values of [[UIDataOverlayInteraction]].

## active

If `true`, the overlay receives the input.

## cancelClick

If `true`, a mouse input that the overlay consumed does not reach the host.

## cancelScroll

If `true`, a wheel turn that the overlay consumed does not reach the host.

## cancelKeyboard

If `true`, a key or a character that the overlay consumed does not reach the host.

## toString()

Returns the class and the value of every option, for logs.
