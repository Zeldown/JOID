---
summary: A change of the scroll of a node on one axis, with the offset before and after, in a cancellable `Pre` and a `Post`.
---

[[Node#beforeScroll]] receives a [[NodeScrollEvent.Pre]] and can refuse the scroll; [[Node#onScroll]], [[Node#onScrollToEnd]] and [[Node#onScrollEndReached]] receive a [[NodeScrollEvent.Post]].

Learn: [Layout](/use/learn/describing-the-ui/layout/#react-to-scrolling).

## getAxis()

Returns the axis that scrolls: `HORIZONTAL` or `VERTICAL`.

## getOffset()

Returns the offset after the change, in design pixels: `0D` at the start of the content, down to minus the scrollable length at its end.
At `onScrollEndReached`, the offset that the content reached.

## getPrevious()

Returns the offset before the change, in design pixels.
