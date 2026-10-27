---
summary: The nine standard font weights, from 100 (thin) to 900 (black).
---

A font draws a weight with the face of that weight; when the font has no such face, it draws the closest one and warns once in dev mode.
For a requested weight of 400 or above 500, the heavier of two equally close faces wins; otherwise the lighter one.

Learn: [Fonts](/use/learn/describing-the-ui/fonts/).

## getValue()

Returns the numeric weight, such as `400` for `REGULAR`.

## of(int)

Returns the weight closest to `value`, rounded to the nearest hundred and clamped between `THIN` and `BLACK`.

## THIN

Weight 100.

## EXTRA_LIGHT

Weight 200.

## LIGHT

Weight 300.

## REGULAR

Weight 400, the default of a [[TextInfo]].

## MEDIUM

Weight 500.

## SEMI_BOLD

Weight 600.

## BOLD

Weight 700.

## EXTRA_BOLD

Weight 800.

## BLACK

Weight 900.
