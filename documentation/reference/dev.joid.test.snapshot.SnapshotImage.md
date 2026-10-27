---
summary: The pixels of a snapshot, as opaque ARGB `int` values row after row from the top-left corner.
---

Learn: [Render a screen](/use/learn/running/testing/#render-a-screen).

## from(ByteBuffer,int,int,boolean,PixelLayout)

Reads the pixels of a capture in that layout, bottom row first when the boolean is `true`, and makes them opaque.

## read(File)

Reads a PNG file; throws an `UncheckedIOException` when it cannot be read or decoded.

## write(File)

Writes the image as a PNG file, creating its folders; throws an `UncheckedIOException` when it cannot be written.

## isSame(SnapshotImage)

If `true`, both images have the same size and the same pixels.

## fill(int,int,int,int,int)

Fills that rectangle, clipped to the image, with that ARGB color.

## compare(SnapshotImage,int)

Returns how much the image differs from the reference: the pixels whose largest channel difference is above the tolerance, every pixel when the sizes differ.

## getWidth()

Returns the width, in pixels.

## getHeight()

Returns the height, in pixels.

## getPixels()

Returns the pixels, ARGB, row after row from the top-left corner.
