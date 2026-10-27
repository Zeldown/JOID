---
summary: Holds the listeners of an object, one ordered list per event type, and runs them when an event fires.
---

Every node and every UI holds its listeners in an `EventBus`; their `onX`, `beforeX` and `handleX` methods add to it.
The listeners of one type run in the order of their [[EventPriority]], then in the order they were added.
A listener that throws does not stop the others: JOID prints a warning once for each new failure of that listener and runs the next one.

Learn: [Events](/use/learn/interactivity/events/#order-the-listeners-of-one-event).

## create(Supplier)

Creates an empty bus.
`owner` gives the name of the object that holds it, printed in the warning of a listener that fails.

## listen(EventType,EventListener)

Adds a listener for a type of event, after the listeners of the same priority.

## addAll(EventBus)

Adds every listener of another bus to this one, type by type, in their order; a copy of a node gets the listeners of the original this way.

## keep()

Marks every listener added so far as permanent: `reset()` keeps them.
A UI calls it when it opens for the first time, so that the listeners of its constructor survive every closing.

## reset()

Removes every listener that `keep()` did not mark.
A UI calls it when it opens and when it closes, so that the listeners added in `init()` last one opening.

## clear()

Removes every listener, the permanent ones included.

## fire(EventType,Event)

Runs the listeners of a type: first `fireListeners`, then `fireMonitor`.

## fireListeners(EventType,Event)

Runs the listeners of a type that are not `MONITOR`, in their order, and stops as soon as one of them cancels or consumes the event.

## fireMonitor(EventType,Event)

Runs the `MONITOR` listeners of a type, even when the event is cancelled or consumed.
The event is read-only for them: `cancel()` or `consume()` throws an `IllegalStateException`.

## has(EventType)

If `true`, at least one listener listens to the type.

## isEmpty()

If `true`, the bus holds no listener.

## count(EventType)

Returns the number of listeners of a type.

## getTypes()

Returns the types that have at least one listener, as a read-only view.
