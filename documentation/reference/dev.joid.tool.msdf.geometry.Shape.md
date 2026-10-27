---
summary: The outline of a glyph, as closed contours of edges.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## add(List)

Adds a contour.

## getContours()

Returns the contours, read-only.

## isEmpty()

If `true`, the outline has no contour.

## bounds()

Returns `{left, bottom, right, top}` of every point of the outline.

## orient()

Reverses the contours whose winding does not match their depth, so that outer contours and holes wind in opposite directions.
