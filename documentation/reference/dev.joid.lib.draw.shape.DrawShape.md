---
summary: Draws filled and outlined shapes, lines and curves with a `Color`, sharp at every scale.
---

Reach it through `DrawUtils.SHAPE`.
Positions and sizes are in design pixels, in the coordinates you draw in; every method accepts a gradient or an animated [[Color]].
Edges stay sharp by themselves: on the pixel grid they snap to whole pixels, and under a rotation or a fractional scale they are smoothed, so no method has a thin or smooth variant.
A rectangle without width or height draws nothing.

Learn: [Drawing shapes and text](/use/learn/going-further/drawing/), [Pixel alignment](/use/in-depth/pixel-alignment/).

## inst()

Returns the shared instance, the one in `DrawUtils.SHAPE`.

## drawRect(double,double,double,double,Color)

Fills the rectangle at `x`, `y` of `width` × `height`.
A rectangle thinner than one screen pixel draws with its coverage as opacity, so a hairline stays visible.

## drawRoundedRect(double,double,double,double,Color,float)

Fills the rectangle with its four corners rounded by `radius`.
The radius is capped at half the shorter side, so `999F` draws a pill.

## drawRoundedRect(double,double,double,double,Color,float,boolean,boolean,boolean,boolean)

Fills the rectangle and rounds only some corners, with one `boolean` per side: `roundedLeft`, `roundedTop`, `roundedRight` and `roundedBottom`.
A corner is rounded when both of its sides are: `true, true, true, false` keeps the bottom corners square.

## drawRoundedBorder(double,double,double,double,Color,float)

Strokes the outline of the rounded rectangle, 1 design pixel wide, inside its edge.

## drawRoundedBorder(double,double,double,double,Color,float,double)

Strokes the outline of the rounded rectangle, `stroke` design pixels wide, inside its edge.

## drawShadow(double,double,double,double,Color,float,float)

Draws the soft shadow of a rounded box: the box at `x`, `y` of `width` × `height` with corner `radius`, blurred by `blur` design pixels.
The shadow spreads 1.5 times the blur beyond the box; a `blur` of `0F` or less draws a plain rounded rectangle.
A gradient color uses its start color.

## drawShadow(double,double,double,double,Color,float,boolean,boolean,boolean,boolean,float)

Draws the soft shadow of a box whose corners are rounded only on the chosen sides, as in `drawRoundedRect`, blurred by `blur` design pixels.

## drawCircle(double,double,Color,double)

Fills the disc centered on `x`, `y` with the given `radius`.

## drawBorder(double,double,double,double,Color)

Strokes a border 1 design pixel wide inside the rectangle at `x`, `y` of `width` × `height`.
It covers the same pixels as `drawRoundedBorder` with a radius of `0F`.

## drawBorder(double,double,double,double,Color,double)

Strokes a border `stroke` design pixels wide inside the rectangle.

## drawPolygon(Color,Point[])

Fills the polygon through `points`, in order; a convex polygon gets smoothed edges.

## drawLine(Color,Point[])

Draws a smoothed line through `points`, in order, with the line width of the render state, in design pixels.

## drawLine(Color,float,Point[])

Draws a smoothed line through `points`, `stroke` design pixels wide.

## drawDashedLine(Color,int,float,Point[])

Draws a dashed line through `points`, `stroke` design pixels wide: dashes of `pattern` design pixels separated by gaps of the same length, restarted at each point.
Throws `IllegalArgumentException` when `pattern` is 0 or less.

## drawCurvedLine(Color,Point,Point,Point)

Draws a quadratic Bézier curve from `start` to `end`, pulled toward `control`, in about one segment per design pixel of its length.

## drawCurvedLine(Color,float,Point,Point,Point)

Draws the quadratic Bézier curve `stroke` design pixels wide.

## drawCurvedLine(Color,Point,Point,Point,Point)

Draws a cubic Bézier curve from `start` to `end`, shaped by `startControl` and `endControl`.

## drawCurvedLine(Color,float,Point,Point,Point,Point)

Draws the cubic Bézier curve `stroke` design pixels wide.

## drawShape(DrawMode,Color,Point[])

Draws `points` as the primitive that `mode` names, such as [[DrawMode#LINES]] or [[DrawMode#POLYGON]].

## drawRawRect(double,double,double,double)

Fills the rectangle with the current color of the render state, without a texture and without edge smoothing.
Use it inside a shader pass or a mask, where the color comes from elsewhere.
