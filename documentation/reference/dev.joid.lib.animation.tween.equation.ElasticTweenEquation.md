---
summary: Easing curves that oscillate like a spring at the start or at the end.
---

Each constant is also in [[TweenEquations]], as `ELASTIC_IN`, `ELASTIC_OUT` and `ELASTIC_IN_OUT`.
`period(...)` and `amplitude(...)` give the same curve with other oscillations, whose `toString()` carries them, period first: `"Elastic.IN(0.3, 2.0)"`, which [[TweenEquations#of]] reads back.

Learn: [Animation](/use/learn/style-and-motion/animation/).

## ElasticTweenEquation(float,float)

Creates a curve with the oscillation `period` and the `amplitude`, for a subclass.

## period

The length of one oscillation, as a part of the step: smaller values oscillate faster.
`period(float)` returns a curve of the same direction with that period and leaves this one unchanged.

Default: `0.3F` in `IN` and `OUT`, `0.45F` in `IN_OUT`

## amplitude

The height of the oscillations; a value below `1F` counts as `1F`.
`amplitude(float)` returns a curve of the same direction with that amplitude and leaves this one unchanged.

Default: `0F` in the three constants, which counts as `1F`

## IN

Oscillates at the start, like a spring.

## OUT

Oscillates at the end, like a spring.

## IN_OUT

Oscillates at the start and at the end.
