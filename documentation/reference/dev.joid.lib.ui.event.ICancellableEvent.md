---
summary: An event that a listener cancels to refuse the change it announces.
---

The `beforeX` events implement it: a `beforeMove`, `beforeChange` or `beforeClose` listener that calls `cancel()` refuses the change, and the listeners after it do not run, except the `MONITOR` ones.

Learn: [Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens).

## cancel()

Refuses the change: it does not apply, and its `onX` event does not fire.

## isCancelled()

If `true`, a listener cancelled the event.
