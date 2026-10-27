---
summary: The built-in format of PNG files, still or animated (APNG).
---

A PNG with an animation control chunk plays as an animated image; any other PNG is a still image.

Learn: [Images and media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/).

## ApngResourceFormat()

Creates the format.

## supports(byte[])

If `true`, `header` starts with the PNG signature.

## decoder(Asset,byte[])

Returns an [[AnimatedResourceDecoder]] when the file is an APNG, otherwise a [[RasterResourceDecoder]]; it reads up to 64 KB when the header is not enough to tell.
