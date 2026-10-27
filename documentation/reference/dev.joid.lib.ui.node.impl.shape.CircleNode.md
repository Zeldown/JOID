---
summary: Draws a filled disc in its bounds, with a color that fades on hover.
---
The disc is centered in the bounds, with a diameter of the smaller of the width and the height.

Learn: [Nodes and the tree](/use/learn/describing-the-ui/nodes/), [Colors and styling](/use/learn/style-and-motion/styling/).

## create(double,double,double)

Creates a disc whose bounds start at `x`, `y` and are `diameter` design pixels wide and high.

## CircleNode(double,double,double,double)

Called by a subclass with the position and the size of the bounds.

## color

Fill color of the disc.

## hoveredColor

Fill color while the node is hovered, reached with the hover progress; `null` keeps the fill unchanged.

Default: none, the fill keeps `color` on hover

## draw(double,double)

Draws the disc with the color blended toward `hoveredColor` by the hover progress.

## drawSkeleton(double,double)

Draws a disc of the loading color while the node waits.
