---
summary: Annotation that makes a UI a popup, opened above the current screen with an opening and a closing transition.
---
A popup takes every input while it is open: nothing reaches the UIs below it.
Add `@UIData(background = true)` to dim what is under it.
A UI cannot be both a popup and an overlay.

Learn: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/), [Transitions](/use/learn/style-and-motion/transitions/#choose-what-a-popup-plays).

## active()

If `true`, the UI is a popup; the annotation without `active = true` changes nothing, and dev mode warns.

## transition()

Which steps of the [[PopTransition]] the popup plays: on opening, on closing, both or none.
