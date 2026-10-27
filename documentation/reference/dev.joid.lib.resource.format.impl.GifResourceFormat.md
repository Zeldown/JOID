---
summary: The built-in format of GIF files, played as animated images.
---

A GIF of one frame plays as an animation of one frame.

Learn: [Images and media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/).

## GifResourceFormat()

Creates the format.

## supports(byte[])

If `true`, `header` starts with `GIF87a` or `GIF89a`.

## decoder(Asset,byte[])

Returns an [[AnimatedResourceDecoder]] that reads the GIF.
