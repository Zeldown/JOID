---
summary: A signed distance from a point to an edge, with its orthogonality to break ties.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## SignedDistance()

Creates the farthest possible distance.

## reset()

Goes back to the farthest possible distance, without edge.

## set(double,double,int)

Sets the distance, its orthogonality and the side of the edge.

## copy(SignedDistance)

Copies another distance, edge included.

## closerThan(SignedDistance)

If `true`, this distance is shorter in absolute value, or as short with a lower orthogonality.

## getSide()

Returns the side of the edge that the point is on.

## getDistance()

Returns the signed distance.

## getOrthogonality()

Returns the tie-breaker between two equal distances: the lower, the closer.

## getEdge()

Returns the edge of the distance, or `null`.

## setEdge(Edge)

Sets the edge of the distance.
