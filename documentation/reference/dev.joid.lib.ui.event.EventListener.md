---
summary: A function that JOID calls with an event, written as a lambda, with a priority that orders it among the other listeners.
---

Every `onX`, `beforeX` and `handleX` method of JOID takes one.
A lambda has the priority [[EventPriority#NORMAL]]; wrap it with `priority(...)` to give it another one.

Learn: [Events](/use/learn/interactivity/events/#order-the-listeners-of-one-event).

## priority(EventPriority,EventListener)

Returns the listener with another priority, for example `EventListener.priority(EventPriority.HIGH, event -> ...)`.

## apply(E)

Receives the event; JOID calls it when the event fires.

## getPriority()

Returns the priority of the listener, which orders it among the listeners of the same event.
