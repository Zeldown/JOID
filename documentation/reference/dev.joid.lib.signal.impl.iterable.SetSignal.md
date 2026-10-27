---
summary: Signal of a set that keeps the insertion order, changed through its own methods so that every change notifies its followers.
---

It works like [[ListSignal]]: the value is a read-only copy, each method writes a changed copy, and the read methods record the read.
The elements keep the order in which they were added.

Learn: [Signals](/use/learn/interactivity/signals/).

## SetSignal()

Creates a signal whose value and default value are an empty set.

## SetSignal(Collection)

Creates a signal whose value and default value are the distinct elements of `value`, in its iteration order.

## clear()

Removes every element, then returns this signal.

## add(E)

Adds `e`, then returns this signal; an element already in the set changes nothing.

## remove(E)

Removes `e`, then returns this signal; nothing changes when the set does not hold it.

## contains(E)

If `true`, the set holds `e`.

## isEmpty()

If `true`, the set holds no element.

## size()

Returns the number of elements.

## seal(Set)

Returns a read-only copy of `value` in the same order, which the signal stores.
