---
summary: The built-in format of MP4, Matroska (MKV, WebM) and AVI files, played as videos with FFmpeg.
---

A video needs the FFmpeg natives, which JOID downloads on the first use ([[VideoNatives]]).

Learn: [Media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/).

## VideoResourceFormat()

Creates the format.

## supports(byte[])

If `true`, `header` is an MP4, Matroska or AVI file.

## decoder(Asset,byte[])

Returns a [[VideoResourceDecoder]] that plays the file.
