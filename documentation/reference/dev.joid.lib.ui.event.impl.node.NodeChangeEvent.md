---
summary: The change of a value of a node, with the value before and the value after, in a cancellable `Pre` and a `Post`.
---

A `beforeX` listener receives a [[NodeChangeEvent.Pre]] before the change and can refuse it; an `onX` listener receives a [[NodeChangeEvent.Post]] once the value changed.
The moves of a node ([[Node#beforeMove]], [[Node#onMove]]), the value of a checkbox, a toggle or a choice (`beforeChange`, `onChange`), the focus, the cursor and the selection of a field, the opening of a selector, and the zoom and the rotation of a [[ModelViewerNode]] use it.

Learn: [Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens), [Controls](/use/learn/interactivity/controls/#react-to-a-change).

## getValue()

Returns the value after the change: the value asked for in `Pre`, the value applied in `Post`.

## getPrevious()

Returns the value before the change.
