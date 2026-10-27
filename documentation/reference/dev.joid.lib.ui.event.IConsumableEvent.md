---
summary: An input event that a listener consumes to stop it.
---

The `handleX` events implement it.
A consumed input reaches no other listener except the `MONITOR` ones, none of the handling of a node, no UI below and not the host application.

Learn: [Events](/use/learn/interactivity/events/#hear-the-raw-input).

## consume()

Stops the input.

## isConsumed()

Returns `true` once a listener or a node consumed the input.
