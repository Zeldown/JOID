---
summary: Draws an image resource in its bounds, tinted by a color, with an optional image that fades in on hover.
---
The node waits for its resource: it draws its skeleton until the image is loaded.
Without a declared size it takes the size of the image; with one declared size, it keeps the ratio of the image on the other axis.
A resource that fails to load takes 64 × 64 design pixels when no size is declared, and draws a magenta and black checkerboard there in dev mode, nothing in production.

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## create(double,double)

Creates an image node at `x`, `y` that takes the size of its image.

## create(double,double,double,double)

Creates an image node with a declared size; a size of `0` on an axis follows the ratio of the image.

## ResourceNode(double,double,double,double)

Called by a subclass with the same arguments as `create`.

## resource

The image to draw.
Throws an `IllegalStateException` when the node is attached without one.

## hoveredResource

Image that fades in over the first one while the node is hovered; `null` keeps the image on hover.

## color

Tint multiplied with the image; white draws the image as it is.

## hoveredColor

Tint while the node is hovered, reached with the hover progress; `null` keeps the tint.

Default: none, the tint stays the same on hover

## linear

If `true`, the image is smoothed when scaled; if `false`, its pixels stay sharp; `null` keeps the filtering of the resource.

Default: the filtering of the resource

## stretch

How the image fills the bounds when their ratio differs from the ratio of the image.

## getFilteredResource()

The resource drawn for `resource`, a copy with the filtering of `linear` when it is set.

## getFilteredHoveredResource()

The resource drawn for `hoveredResource`, a copy with the filtering of `linear` when it is set.

## update()

Measures the image on every frame, so that the node takes its size as soon as it loads.

## draw(double,double)

Draws the image, tinted, then the hovered image faded by the hover progress.
