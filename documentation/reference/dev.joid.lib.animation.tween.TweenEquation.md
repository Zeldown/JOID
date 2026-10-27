---
summary: Easing curve that turns the elapsed part of a step into its progress.
---

Pick one in [[TweenEquations]]; extend this class for a curve of your own.

Learn: [Animation](/use/learn/style-and-motion/animation/).

## TweenEquation()

Creates a curve, for a subclass.

## compute(float)

Returns the progress for `t`, the elapsed part of the step from `0F` to `1F`; it gives `0F` at the start and `1F` at the end, and may go past them on the way.

## isValueOf(String)

If `true`, `name` is the name of this curve, as `toString()` gives it (`"Quad.OUT"`).
