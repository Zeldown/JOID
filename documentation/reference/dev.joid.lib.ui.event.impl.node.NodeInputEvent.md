---
summary: The base class of the input events of a node, which a listener consumes to stop the input.
---

The `handleX` listeners of a node and its input hooks receive one.
JOID creates one event per node on the path of an input, all bound to the input of the UI: consuming any of them consumes the input for every node after it.

Learn: [Events](/use/learn/interactivity/events/#hear-the-raw-input).

## NodeInputEvent(N,IConsumableEvent)

Creates an input event for a node, from the constructor of a subclass.
`source` is the input that the event belongs to, or `null` for an input of its own.

## consume()

Stops the input: no other `handle` listener, no `onClick`, none of the handling of a node, no UI below and not the host application receive it; only the `MONITOR` listeners still hear it.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isConsumed()

Returns `true` once a listener or a node consumed the input.
