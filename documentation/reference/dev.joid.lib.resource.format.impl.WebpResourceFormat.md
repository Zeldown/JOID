---
summary: The built-in format of WebP files, still or animated.
---

An animated WebP plays as an animated image; any other WebP is a still image.

Learn: [Media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/).

## supports(byte[])

If `true`, `header` is a RIFF file of type `WEBP`.

## decoder(Asset,byte[])

Returns an [[AnimatedResourceDecoder]] for an animated WebP, otherwise a [[RasterResourceDecoder]] with the WebP reader.
