---
summary: The event of `onWatch`, which fires after a watch of a node applied its properties.
---

[[Node#watch]] applies its properties each time its signal changes, then fires [[Node#onWatch]].

Learn: [Rebuilding the tree](/use/learn/state/rebuilding/).

## create(N,Object,WatchProperty[])

Creates the event for a node, the value of the watched signal and the properties applied.

## getValue()

Returns the value of the watched signal after the change.

## getProperties()

Returns the properties that the watch applied, as a read-only list.
