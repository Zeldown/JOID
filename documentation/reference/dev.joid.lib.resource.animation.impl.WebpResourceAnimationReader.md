---
summary: Reads the frames of an animated WebP.
---

A frame duration of 10 ms or less shows for 100 ms.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## read(InputStream)

Reads every frame of the WebP, composed with its blend and disposal, and its play count.
Throws `IllegalArgumentException` when the file is not a WebP, has no animation frame, or holds a chunk that runs past its end.

## isWebp(byte[])

If `true`, `bytes` start with a RIFF header of type `WEBP`.

## isAnimated(byte[])

If `true`, `bytes` are a WebP whose extended header has the animation flag.
