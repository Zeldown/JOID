---
summary: The event of `beforeChange` on a field, which can refuse an edit.
---

It fires for what the user types, pastes, cuts or deletes, and for `change(...)`; the text or the value given by a setter or a bound signal is never refused.

Learn: [Controls](/use/learn/interactivity/controls/#refuse-a-change).

## create(N,String,String,V,boolean)

Creates the event for a field, the text now, the text asked for, the value read from it and whether it is valid.

## cancel()

Refuses the edit: the text stays as it was.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isCancelled()

If `true`, a listener refused the edit.
