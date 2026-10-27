---
summary: Decodes an image with a chosen reader and prepares pixels with transparent areas for filtering.
---

The image decoders of JOID use it; a decoder of your own can too.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## read(InputStream,ImageReaderSpi)

Decodes the first image of `stream` with the ImageIO reader of `spi`.
Throws an `IOException` when the stream cannot be read or decoded.

## bleedAlpha(int[],int,int)

Gives each fully transparent pixel of `pixels` (ARGB, `width` × `height`, row after row) the color of the nearest pixel that is not, and keeps its alpha at `0`.
A filtered texture then shows no dark fringe around its transparent areas; an image fully opaque or fully transparent stays unchanged.
