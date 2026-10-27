---
summary: The base class of every event that a listener receives.
---

An event holds what happened: the node or the UI it concerns and the values of the change or of the input.
A listener of the priority [[EventPriority#MONITOR]] receives it read-only: a subclass that changes its event, such as one that cancels or consumes it, calls `checkWritable` first.

Learn: [Events](/use/learn/interactivity/events/), [Custom nodes](/use/learn/going-further/custom-nodes/#fire-an-event-of-your-own).

## monitor(boolean)

Marks the event read-only while the `MONITOR` listeners of an [[EventBus]] run, and writable again after them.
[[EventBus#fireMonitor]] calls it.

## checkWritable(String)

Throws an `IllegalStateException` when a `MONITOR` listener receives the event.
Call it at the start of every method that changes the event; `verb` names that method in the message.
