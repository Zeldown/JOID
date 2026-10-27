---
summary: Read-only list of the options of a slider range, whose values are computed from their index instead of stored.
---

[[IntegerSliderNode]] and [[DoubleSliderNode]] hold their range in one, so a range of millions of values costs nothing; `getValueList()` returns it.
`contains` and `indexOf` compute the index of a value instead of walking the list.

Learn: [Controls](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## RangeList()

Creates the list, for a subclass that gives `get(int)`, `size()`, `indexOf(Object)` and `nearest(E)`.

## nearest(E)

Returns the option nearest to `value`, the smaller one on a tie.

## contains(Object)

If `true`, `value` is one of the options; it calls `indexOf`.

## lastIndexOf(Object)

Returns the index of `value`, the same as `indexOf`, since an option appears once.

## toString()

Returns the options in brackets, shortened to the first two and the last one past ten options: `[1, 2, ..., 2147483647]`.
