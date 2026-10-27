---
summary: Builds the full images of an animation from frames that update only a part of the picture, as GIF, APNG and WebP store them.
---

Create one per file at the size of the animation, then compose each frame in order.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## ResourceAnimationCanvas(int,int)

Creates a transparent canvas of `width` × `height` pixels.

## compose(int[],int,int,int,int,Blend,Disposal)

Draws `frame`, of `frameWidth` × `frameHeight` pixels, at `x`, `y` with `blend`, returns the full image that results, then prepares the canvas for the next frame as `disposal` says.
The part of the frame outside the canvas is ignored.

## getWidth()

Returns the width of the canvas.

## getHeight()

Returns the height of the canvas.

## getPixels()

Returns the pixels of the canvas, as the next frame reads them.
