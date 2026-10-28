---
summary: Easing curves that move back a little before they start or go past the end before they settle.
---

Each constant is also in [[TweenEquations]], as `BACK_IN`, `BACK_OUT` and `BACK_IN_OUT`.
`overshoot(...)` gives the same curve with a larger or smaller swing, whose `toString()` carries it: `"Back.OUT(2.5)"`, which [[TweenEquations#of]] reads back.

Learn: [Animation](/use/learn/style-and-motion/animation/).

## BackTweenEquation(float)

Creates a curve with the swing `overshoot`, for a subclass.

## overshoot

How far the curve swings past its ends: `0F` gives no swing, larger values swing further.
`overshoot(float)` returns a curve of the same direction with that swing and leaves this one unchanged.

Default: `1.70158F` in `IN`, `OUT` and `IN_OUT`

## IN

Moves back a little before it starts.

## OUT

Goes past the end a little before it settles.

## IN_OUT

Moves back a little at the start and goes past the end a little.
