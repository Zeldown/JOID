---
summary: Every easing curve of JOID as a constant, and the lookup of a curve by its name.
---

`IN` curves start slowly, `OUT` curves end slowly, `IN_OUT` curves do both.
The catalog shows every curve.

Learn: [Animation](/use/learn/style-and-motion/animation/), [Catalog](/use/catalog/).

## of(String)

Returns the curve named `name` (`"Linear.IN_OUT"`, `"Quad.IN"`, `"Elastic.IN_OUT"`…), or `null` for an unknown name.
It also reads the names of tuned curves that `toString()` gives, such as `"Back.OUT(2.5)"` or `"Elastic.IN(0.3, 2.0)"` (period, then amplitude).

## LINEAR

Constant speed.

## QUAD_IN

Starts slowly, with a power of 2.

## QUAD_OUT

Ends slowly, with a power of 2.

## QUAD_IN_OUT

Starts and ends slowly, with a power of 2.

## CUBIC_IN

Starts slowly, with a power of 3.

## CUBIC_OUT

Ends slowly, with a power of 3.

## CUBIC_IN_OUT

Starts and ends slowly, with a power of 3.

## QUART_IN

Starts slowly, with a power of 4.

## QUART_OUT

Ends slowly, with a power of 4.

## QUART_IN_OUT

Starts and ends slowly, with a power of 4.

## QUINT_IN

Starts slowly, with a power of 5.

## QUINT_OUT

Ends slowly, with a power of 5.

## QUINT_IN_OUT

Starts and ends slowly, with a power of 5.

## CIRC_IN

Starts slowly, along a quarter circle.

## CIRC_OUT

Ends slowly, along a quarter circle.

## CIRC_IN_OUT

Starts and ends slowly, along quarter circles.

## SINE_IN

Starts slowly, along a sine.

## SINE_OUT

Ends slowly, along a sine.

## SINE_IN_OUT

Starts and ends slowly, along a sine.

## EXPO_IN

Starts very slowly, with an exponential.

## EXPO_OUT

Ends very slowly, with an exponential.

## EXPO_IN_OUT

Starts and ends very slowly, with an exponential.

## BACK_IN

Moves back a little before it starts.

## BACK_OUT

Goes past the end a little before it settles.

## BACK_IN_OUT

Moves back a little at the start and goes past the end a little.

## BOUNCE_IN

Bounces at the start.

## BOUNCE_OUT

Bounces at the end, like a ball that falls.

## BOUNCE_IN_OUT

Bounces at the start and at the end.

## ELASTIC_IN

Oscillates at the start, like a spring.

## ELASTIC_OUT

Oscillates at the end, like a spring.

## ELASTIC_IN_OUT

Oscillates at the start and at the end.
