---
summary: The base class of the input events of a UI, which a listener consumes to stop the input.
---

The `handleX` listeners of a UI receive an input after its nodes, and only when no node consumed it.
The nodes and the UI share the same input: the event of the UI is the source of the events of its nodes.

Learn: [Events](/use/learn/interactivity/events/#follow-an-input-through-the-tree).

## UIInputEvent(U)

Creates an input event for a UI, from the constructor of a subclass.

## consume()

Stops the input: no other listener except the `MONITOR` ones, no UI below and not the host application receive it.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isConsumed()

If `true`, a node or a listener consumed the input.
