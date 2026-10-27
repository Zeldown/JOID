---
summary: A segment of an outline, linear, quadratic or cubic, with the channels it contributes to.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## Edge(List)

Creates the edge of those points, in all three channels.

## start()

Returns the first point.

## end()

Returns the last point.

## startDirection()

Returns the direction of the edge at its start.

## endDirection()

Returns the direction of the edge at its end.

## distance(double,double,SignedDistance)

Writes into the result the signed distance from that point to the edge.

## pseudoDistance(double,double,SignedDistance)

Writes into the result the pseudo-distance from that point to the edge, extended beyond its ends.

## getX()

Returns the abscissas of the points.

## getY()

Returns the ordinates of the points.

## getColor()

Returns the channels of the edge, a bit mask of the three channels, `7` for all of them.

## setColor(int)

Sets the channels of the edge.
