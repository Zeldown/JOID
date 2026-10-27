---
summary: Curve that joins the start, the waypoints and the target of a tween.
---

[[TweenPaths]] holds the curves of JOID; implement this interface for a path of your own.

Learn: [Animation](/use/learn/style-and-motion/animation/).

## compute(float,float[],int)

Returns the value at `t`, the progress from `0F` to `1F`, on the path through the first `pointCount` values of `points` (start, waypoints, target).
