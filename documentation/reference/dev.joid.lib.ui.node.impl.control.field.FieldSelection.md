---
summary: A range of characters of a field, from a start index to an end index.
---

The start is never after the end; an empty range has the start equal to the end.

Learn: [Text fields](/use/learn/interactivity/text-fields/#give-and-take-the-focus).

## of(int,int)

Creates a range from two indexes in any order.

## isEmpty()

If `true`, the range holds no character.

## getStart()

Returns the index of the first character of the range.

## getEnd()

Returns the index after the last character of the range.

## equals(Object)

If `true`, the other object is of the same class with the same start and end.

## hashCode()

Returns a hash of the values that `equals` compares.

## toString()

Returns the start and the end, for logs.
