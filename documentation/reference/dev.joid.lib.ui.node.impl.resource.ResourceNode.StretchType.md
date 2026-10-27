---
summary: How an image or a video fills bounds whose ratio differs from its own.
---
Learn: [Images and media](/use/learn/describing-the-ui/media/#fit-tint-and-crop).

## STRETCH

Fills the bounds, deforming the image.

## CONTAIN

Shows the whole image, centered, as large as the bounds allow; the rest of the bounds stays empty.

## COVER

Fills the bounds with the image, centered, and crops what goes past them.

## draw(double,double,double,double,Resource)

Draws the resource in these bounds with this fit.
