---
summary: Draws convex shapes with a soft edge one screen pixel wide, so that a rotated or scaled shape stays smooth without multisampling.
---

[[DrawShape]] and [[DrawResource]] use it under a rotation or a fractional scale; you call it only to smooth geometry of your own.
Coordinates are in screen pixels of the current matrices: the edge fades over half a pixel inside and half a pixel outside the outline.

Learn: [Pixel alignment](/use/in-depth/pixel-alignment/).

## isConvex(Point[])

If `true`, the points, in order, form a convex polygon of 3 points or more; aligned points are allowed.

## polygon(float,float,float,float,Point[])

Draws the convex polygon through `points` in the color `red`, `green`, `blue`, `alpha` on 1, with a soft edge; a polygon thinner than one pixel draws with its coverage as opacity.

## rect(double,double,double,double,double[],float,float,float,float)

Draws the rectangle from `left`, `top` to `right`, `bottom` with a soft edge, textured by the bound texture between the coordinates `uv` (`u0`, `v0`, `u1`, `v1`), and tinted by the color on 1.
