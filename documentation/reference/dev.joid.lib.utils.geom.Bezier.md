---
summary: Computes the points of quadratic and cubic Bézier curves.
---

The curve functions of [[DrawUtils]] use it to sample their curves.

Learn: [Drawing shapes and text](/use/learn/going-further/drawing/).

## quadratic(double,Point,Point,Point)

Returns the point at `t` (from `0D` at `start` to `1D` at `end`) of the curve pulled toward `control`.

## cubic(double,Point,Point,Point,Point)

Returns the point at `t` (from `0D` at `start` to `1D` at `end`) of the curve that leaves `start` toward `startControl` and reaches `end` from `endControl`.
