---
summary: Annotation that makes a UI an overlay, drawn with the screens instead of replacing them.
---
An overlay opens next to the current screen and stays open when the screen changes.
By default it is drawn below the screens and receives no input; `render` and `interaction` change that.
A UI cannot be both an overlay and a popup.

Learn: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).

## active()

If `true`, the UI is an overlay; the annotation without `active = true` changes nothing, and dev mode warns.

## render()

When the overlay is drawn: see [[UIDataOverlayRender]].

## interaction()

Whether the overlay receives the input, and what it lets through: see [[UIDataOverlayInteraction]].
