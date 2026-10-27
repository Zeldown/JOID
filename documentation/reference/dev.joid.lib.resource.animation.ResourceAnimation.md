---
summary: The decoded frames of an animated image, with its size and its play count.
---

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## of(int,int,int,List)

Creates an animation of `width` × `height` pixels that plays `plays` times (`0` forever) through `frames`.
A frame shorter than 1 ms lasts 1 ms.
Throws `IllegalArgumentException` when `frames` is empty.

## getEnds()

Returns a copy of the end time of each frame, in milliseconds from the start.

## getDuration()

Returns the length of one play, in milliseconds.

## indexAt(long)

Returns the index of the frame shown at `time` milliseconds into a play.

## getWidth()

Returns the width of the animation, in pixels.

## getHeight()

Returns the height of the animation, in pixels.

## getPlays()

Returns how many times the animation plays, `0` for forever.

## getFrames()

Returns the frames, in order, as a list you cannot change.
