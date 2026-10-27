---
summary: Signal of a list, changed through its own methods so that every change notifies the nodes and listeners that follow it.
---

The value is a read-only copy: `get()` returns a list that throws `UnsupportedOperationException` on any change.
Each method of the signal copies the list, applies the change to the copy and writes it; called from another thread, the change applies on the render thread to the list held then.
The read methods (`get(int)`, `size()`, `contains(...)`…) record the read like [[Signal#get]].

Learn: [Signals](/use/learn/interactivity/signals/), [Rebuilding the tree](/use/learn/state/rebuilding/).

## ListSignal()

Creates a signal whose value and default value are an empty list.

## ListSignal(Collection)

Creates a signal whose value and default value are a copy of `value`, in its iteration order.

## clear()

Removes every element, then returns this signal.

## add(E)

Adds `e` at the end of the list, then returns this signal.

## remove(E)

Removes the first element equal to `e`, then returns this signal; nothing changes when there is none.

## remove(int)

Removes the element at `index`, then returns this signal.
An index outside the list throws an `IndexOutOfBoundsException` when the change applies.

## set(int,E)

Replaces the element at `index` with `element`, then returns this signal.
An index outside the list throws an `IndexOutOfBoundsException` when the change applies.

## get(int)

Returns the element at `index`; throws an `IndexOutOfBoundsException` when there is none.

## contains(E)

If `true`, the list holds an element equal to `e`.

## indexOf(E)

Returns the index of the first element equal to `e`, or `-1`.

## isEmpty()

If `true`, the list holds no element.

## size()

Returns the number of elements.

## seal(List)

Returns a read-only copy of `value`, which the signal stores.
