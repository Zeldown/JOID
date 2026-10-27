---
summary: Draws a filled rectangle on its bounds, with an optional border, and colors that fade on hover.
---
Its size is the `width` and `height` of the node; rounded corners, shadows and the other decorations come from [effects](/use/learn/style-and-motion/effects/).
The border properties drive a [[BorderNodeEffect]] that the node adds the first time you set one.

Learn: [Nodes and the tree](/use/learn/describing-the-ui/nodes/), [Colors and styling](/use/learn/style-and-motion/styling/).

## create(double,double,double,double)

Creates a rectangle at `x`, `y` with this width and height, in design pixels.

## RectNode(double,double,double,double)

Called by a subclass with the same arguments as `create`.

## color

Fill color of the rectangle.
A transparent fill draws nothing but the node can still be hovered and clicked.

## hoveredColor

Fill color while the node is hovered, reached with the hover progress; `null` keeps the fill unchanged on hover.

## borderColor

Color of the border.

## hoveredBorderColor

Color of the border while the node is hovered; `null` keeps the border unchanged on hover.

## borderStroke

Thickness of the border, in design pixels; `0` draws no border.

## borderFill

If `true`, the border covers its corners; if `false`, the corners stay empty.

## draw(double,double)

Fills the bounds with the color, blended toward `hoveredColor` by the hover progress.
A subclass that overrides it calls `super.draw(...)` to keep the fill.
