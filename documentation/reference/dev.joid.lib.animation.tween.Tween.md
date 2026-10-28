---
summary: Moves one `float` value, read and written through a getter and a setter, to a target over a duration.
---

The getter is read when the tween begins, after its delay, so a tween placed in a sequence starts from the value that the previous steps left.
[[TweenAnimator#tween]] gives a tween already wired to an animator.
A duration below `0` throws an `IllegalArgumentException`.
This tween moves `alpha` to `1F` in 400 ms; it moves only while its manager is updated:

```java file="com/example/reference/animation/FadeTween.java" region="tween"
```

Learn: [Animation](/use/learn/style-and-motion/animation/).

## to(Supplier,Consumer,float)

Creates a tween of `duration` ms that moves the value from where it is when the tween begins to the target.

## from(Supplier,Consumer,float)

Creates a tween of `duration` ms that moves the value from the target back to where it was when the tween began.

## set(Supplier,Consumer)

Creates a tween without duration that sets the target at once.

## call(EventListener)

Creates an empty step that calls `listener` when a timeline reaches it.

## mark()

Creates an empty step without value, a place in a timeline.

## target

The value to reach; a call to `target(...)` cancels a relative target.

## targetRelative(float)

Sets the target to the start value plus `target`, then returns this tween.

## waypoint(float)

Adds a value to pass through on the way, after the previous waypoints, then returns this tween.
With a relative target, the waypoint is relative to the start value too.

## path

The curve that joins the start, the waypoints and the target; it has an effect only with waypoints.

## easing

The curve that maps the elapsed time to the progress.
