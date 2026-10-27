---
summary: Signal of a map that keeps the insertion order of its keys, changed through its own methods so that every change notifies its followers.
---

It works like [[ListSignal]]: the value is a read-only copy, each method writes a changed copy, and the read methods record the read.
The keys keep the order in which they were first put.

Learn: [Signals](/use/learn/interactivity/signals/).

## MapSignal()

Creates a signal whose value and default value are an empty map.

## MapSignal(Map)

Creates a signal whose value and default value are a copy of `value`, in its iteration order.

## clear()

Removes every entry, then returns this signal.

## get(K)

Returns the value of `key`, or `null` when the map does not hold it.

## put(K,V)

Sets the value of `key`, then returns this signal; a key already present keeps its place.

## remove(K)

Removes the entry of `key`, then returns this signal; nothing changes when the map does not hold it.

## containsKey(K)

If `true`, the map holds an entry for `key`.

## entrySet()

Returns the entries, read-only, in the order of the keys.

## isEmpty()

If `true`, the map holds no entry.

## keySet()

Returns the keys, read-only, in their order.

## size()

Returns the number of entries.

## values()

Returns the values, read-only, in the order of their keys.

## seal(Map)

Returns a read-only copy of `value` in the same order, which the signal stores.
