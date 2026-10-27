---
summary: The input options of an overlay for a running UI.
---
[[UIDataOverlayObject#getInteraction]] returns it.

Learn: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).

## create()

Creates the options with the default values of [[UIDataOverlayInteraction]].

## isActive()

If `true`, the overlay receives the input.

## setActive(boolean)

Lets the overlay receive the input or not.

## isCancelClick()

If `true`, a mouse input that the overlay consumed does not reach the host.

## setCancelClick(boolean)

Keeps the consumed mouse input from the host, or lets it through.

## isCancelScroll()

If `true`, a wheel turn that the overlay consumed does not reach the host.

## setCancelScroll(boolean)

Keeps the consumed wheel turns from the host, or lets them through.

## isCancelKeyboard()

If `true`, a key or a character that the overlay consumed does not reach the host.

## setCancelKeyboard(boolean)

Keeps the consumed keyboard input from the host, or lets it through.
