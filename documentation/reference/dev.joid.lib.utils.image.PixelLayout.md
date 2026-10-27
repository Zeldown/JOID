---
summary: Order of the four bytes of a pixel in a buffer, to convert between buffers and ARGB `int` pixels.
---

Learn: [Testing a backend](/build/testing-a-backend/).

## read(ByteBuffer,int,int,boolean)

Returns the `width` × `height` pixels of `source` as ARGB `int` values, row after row from the top, reading from the position of the buffer without moving it.
With `bottomUp`, the buffer holds the bottom row first, as a GL read gives it.

## write(int[],ByteBuffer)

Writes the ARGB `pixels` into `target` in this byte order, from its position without moving it, then returns `target`.

## RGBA8

Red, green, blue, then alpha, one byte each.

## BGRA8

Blue, green, red, then alpha, one byte each.
