---
summary: The order in which the listeners of one event run, from `HIGHEST` to `LOWEST`, then `MONITOR`.
---

Listeners of the same priority run in the order they were added.
Once a listener cancels or consumes the event, the listeners after it do not run, except the `MONITOR` ones.

Learn: [Events](/use/learn/interactivity/events/#order-the-listeners-of-one-event).

## isMonitor()

Returns `true` for [[EventPriority#MONITOR]].

## HIGHEST

Runs before every other listener.

## HIGH

Runs after `HIGHEST`, before `NORMAL`.

## NORMAL

The priority of a plain lambda.

## LOW

Runs after `NORMAL`, before `LOWEST`.

## LOWEST

Runs after every other listener that can change the event.

## MONITOR

Runs last, even after a cancel or a consume, and only reads: `cancel()` or `consume()` there throws an `IllegalStateException`.
Use it to log or count what happened.
