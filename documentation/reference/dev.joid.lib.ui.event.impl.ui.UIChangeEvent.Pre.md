---
summary: The event of a `beforeX` listener of a UI, which can refuse the change it announces.
---

Learn: [Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens).

## create(U,V,V)

Creates the event for a UI, the value now and the value asked for.

## cancel()

Refuses the change: the value stays, and the `onX` event does not fire.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isCancelled()

Returns `true` once a listener refused the change.
