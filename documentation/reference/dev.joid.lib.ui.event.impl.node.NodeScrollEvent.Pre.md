---
summary: The event of `beforeScroll`, which can refuse a scroll of a node.
---

It fires for a scroll that the wheel, a scrollbar or a call such as `scrollTo(...)` asks for.

Learn: [Layout](/use/learn/describing-the-ui/layout/#react-to-scrolling).

## create(N,Axis,double,double)

Creates the event for a node, the axis, the offset now and the offset asked for.

## cancel()

Refuses the scroll: the content stays where it is.
Throws an `IllegalStateException` in a `MONITOR` listener.

## isCancelled()

If `true`, a listener refused the scroll.
