---
summary: Plays tweens and other timelines one after the other or all together, as one tween.
---

`Timeline.sequence()` plays its children in order; `Timeline.parallel()` plays them together and lasts as long as the longest.
`beginSequence()` and `beginParallel()` open a nested block that `end()` closes.
A timeline takes its children before `start()`: after it, `push(...)`, `pushPause(...)`, `begin...()` and `end()` throw an `IllegalStateException`.
At the start, a child that repeats forever throws an `IllegalArgumentException` and a block left open throws an `IllegalStateException`.
This sequence moves `x`, waits, moves `y`, then plays back and forth forever:

```java file="com/example/style/TweenUI.java" region="timeline"
```

Learn: [Animation](/use/learn/style-and-motion/animation/).

## sequence()

Creates a timeline that plays its children one after the other.

## parallel()

Creates a timeline that plays its children together.

## push(Tween)

Adds `tween` to the open block, then returns this timeline.

## push(Timeline)

Adds `timeline` to the open block, then returns this timeline.
Throws an `IllegalArgumentException` when `timeline` still has a block open.

## pushPause(float)

Adds a wait of `time` ms to the open block, then returns this timeline.

## beginSequence()

Opens a nested block whose children play one after the other, then returns this timeline.

## beginParallel()

Opens a nested block whose children play together, then returns this timeline.

## end()

Closes the last block opened, then returns this timeline.
Throws an `IllegalStateException` when no block is open.

## getChildren()

Returns the children of the top level, read-only; a nested block is one child.
