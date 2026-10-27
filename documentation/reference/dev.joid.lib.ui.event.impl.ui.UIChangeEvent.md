---
summary: The change of a value of a UI, with the value before and the value after, in a cancellable `Pre` and a `Post`.
---

[[UI#beforeZoom]] receives a [[UIChangeEvent.Pre]] and can refuse a new zoom; [[UI#onZoom]] receives a [[UIChangeEvent.Post]] once the zoom changed.

Learn: [Design canvas](/use/learn/describing-the-ui/design-canvas/), [Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens).

## getValue()

Returns the value after the change: the value asked for in `Pre`, the value applied in `Post`.

## getPrevious()

Returns the value before the change.
