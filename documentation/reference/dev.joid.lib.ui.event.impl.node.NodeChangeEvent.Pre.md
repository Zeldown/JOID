---
summary: The event of a `beforeX` listener of a node, which can refuse the change it announces.
---

It fires for a change that a gesture or a call such as `move(...)` or `change(...)` asks for; the setters and the bound signals are never refused.

Learn: [Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens).

## create(N,V,V)

Creates the event for a node, the value now and the value asked for.

## cancel()

Refuses the change: the value stays, and the `onX` event does not fire.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isCancelled()

Returns `true` once a listener refused the change.
