---
summary: Whether an overlay receives the input and what it lets through, set in the interaction option of @UIDataOverlay.
---
An overlay that receives the input gets it before the screen when it is drawn above it.
The `cancel` options decide whether an input that a node of the overlay consumed still reaches the host application.

Learn: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/#draw-an-overlay-above-the-screens-and-let-it-take-input).

## active()

If `true`, the overlay receives the input.

## cancelClick()

If `true`, a mouse press, release or drag that the overlay consumed does not reach the host; if `false`, the host receives it too.

## cancelScroll()

If `true`, a wheel turn that the overlay consumed does not reach the host.

## cancelKeyboard()

If `true`, a key or a character that the overlay consumed does not reach the host.
