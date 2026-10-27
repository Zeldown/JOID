---
summary: List kept sorted by the index of its elements, with equal indexes in insertion order.
---

The children of a node and the UIs of a bridge live in one, sorted by their `zindex`.
When the index of an element changes, the list sorts itself again at the next read.

Learn: [Position and size](/use/learn/describing-the-ui/position-and-size/).

## linked()

Creates a list for one thread.

## concurrent()

Creates a list that several threads can read while one changes it: each change copies the list.

## add(E)

Adds `element` at its place; an element already in the list is not added twice.

## remove(E)

Removes `element`.

## clear()

Removes every element.

## size()

Returns the number of elements.

## isEmpty()

If `true`, the list holds no element.

## contains(E)

If `true`, the list holds `element`.

## getFirst()

Returns the element of the lowest index, or `null` when the list is empty.

## getLast()

Returns the element of the highest index, or `null` when the list is empty.

## get(int)

Returns the element at position `index` in the sorted order; throws an `IndexOutOfBoundsException` outside the list.

## ordered()

Returns a read-only view of the elements from the lowest index to the highest; the view follows the changes of the list.

## reversed()

Returns a read-only view of the elements from the highest index to the lowest; the view follows the changes of the list.

## iterator()

Iterates over the elements from the lowest index to the highest.
